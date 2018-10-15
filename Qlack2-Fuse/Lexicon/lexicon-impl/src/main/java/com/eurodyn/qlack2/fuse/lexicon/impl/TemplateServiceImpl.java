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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import com.eurodyn.qlack2.fuse.lexicon.api.TemplateService;
import com.eurodyn.qlack2.fuse.lexicon.api.dto.TemplateDTO;
import com.eurodyn.qlack2.fuse.lexicon.api.exception.QTemplateProcessingException;
import com.eurodyn.qlack2.fuse.lexicon.impl.model.Language;
import com.eurodyn.qlack2.fuse.lexicon.impl.model.Template;
import com.eurodyn.qlack2.fuse.lexicon.impl.util.ConverterUtil;


import freemarker.template.TemplateException;

public class TemplateServiceImpl implements TemplateService {
	private static final Logger LOGGER = Logger.getLogger(TemplateServiceImpl.class.getName());
	private EntityManager em;

	public void setEm(EntityManager em) {
		this.em = em;
	}

	@Override
	public String createTemplate(TemplateDTO template) {
		Template entity = new Template();
		entity.setName(template.getName());
		entity.setContent(template.getContent());
		entity.setLanguage(Language.find(template.getLanguageId(), em));
		em.persist(entity);
		return entity.getId();
	}

	@Override
	public void updateTemplate(TemplateDTO template) {
		Template entity = Template.find(template.getId(), em);
		entity.setName(template.getName());
		entity.setContent(template.getContent());
		entity.setLanguage(Language.find(template.getLanguageId(), em));
	}

	@Override
	public void deleteTemplate(String templateID) {
		em.remove(Template.find(templateID, em));
	}

	@Override
	public TemplateDTO getTemplate(String templateID) {
		return ConverterUtil.templateToTemplateDTO(Template.find(templateID, em));
	}

	@Override
	public Map<String, String> getTemplateContentByName(String templateName) {
		List<Template> templates = Template.findByName(templateName, em);
		if (templates.isEmpty()) {
			return null;
		}
		Map<String, String> contents = new HashMap<>();
		for (Template template : templates) {
			contents.put(template.getLanguage().getId(), template.getContent());
		}
		return contents;
	}

	@Override
	public String getTemplateContentByName(String templateName,
			String languageId) {
		Template template = Template.findByNameAndLanguageId(templateName, languageId, em);
		if (template == null) {
			return null;
		}
		return template.getContent();
	}

	@Override
	public String processTemplateByName(String templateName, String languageId,
			Map<String, Object> templateData) {
		Template template = Template.findByNameAndLanguageId(templateName, languageId, em);

		return processTemplate(template, templateData);
	}

	@Override
	public String processTemplateByNameAndLocale(String templateName,
			String locale, Map<String, Object> templateData) {
		Template template = Template.findByNameAndLocale(templateName, locale, em);
		return processTemplate(template, templateData);
	}

	private String processTemplate(Template template,
			Map<String, Object> templateData) {
		StringWriter retVal = new StringWriter();
        try {
        	freemarker.template.Template fTemplate = new freemarker.template.Template(template.getName(),
        			new StringReader(template.getContent()), null);
        	fTemplate.process(templateData, retVal);
            retVal.flush();
        } catch (TemplateException | IOException ex) {
        	// Catch exceptions and throw RuntimeException instead in order to
        	// also roll back the transaction.
        	LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            throw new QTemplateProcessingException("Error processing template " + template.getName()
            		+ " for language  " + template.getLanguage().getLocale());
        }
        return retVal.toString();
	}

}
