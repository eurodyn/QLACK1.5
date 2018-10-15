package com.eurodyn.qlack2.webdesktop.web.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.eurodyn.qlack2.fuse.lexicon.api.GroupService;
import com.eurodyn.qlack2.fuse.lexicon.api.KeyService;
import com.eurodyn.qlack2.fuse.lexicon.api.LanguageService;
import com.eurodyn.qlack2.fuse.lexicon.api.dto.GroupDTO;
import com.eurodyn.qlack2.fuse.lexicon.api.dto.LanguageDTO;

@Path("i18n")
public class I18nRest {
	private static final Logger LOGGER = Logger.getLogger(I18nRest.class.getName());

	private LanguageService languageService;
	private KeyService keyService;
	private GroupService groupService;

	public void setLanguageService(LanguageService languageService) {
		this.languageService = languageService;
	}

	public void setKeyService(KeyService keyService) {
		this.keyService = keyService;
	}

	public void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	@GET
	@Path("languages")
	@Produces(MediaType.APPLICATION_JSON)
	public List<LanguageDTO> getActiveLanguages() {
		return languageService.getLanguages(false);
	}

	@GET
	@Path("translations/{groupName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> getModuleTranslations(@PathParam("groupName") String groupName, @QueryParam("lang") String locale) {
		GroupDTO group = groupService.getGroupByName(groupName);
		if (group == null) {
			LOGGER.log(Level.SEVERE, "Error retrieving translations for group " + groupName + ". The group does not exist");
		}
		Map<String, String> translations = keyService.getTranslationsForGroupAndLocale(group.getId(), locale);
		return translations;
	}

	@GET
	@Path("translations")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Map<String, String>> getTranslations(@QueryParam("lang") String locale) {
		Map<String, Map<String, String>> retVal = new HashMap<>();
		Set<GroupDTO> groups = groupService.getGroups();
		for (GroupDTO group : groups) {
			Map<String, String> translations = keyService.getTranslationsForGroupAndLocale(group.getId(), locale);
			retVal.put(group.getTitle(), translations);
		}
		return retVal;
	}

}
