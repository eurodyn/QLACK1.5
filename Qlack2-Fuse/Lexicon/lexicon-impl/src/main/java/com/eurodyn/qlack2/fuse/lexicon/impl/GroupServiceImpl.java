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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.eurodyn.qlack2.fuse.caching.client.api.CacheService;
import com.eurodyn.qlack2.fuse.lexicon.api.GroupService;
import com.eurodyn.qlack2.fuse.lexicon.api.dto.GroupDTO;
import com.eurodyn.qlack2.fuse.lexicon.impl.model.Data;
import com.eurodyn.qlack2.fuse.lexicon.impl.model.Group;
import com.eurodyn.qlack2.fuse.lexicon.impl.model.Language;
import com.eurodyn.qlack2.fuse.lexicon.impl.util.Constants;
import com.eurodyn.qlack2.fuse.lexicon.impl.util.ConverterUtil;

public class GroupServiceImpl implements GroupService {
	private EntityManager em;
	private List<CacheService> cacheServiceList;
	private Long translationsCacheTime;

	public void setEm(EntityManager em) {
		this.em = em;
	}

	public void setCacheServiceList(List<CacheService> cacheServiceList) {
		this.cacheServiceList = cacheServiceList;
	}

	public void setTranslationsCacheTime(Long translationsCacheTime) {
		this.translationsCacheTime = translationsCacheTime;
	}

	@Override
	public String createGroup(GroupDTO group) {
		Group entity = ConverterUtil.groupDTOToGroup(group);
		em.persist(entity);
		return entity.getId();
	}

	@Override
	public void updateGroup(GroupDTO group) {
		Group entity = Group.find(group.getId(), em);
		entity.setTitle(group.getTitle());
		entity.setDescription(group.getDescription());

		// Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).invalidate(Constants.TRANSLATIONS_CACHE_NAMESPACE);
        }
	}

	@Override
	public void deleteGroup(String groupID) {
		em.remove(Group.find(groupID, em));

		// Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).invalidate(Constants.TRANSLATIONS_CACHE_NAMESPACE);
        }
	}

	@Override
	public GroupDTO getGroup(String groupID) {
		return ConverterUtil.groupToGroupDTO(Group.find(groupID, em));
	}

	@Override
	public GroupDTO getGroupByName(String groupName) {
		return ConverterUtil.groupToGroupDTO(Group.findByName(groupName, em));
	}

	@Override
	public Set<GroupDTO> getGroups() {
		Query q = em.createQuery("SELECT g FROM Group g");
		return ConverterUtil.groupToGroupDTOSet(q.getResultList());
	}

	@Override
	public void deleteLanguageTranslations(String groupID, String languageID) {
		Language language = Language.find(languageID, em);

		List<Data> dataList = Data.findByGroupIDAndLocale(groupID, language.getLocale(), em);
		for (Data data : dataList) {
			em.remove(data);
		}

		// Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).delete(Constants.TRANSLATIONS_CACHE_NAMESPACE,
        			getCacheKeyForLocaleAndGroup(language.getLocale(), groupID));
        	cacheServiceList.get(0).delete(Constants.TRANSLATIONS_CACHE_NAMESPACE, language.getLocale());
        }
	}

	@Override
	public void deleteLanguageTranslationsByLocale(String groupID, String locale) {
		List<Data> dataList = Data.findByGroupIDAndLocale(groupID, locale, em);
		for (Data data : dataList) {
			em.remove(data);
		}

		// Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).delete(Constants.TRANSLATIONS_CACHE_NAMESPACE,
        			getCacheKeyForLocaleAndGroup(locale, groupID));
        	cacheServiceList.get(0).delete(Constants.TRANSLATIONS_CACHE_NAMESPACE, locale);
        }
	}

	private String getCacheKeyForLocaleAndGroup(String locale, String groupId) {
		return locale + ":" + groupId;
	}

}
