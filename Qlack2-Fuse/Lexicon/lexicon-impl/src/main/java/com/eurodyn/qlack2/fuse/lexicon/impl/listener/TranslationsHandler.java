package com.eurodyn.qlack2.fuse.lexicon.impl.listener;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import com.eurodyn.qlack2.fuse.lexicon.api.GroupService;
import com.eurodyn.qlack2.fuse.lexicon.api.KeyService;
import com.eurodyn.qlack2.fuse.lexicon.api.LanguageService;
import com.eurodyn.qlack2.fuse.lexicon.api.dto.GroupDTO;
import com.eurodyn.qlack2.fuse.lexicon.api.dto.KeyDTO;
import com.eurodyn.qlack2.fuse.lexicon.api.dto.LanguageDTO;
import com.eurodyn.qlack2.fuse.lexicon.impl.model.Application;

public class TranslationsHandler implements Runnable {
	private final static Logger LOGGER = Logger.getLogger(TranslationsHandler.class.getName());
	private EntityManager em;
	private GroupService groupService;
	private LanguageService languageService;
	private KeyService keyService;
	private TransactionManager transactionManager;
	
	private String bundleSymbolicName;
	private URL yamlUrl;

	public TranslationsHandler(EntityManager em, GroupService groupService,
			LanguageService languageService, KeyService keyService,
			TransactionManager transactionManager, String bundleSymbolicName,
			URL yamlUrl) {
		super();
		this.em = em;
		this.groupService = groupService;
		this.languageService = languageService;
		this.keyService = keyService;
		this.transactionManager = transactionManager;
		this.bundleSymbolicName = bundleSymbolicName;
		this.yamlUrl = yamlUrl;
	}

	@Override
	public void run() {
		handle(bundleSymbolicName, yamlUrl);
	}
	
	public void handle(String symbolicName, URL yamlUrl) {
		LOGGER.log(Level.INFO, "Handling bundle " + symbolicName + " in Lexicon Translations Handler");
		
		try {
			transactionManager.begin();
			
			String checksum = DigestUtils.md5Hex(yamlUrl.openStream());
			Yaml yaml = new Yaml(new CustomClassLoaderConstructor(getClass().getClassLoader()));

			//Check if this file has been executed again in the past
			Application application = Application.findBySymbolicName(symbolicName, em);
			if (application == null) {
				// If the application was not found in the DB initialize the entity
				// to use it lated on when registering the lexicon file execution.
				LOGGER.log(Level.FINE, "Processing translations of bundle " + symbolicName);
				application = new Application();
				application.setSymbolicName(symbolicName);
			} else if (application.getChecksum().equals(checksum)) {
				// If the application has been processed before and the lexicon file
				// has not been modified in the meantime no need to proceed with the processing.
				LOGGER.log(Level.FINEST, "Ignoring translations found in bundle " + symbolicName
						+ "; the translations are unchanged since the last time they were processed");
				transactionManager.commit();
				return;
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> contents = (Map<String, Object>) yaml.load(yamlUrl.openStream());

			// Process translation groups
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> groups = (List<Map<String, Object>>) contents.get("groups");
			for (Map<String, Object> group : groups) {
				String groupName = (String) group.get("name");
				String groupDescription = (String) group.get("description");
				LOGGER.log(Level.FINE, "Looking for translations group " + groupName);
				GroupDTO groupDTO = groupService.getGroupByName(groupName);
				// If a group with this name does not exist create it.
				if (groupDTO == null) {
					LOGGER.log(Level.FINE, "Group not found; it will be created.");
					groupDTO = new GroupDTO();
					groupDTO.setTitle(groupName);
					groupDTO.setDescription(groupDescription);
					groupService.createGroup(groupDTO);
				}
				// Else check the value of the forceUpdate flag
				else if ((group.get("forceUpdate") != null) && ((Boolean)group.get("forceUpdate") == true)) {
					groupDTO.setDescription(groupDescription);
					groupService.updateGroup(groupDTO);
				}
			}

			// Process languages
			processLanguages(contents);

			//Process translations
			processTranslations(contents);

			//Register the processing of the file in the DB
			application.setChecksum(checksum);
			application.setExecutedOn(DateTime.now().getMillis());
			em.merge(application);
			
			transactionManager.commit();
		} catch (IOException | NotSupportedException | SystemException | IllegalStateException | 
				SecurityException | HeuristicMixedException | HeuristicRollbackException | RollbackException ex) {
			LOGGER.log(Level.SEVERE, "Error handling lexicon YAML file", ex);
			try {
				transactionManager.rollback();
			} catch (IllegalStateException | SecurityException
					| SystemException e) {
				LOGGER.log(Level.SEVERE, "Error rolling back Lexicon transaction", e);
			}
		} 
	}

	private void processTranslations(Map<String, Object> contents) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> translationContents = (List<Map<String, Object>>) contents.get("translations");
		for (Map<String, Object> translationContent : translationContents) {
			LOGGER.log(Level.FINE, "Processing translations.");
			String groupName = (String) translationContent.get("group");
			String groupId = groupService.getGroupByName(groupName).getId();
			String locale = (String) translationContent.get("locale");
			String languageId = languageService.getLanguageByLocale(locale).getId();
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> translations = (List<Map<String, Object>>)translationContent.get("keys");

			for (Map<String, Object> translation : translations) {
				String translationKey = translation.keySet().iterator().next();
				String translationValue = (String) translation.get(translationKey);
				KeyDTO keyDTO = keyService.getKeyByName(translationKey, groupId, true);
				// If the key does not exist in the DB then create it.
				if (keyDTO == null) {
					keyDTO = new KeyDTO();
					keyDTO.setGroupId(groupId);
					keyDTO.setName(translationKey);
					Map<String, String> keyTranslations = new HashMap<>();
					keyTranslations.put(languageId, translationValue);
					keyDTO.setTranslations(keyTranslations);
					keyService.createKey(keyDTO, false);
				}
				// If the key exists check if a translation exists and if it does check
				// if it is the same as the key name, which means that the translation
				// was created automatically (ex. when adding a new language) and
				// therefore it should be updated. Otherwise only update the key if
				// the forceUpdate flag is set to true.
				else if ((keyDTO.getTranslations().get(languageId) == null)
							|| (keyDTO.getTranslations().get(languageId).equals(translationKey))
							|| ((translation.get("forceUpdate") != null) && ((Boolean)translation.get("forceUpdate") == true))) {
					keyService.updateTranslation(keyDTO.getId(), languageId, translationValue);
				}
			}
		}
	}

	private void processLanguages(Map<String, Object> contents) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> languages = (List<Map<String, Object>>) contents.get("languages");
		for (Map<String, Object> language : languages) {
			String languageName = (String) language.get("name");
			String locale = (String) language.get("locale");
			LOGGER.log(Level.FINE, "Looking for language " + languageName + " with locale " + locale);
			LanguageDTO languageDTO = languageService.getLanguageByLocale(locale);
			// If a language with this locale does not exist create it.
			if (languageDTO == null) {
				LOGGER.log(Level.FINE, "Language not found; it will be created.");
				languageDTO = new LanguageDTO();
				languageDTO.setName(languageName);
				languageDTO.setLocale(locale);
				languageDTO.setActive(true);
				languageService.createLanguage(languageDTO, null);
			}
			// Else check the value of the forceUpdate flag
			else if ((language.get("forceUpdate") != null) && ((Boolean)language.get("forceUpdate") == true)) {
				languageDTO.setName(languageName);
				languageService.updateLanguage(languageDTO);
			}
		}
	}

}
