/*
* Copyright 2014 EUROPEAN DYNAMICS SA <info@eurodyn.com>
*
* Licensed under the EUPL, Version 1.1 only (the "License").
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
* https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and
* limitations under the Licence.
*/
package com.eurodyn.qlack2.fuse.lexicon.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.eurodyn.qlack2.fuse.caching.client.api.CacheService;
import com.eurodyn.qlack2.fuse.lexicon.api.KeyService;
import com.eurodyn.qlack2.fuse.lexicon.api.LanguageService;
import com.eurodyn.qlack2.fuse.lexicon.api.dto.LanguageDTO;
import com.eurodyn.qlack2.fuse.lexicon.api.exception.QLanguageProcessingException;
import com.eurodyn.qlack2.fuse.lexicon.impl.model.Group;
import com.eurodyn.qlack2.fuse.lexicon.impl.model.Key;
import com.eurodyn.qlack2.fuse.lexicon.impl.model.Language;
import com.eurodyn.qlack2.fuse.lexicon.impl.util.Constants;
import com.eurodyn.qlack2.fuse.lexicon.impl.util.ConverterUtil;

public class LanguageServiceImpl implements LanguageService {
	private static final Logger LOGGER= Logger.getLogger(LanguageServiceImpl.class.getName());
	private EntityManager em;
	private KeyService keyService;
	private List<CacheService> cacheServiceList;

	public void setEm(EntityManager em) {
		this.em = em;
	}

	public void setKeyService(KeyService keyService) {
		this.keyService = keyService;
	}

	public void setCacheServiceList(List<CacheService> cacheServiceList) {
		this.cacheServiceList = cacheServiceList;
	}

	@Override
	public String createLanguage(LanguageDTO language) {
		Language entity = ConverterUtil.languageDTOToLanguage(language);
        em.persist(entity);

        // Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).invalidate(Constants.TRANSLATIONS_CACHE_NAMESPACE);
        }

        return entity.getId();
	}

	@Override
	public String createLanguage(LanguageDTO language, String translationPrefix) {
		Language entity = ConverterUtil.languageDTOToLanguage(language);
        em.persist(entity);
        Map<String, String> translations = new HashMap<>();
        for (Key key : Key.getAllKeys(em)) {
        	translations.put(key.getId(), (translationPrefix != null) ? (translationPrefix + key.getName()) : key.getName());
        }
		keyService.updateTranslationsForLanguage(entity.getId(), translations);

        // Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).invalidate(Constants.TRANSLATIONS_CACHE_NAMESPACE);
        }

        return entity.getId();
	}

	@Override
	public String createLanguage(LanguageDTO language,
			String sourceLanguageId, String translationPrefix) {
		Language entity = ConverterUtil.languageDTOToLanguage(language);

		entity.setId(sourceLanguageId);
        em.persist(entity);
        Map<String, String> translations;

		translations = keyService.getTranslationsForLocale(em.find(Language.class, sourceLanguageId).getLocale().toString());
		if (translationPrefix != null) {
			for (String keyId : translations.keySet()) {
	        	translations.put(keyId, translationPrefix + translations.get(keyId));
	        }
		}
		keyService.updateTranslationsForLanguage(entity.getId(), translations);

		// Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).invalidate(Constants.TRANSLATIONS_CACHE_NAMESPACE);
        }

        return entity.getId();
	}

	@Override
	public void updateLanguage(LanguageDTO language) {
		Language entity = Language.find(language.getId(), em);
		entity.setName(language.getName());
		entity.setLocale(language.getLocale());

		// Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).invalidate(Constants.TRANSLATIONS_CACHE_NAMESPACE);
        }
	}

	@Override
	public void deleteLanguage(String languageID) {
		em.remove(Language.find(languageID, em));

		// Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).invalidate(Constants.TRANSLATIONS_CACHE_NAMESPACE);
        }
	}

	@Override
	public void activateLanguage(String languageID) {
		Language language = Language.find(languageID, em);
		language.setActive(true);
	}

	@Override
	public void deactivateLanguage(String languageID) {
		Language language = Language.find(languageID, em);
		language.setActive(false);

		// Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).invalidate(Constants.TRANSLATIONS_CACHE_NAMESPACE);
        }
	}

	@Override
	public LanguageDTO getLanguage(String languageID) {
		return ConverterUtil.languageToLanguageDTO(em.find(Language.class, languageID));
	}

	@Override
	public LanguageDTO getLanguageByLocale(String locale) {
		return ConverterUtil.languageToLanguageDTO(Language.findByLocale(locale, em));
	}

	@Override
	public List<LanguageDTO> getLanguages(boolean includeInactive) {
		List<Language> languages = null;
		if (includeInactive) {
			languages = Language.getAllLanguages(em);
		} else {
			languages = Language.getActiveLanguages(em);
		}
		return ConverterUtil.languageToLanguageDTOList(languages);
	}

	@Override
	public String getEffectiveLanguage(String locale, String defaultLocale) {
		Language language = Language.findByLocale(locale, em);
		if ((language != null) && (language.isActive())) {
			return locale;
		}

        // If no active language was found and the user-locale can be further reduced, try again,
        if (locale.indexOf("_") > -1) {
            String reducedLocale = locale.substring(0, locale.indexOf("_"));
            language = Language.findByLocale(reducedLocale, em);
            if ((language != null) && (language.isActive())) {
    			return reducedLocale;
    		}
        }

        // If nothing worked, return the default locale after checking
        // that it corresponds to an existing active language
        Language defaultLanguage = Language.findByLocale(defaultLocale, em);
        if ((defaultLanguage != null) && (defaultLanguage.isActive())) {
        	return defaultLocale;
        }
        return null;
	}

	@Override
	public byte[] downloadLanguage(String languageID) {
		byte[] retVal = null;

        // Check that the language exists and get its translations
		Language language = Language.find(languageID, em);

        // Create an Excel workbook. The workbook will contain a sheet for each group.
        Workbook wb = new HSSFWorkbook();
        CreationHelper createHelper = wb.getCreationHelper();

        // Iterate over all existing groups and create a sheet for each one.
        // Creating a new list below and not using the one retrieved from
        // Group.getAllGroups since result lists are read only and
        // we need to add the empty group below to the list.
        List<Group> groups = new ArrayList<>(Group.getAllGroups(em));
        // Add an dummy entry to the list to also check for translations without a group.
        Group emptyGroup = new Group();
        emptyGroup.setId(null);
        emptyGroup.setTitle("<No group>");
        groups.add(0, emptyGroup);
        for (Group group : groups) {
        	Map<String, String> translations;
			translations = keyService.getTranslationsForGroupAndLocale(group.getId(), language.getLocale());
        	if (!translations.isEmpty()) {
	        	Sheet sheet = wb.createSheet(group.getTitle());

	        	// Add the header.
	            Row headerRow = sheet.createRow(0);
	            headerRow.createCell(0).setCellValue(createHelper.createRichTextString("Key"));
	            headerRow.createCell(1).setCellValue(createHelper.createRichTextString("Translation"));

	            // Add the data.
	            int rowCounter = 1;
	            for (String key : translations.keySet()) {
	                Row row = sheet.createRow(rowCounter++);
	                row.createCell(0).setCellValue(createHelper.createRichTextString(key));
	                row.createCell(1).setCellValue(createHelper.createRichTextString(translations.get(key)));
	            }
        	}
        }

        // Create the byte[] holding the Excel data.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            wb.write(bos);
            retVal = bos.toByteArray();
        } catch (IOException ex) {
        	// Convert to a runtime exception in order to roll back transaction
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            throw new QLanguageProcessingException("Error creating Excel file for language " + languageID);
        }

        return retVal;
	}

	@Override
	public void uploadLanguage(String languageID, byte[] lgXL) {
		Map<String, String> translations = new HashMap<>();
        try {
            Workbook wb = WorkbookFactory.create(new BufferedInputStream(
                    new ByteArrayInputStream(lgXL)));
            for (int si = 0; si < wb.getNumberOfSheets(); si++) {
            	Sheet sheet = wb.getSheetAt(si);
            	String groupName = sheet.getSheetName();
            	String groupID = null;
            	if (StringUtils.isNotBlank(groupName)) {
            		groupID = Group.findByName(groupName, em).getId();
            	}
                // Skip first row (the header of the Excel file) and start parsing translations.
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    String keyName = sheet.getRow(i).getCell(0).getStringCellValue();
                    String keyValue = sheet.getRow(i).getCell(1).getStringCellValue();
                    translations.put(keyName, keyValue);
                }
                keyService.updateTranslationsForLanguageByKeyName(languageID, groupID, translations);
            }
        } catch (IOException | InvalidFormatException ex) {
        	// Convert to a runtime exception in order to roll back transaction
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            throw new QLanguageProcessingException("Error reading Excel file for language " + languageID);
        }

        // Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).invalidate(Constants.TRANSLATIONS_CACHE_NAMESPACE);
        }

	}

}
