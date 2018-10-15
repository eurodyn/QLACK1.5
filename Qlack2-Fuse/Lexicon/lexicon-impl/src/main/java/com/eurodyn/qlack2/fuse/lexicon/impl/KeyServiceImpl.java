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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.joda.time.DateTime;

import com.eurodyn.qlack2.fuse.caching.client.api.CacheService;
import com.eurodyn.qlack2.fuse.lexicon.api.KeyService;
import com.eurodyn.qlack2.fuse.lexicon.api.criteria.KeySearchCriteria;
import com.eurodyn.qlack2.fuse.lexicon.api.dto.KeyDTO;
import com.eurodyn.qlack2.fuse.lexicon.impl.model.Data;
import com.eurodyn.qlack2.fuse.lexicon.impl.model.Group;
import com.eurodyn.qlack2.fuse.lexicon.impl.model.Key;
import com.eurodyn.qlack2.fuse.lexicon.impl.model.Language;
import com.eurodyn.qlack2.fuse.lexicon.impl.util.Constants;
import com.eurodyn.qlack2.fuse.lexicon.impl.util.ConverterUtil;

public class KeyServiceImpl implements KeyService {
	private static final Logger LOGGER = Logger.getLogger(KeyServiceImpl.class.getName());
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
	public String createKey(KeyDTO key, boolean createDefaultTranslations) {
		// Create the new key.
        Key entity = new Key();
        entity.setName(key.getName());
        if (key.getGroupId() != null) {
        	entity.setGroup(Group.find(key.getGroupId(), em));
        }
        em.persist(entity);

        if (createDefaultTranslations) {
        	List<Language> languages = Language.getAllLanguages(em);
        	for (Language language : languages) {
            	String translation = null;
            	if (key.getTranslations() != null) {
            		translation = key.getTranslations().get(language.getId());
            	}
            	if (translation == null) {
            		translation =  key.getName();
            	}
    			updateTranslation(entity.getId(), language.getId(), translation);
            }
        } else if (key.getTranslations() != null) {
        	for (String languageId : key.getTranslations().keySet()) {
        		updateTranslation(entity.getId(), languageId, key.getTranslations().get(languageId));
        	}
        }

        // Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).invalidate(Constants.TRANSLATIONS_CACHE_NAMESPACE);
        }

        return entity.getId();
	}

	@Override
	public void deleteKey(String keyID) {
		List<String> keyIDs = new ArrayList<>(1);
		keyIDs.add(keyID);
		// This call also takes care of invalidating the cache.
		deleteKeys(keyIDs);
	}

	@Override
	public void deleteKeys(Collection<String> keyIDs) {
		for (String keyID : keyIDs) {
			em.remove(Key.find(keyID, em));
		}

		// Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).invalidate(Constants.TRANSLATIONS_CACHE_NAMESPACE);
        }
	}

	@Override
	public void renameKey(String keyID, String newName) {
		Key key = Key.find(keyID, em);
		key.setName(newName);

		// Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).invalidate(Constants.TRANSLATIONS_CACHE_NAMESPACE);
        }
	}

	@Override
	public void moveKey(String keyID, String newGroupId) {
		List<String> keyIDs = new ArrayList<>(1);
		keyIDs.add(keyID);
		// This call also takes care of invalidating the cache.
		moveKeys(keyIDs, newGroupId);
	}

	@Override
	public void moveKeys(Collection<String> keyIDs, String newGroupId) {
		for (String keyID : keyIDs) {
			Key key = Key.find(keyID, em);
			key.setGroup(Group.find(newGroupId, em));
		}

		// Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).invalidate(Constants.TRANSLATIONS_CACHE_NAMESPACE);
        }
	}

	@Override
	public KeyDTO getKeyByID(String keyID, boolean includeTranslations) {
		return ConverterUtil.keyToKeyDTO(em.find(Key.class, keyID), includeTranslations);
	}

	@Override
	public KeyDTO getKeyByName(String keyName, String groupId, boolean includeTranslations) {
		return ConverterUtil.keyToKeyDTO(Key.findByName(keyName, groupId, em), includeTranslations);
	}

	@Override
	public List<KeyDTO> findKeys(KeySearchCriteria criteria,
			boolean includeTranslations) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Key> cq = cb.createQuery(Key.class);
		Root<Key> root = cq.from(Key.class);

		// Add query criteria
		if (criteria.getKeyName() != null) {
			Predicate pr = cb.like(root.<String>get("name"), criteria.getKeyName());
			cq = addPredicate(cq, cb, pr);
		}
		if (criteria.getGroupId() != null) {
			Predicate pr = cb.equal(root.get("group").get("id"), criteria.getGroupId());
			cq = addPredicate(cq, cb, pr);
		}

		// Set ordering
		Order order = null;
		if (criteria.isAscending()) {
			order = cb.asc(root.get("name"));
		} else {
			order = cb.desc(root.get("name"));
		}
		cq = cq.orderBy(order);

		TypedQuery<Key> query = em.createQuery(cq);

		// Apply pagination
		if (criteria.getPaging() != null && criteria.getPaging().getCurrentPage() > -1) {
			query.setFirstResult((criteria.getPaging().getCurrentPage() - 1) * criteria.getPaging().getPageSize());
			query.setMaxResults(criteria.getPaging().getPageSize());
		}

		return ConverterUtil.keyToKeyDTOList(query.getResultList(), includeTranslations);
	}


	private <T> CriteriaQuery<T> addPredicate(CriteriaQuery<T> query,
			CriteriaBuilder cb, Predicate pr) {
		CriteriaQuery<T> cq = query;
		if (cq.getRestriction() != null) {
			cq = cq.where(cb.and(cq.getRestriction(), pr));
		} else {
			cq = cq.where(pr);
		}
		return cq;
	}

	private String getCacheKeyForLocaleAndGroup(String locale, String groupId) {
		return locale + ":" + groupId;
	}

	@Override
	public void updateTranslation(String keyID, String languageID, String value) {
		Key key = Key.find(keyID, em);
		Data data = Data.findByKeyAndLanguageId(keyID, languageID, em);
		if (data == null) {
			data = new Data();
			data.setKey(key);
			data.setLanguage(Language.find(languageID, em));
		}
		data.setValue(value);
		em.merge(data);

		// Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).delete(Constants.TRANSLATIONS_CACHE_NAMESPACE,
        			getCacheKeyForLocaleAndGroup(data.getLanguage().getLocale(), data.getKey().getGroup().getId()));
        	cacheServiceList.get(0).delete(Constants.TRANSLATIONS_CACHE_NAMESPACE, data.getLanguage().getLocale());
        }
	}

	@Override
	public void updateTranslationByKeyName(String keyName, String groupID, String languageID, String value) {
		Data data = Data.findByKeyNameAndLanguageId(keyName, languageID, em);
		if (data == null) {
			data = new Data();
			data.setKey(Key.findByName(keyName, groupID, em));
			data.setLanguage(Language.find(languageID, em));
		}
		data.setValue(value);
		em.merge(data);

		// Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).delete(Constants.TRANSLATIONS_CACHE_NAMESPACE,
        			getCacheKeyForLocaleAndGroup(data.getLanguage().getLocale(),groupID));
        	cacheServiceList.get(0).delete(Constants.TRANSLATIONS_CACHE_NAMESPACE, data.getLanguage().getLocale());
        }
	}

	@Override
	public void updateTranslationByLocale(String keyID, String locale, String value) {
		Data data = Data.findByKeyIdAndLocale(keyID, locale, em);
		if (data == null) {
			data = new Data();
			data.setKey(Key.find(keyID, em));
			data.setLanguage(Language.findByLocale(locale, em));
		}
		data.setValue(value);
		em.merge(data);

		// Invalidate cache
        if (cacheServiceList.size() > 0) {
        	cacheServiceList.get(0).delete(Constants.TRANSLATIONS_CACHE_NAMESPACE,
        			getCacheKeyForLocaleAndGroup(locale, data.getKey().getGroup().getId()));
        	cacheServiceList.get(0).delete(Constants.TRANSLATIONS_CACHE_NAMESPACE, locale);
        }
	}

	@Override
	public void updateTranslationsForKey(String keyID,
			Map<String, String> translations) {
		for (String languageId : translations.keySet()) {
			// This call also takes care of invalidating the cache.
			updateTranslation(keyID, languageId, translations.get(languageId));
		}
	}

	@Override
	public void updateTranslationsForKeyByLocale(String keyID, Map<String, String> translations) {
		for (String locale : translations.keySet()) {
			// This call also takes care of invalidating the cache.
			updateTranslationByLocale(keyID, locale, translations.get(locale));
		}
	}

	@Override
	public void updateTranslationsForLanguage(String languageID,
			Map<String, String> translations) {
		for (String keyId : translations.keySet()) {
			// This call also takes care of invalidating the cache.
			updateTranslation(keyId, languageID, translations.get(keyId));
		}
	}

	@Override
	public void updateTranslationsForLanguageByKeyName(String languageID, String groupID, Map<String, String> translations) {
		for (String keyName : translations.keySet()) {
			// This call also takes care of invalidating the cache.
			updateTranslationByKeyName(keyName, groupID, languageID, translations.get(keyName));
		}
	}

	@Override
	public String getTranslation(String keyName, String locale) {
		Data data = Data.findByKeyNameAndLocale(keyName, locale, em);
		if (data == null) {
			return null;
		}
		return data.getValue();
	}

	@Override
	public Map<String, String> getTranslationsForKeyName(String keyName, String groupID) {
		Key key = Key.findByName(keyName, groupID, em);
		Map<String, String> translations = new HashMap<>();
		for (Data data : key.getData()) {
			translations.put(data.getLanguage().getLocale(), data.getValue());
		}
		return translations;
	}

	@Override
	public Map<String, String> getTranslationsForLocale(String locale) {
		Language language = Language.findByLocale(locale, em);
		Map<String, String> translations = new HashMap<>();
		for (Data data : language.getData()) {
			translations.put(data.getKey().getName(), data.getValue());
		}

		// Cache results if cache is available
		if (cacheServiceList.size() > 0 && translationsCacheTime != null && translationsCacheTime > 0) {
			cacheServiceList.get(0).set(Constants.TRANSLATIONS_CACHE_NAMESPACE, locale,
					translations, DateTime.now().getMillis() + translationsCacheTime);
		}

		return translations;
	}

	@Override
	public Map<String, String> getTranslationsForGroupAndLocale(String groupId,
			String locale) {
		List<Data> dataList = Data.findByGroupIDAndLocale(groupId, locale, em);
		Map<String, String> translations = new HashMap<>();
		for (Data data : dataList) {
			translations.put(data.getKey().getName(), data.getValue());
		}

		// Cache results if cache is available
		if (cacheServiceList.size() > 0 && translationsCacheTime != null && translationsCacheTime > 0) {
			cacheServiceList.get(0).set(Constants.TRANSLATIONS_CACHE_NAMESPACE, getCacheKeyForLocaleAndGroup(locale, groupId),
					translations, DateTime.now().getMillis() + translationsCacheTime);
		}

		return translations;
	}

}
