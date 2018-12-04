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
package com.eurodyn.qlack2.fuse.wiki.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;

import com.eurodyn.qlack2.common.search.PagingParams;
import com.eurodyn.qlack2.fuse.wiki.api.WikiEntryService;
import com.eurodyn.qlack2.fuse.wiki.api.WikiTagService;
import com.eurodyn.qlack2.fuse.wiki.api.dto.WikiEntryDTO;
import com.eurodyn.qlack2.fuse.wiki.api.dto.WikiEntryVersionDTO;
import com.eurodyn.qlack2.fuse.wiki.api.dto.WikiTagDTO;
import com.eurodyn.qlack2.fuse.wiki.api.exception.QAlreadyExists;
import com.eurodyn.qlack2.fuse.wiki.api.exception.QInvalidObject;
import com.eurodyn.qlack2.fuse.wiki.api.exception.QWikiException;
import com.eurodyn.qlack2.fuse.wiki.impl.model.WikEntry;
import com.eurodyn.qlack2.fuse.wiki.impl.model.WikEntryHasTag;
import com.eurodyn.qlack2.fuse.wiki.impl.model.WikEntryVersion;
import com.eurodyn.qlack2.fuse.wiki.impl.model.WikTag;
import com.eurodyn.qlack2.fuse.wiki.impl.model.WikWiki;
import com.eurodyn.qlack2.fuse.wiki.impl.util.ConverterUtil;

/**
 * An OSGI service to manage a Wiki Entry. For
 * details regarding the functionality offered see the respective interfaces.
 *
 * @author European Dynamics SA
 */
public class WikiEntryServiceImpl implements WikiEntryService {
	private static final Logger LOGGER = Logger
			.getLogger(WikiEntryServiceImpl.class.getName());
	private EntityManager em;
	WikiTagService tagService;

	public void setTagService(WikiTagService tagService) {
		this.tagService = tagService;
	}

	public void setEm(EntityManager em) {
		this.em = em;
	}

	public static final String SYSTEM_USER = "System";

	@Override
	public String createEntry(WikiEntryDTO dto) throws QWikiException {
		if (dto == null) {
			throw new QInvalidObject("Wiki Entry object passed is not valid");
		}
		if (dto.getTitle() == null || dto.getNamespace() == null
				|| dto.getWikiId() == null) {
			throw new QInvalidObject("Wiki Entry object passed is not valid");
		}
		if (null == findByName(dto.getTitle())) {
			WikEntry entity = ConverterUtil.convertToWikiEntry(dto, em);
			em.persist(entity);
			if (dto.getWikTags() != null) {
				persistTags(dto.getWikTags(), entity);
			}
			String createdBy = null;
			if (StringUtils.isEmpty(createdBy)) {
				createdBy = dto.getCreatedBy();
			} else {
				createdBy = SYSTEM_USER;
			}
			WikEntryVersion version = new WikEntryVersion(entity, 0, createdBy,
					new Date().getTime(), "Initial version for entry created");
			em.persist(version);

			return entity.getId();
		} else {
			throw new QAlreadyExists("Wiki Entry object already exist");
		}

	}

	/**
	 * Persist the tags
	 *
	 * @param tags
	 * @param entry
	 */
	private void persistTags(Set<WikiTagDTO> tags, WikEntry entry)
			throws QWikiException {
		List<WikTag> wikTags = new ArrayList();
		List<String> tagIds = new ArrayList();
		if (tags != null) {
			for (WikiTagDTO dto : tags) {
				if (dto.getId() != null) {
					tagIds.add(dto.getId());
				} else if (dto.getName() != null) {
					WikTag tag = getTagByName(dto.getName());
					if (tag != null) {
						tagIds.add(tag.getId());
					} else {
						WikiTagDTO wikidto = tagService.createTag(dto);
						if (wikidto != null) {
							tagIds.add(wikidto.getId());
						}
					}
				}
			}
			wikTags = getWikiTags(tagIds);
		}
		for (WikTag wikTag : wikTags) {
			WikEntryHasTag wikEntryHasTag = new WikEntryHasTag();
			wikEntryHasTag.setWikTagId(wikTag);
			wikEntryHasTag.setWikEntryId(entry);
			em.persist(wikEntryHasTag);
		}
	}

	private List<WikTag> getWikiTags(List<String> wikTagIds) {

		List<WikTag> tags = null;
		try {
			//TODO check that the following query works.
			Query q = em.createQuery("select wt from WikTag wt where wt.id in (:id)");
			q.setParameter("id", wikTagIds);
			tags = q.getResultList();
		} catch (NoResultException nre) {
		}

		return tags;
	}

	@Override
	public void editEntry(WikiEntryDTO dto) throws QWikiException {
		if (dto == null) {
			throw new QInvalidObject("Wiki Entry object passed is not valid");
		}
		if (dto.getTitle() == null || dto.getNamespace() == null
				|| dto.getWikiId() == null) {
			throw new QInvalidObject("Wiki Entry object passed is not valid");
		}
		WikWiki wiki = em.find(WikWiki.class, dto.getWikiId());
		WikEntry entity = em.find(WikEntry.class, dto.getId());
		entity.setTitle(dto.getTitle());
		entity.setCreatedBy(dto.getCreatedBy());
		entity.setDtCreated(dto.getDtCreated() != null ? dto.getDtCreated()
				.getTime() : null);
		entity.setDtLastModified(dto.getDtLastModified() != null ? dto
				.getDtLastModified().getTime() : null);
		entity.setLastModifiedBy(dto.getLastModifiedBy());
		entity.setLocked(dto.getLock());
		entity.setLockedBy(dto.getLockedBy());
		entity.setNamespace(dto.getNamespace());
		entity.setPageContent(dto.getPageContent());
		entity.setUrl(dto.getUrl());
		entity.setWikiId(wiki);
		em.merge(entity);
		if (dto.getWikTags() != null && !dto.getWikTags().isEmpty()) {
			// deleteTags(blgPost.getId());
			deleteTags(entity.getId());
			persistTags(dto.getWikTags(), entity);
		} else {
			deleteTags(entity.getId());
		}
		String modifiedBy = null;
		if (dto.getWikVersions() != null && !dto.getWikVersions().isEmpty()) {
			persistVersions(dto.getWikVersions(), entity);
			for (WikiEntryVersionDTO dTO : dto.getWikVersions()) {
				if (dTO.getCreatedBy() != null) {
					modifiedBy = dTO.getCreatedBy();
					break;
				}
			}
		}

	}

	private int deleteTags(String entryId) {
		Query query = em.createQuery("delete from WikEntryHasTag w "
				+ " where w.wikEntryId.id =:entryId");
		query.setParameter("entryId", entryId);
		int entitisDeleted = query.executeUpdate();

		return entitisDeleted;
	}

	@Override
	public void deleteEntry(String entryId) throws QWikiException {
		WikEntry entity = em.find(WikEntry.class, entryId);
		if (entity == null) {
			throw new QInvalidObject("Wiki Entry Does not exist");
		}
		deleteTags(entryId);
		deleteVersions(entryId);

		em.remove(entity);
	}

	private int deleteVersions(String entryId) {
		Query query = em.createQuery("delete from WikEntryVersion v "
				+ " where v.wikEntryId.id =:entryId");
		query.setParameter("entryId", entryId);
		int entitisDeleted = query.executeUpdate();
		// Invalidate cache.
		WikEntry entity = em.find(WikEntry.class, entryId);

		return entitisDeleted;
	}

	@Override
	public List<WikiEntryDTO> searchEntry(String searchTerm,
			PagingParams pagingParams) {
		List<WikiEntryDTO> retVal = new ArrayList<WikiEntryDTO>();
		Query q = em.createQuery("select g from WikEntry g where "
				+ "g.title like :searchTerm order by g.title ");
		q.setParameter("searchTerm", "%" + searchTerm + "%");
		if ((pagingParams != null) && (pagingParams.getCurrentPage() > -1)) {
			q.setFirstResult((pagingParams.getCurrentPage() - 1)
					* pagingParams.getPageSize());
			q.setMaxResults(pagingParams.getPageSize());
		}
		for (Iterator<WikEntry> i = q.getResultList().iterator(); i.hasNext();) {
			WikEntry entry = (WikEntry) i.next();
			retVal.add(ConverterUtil.convertToWikiEntryDTO(entry));
		}
		LOGGER.log(Level.FINEST, "Found {0} wiki entries.", retVal.size());

		return retVal;
	}

	@Override
	public WikiEntryDTO getEntryById(String entryID) {
		WikEntry entity = em.find(WikEntry.class, entryID);
		return (WikiEntryDTO) ConverterUtil.convertToWikiEntryDTO(entity);
	}

	@Override
	public WikiEntryDTO getEntryByNamespace(String namespace) {
		Query query = em
				.createQuery("select object(o) from WikEntry as o where o.namespace=:namespace");
		query.setParameter("namespace", namespace);
		List l = query.getResultList();
		if ((l != null) && (!l.isEmpty())) {
			return (WikiEntryDTO) ConverterUtil
					.convertToWikiEntryDTO((WikEntry) l.get(0));
		}
		return null;
	}

	@Override
	public List<WikiEntryDTO> getLatestEntries(int numberOfrecords) {
		List<WikiEntryDTO> retVal = new ArrayList<WikiEntryDTO>();
		Query q = em
				.createQuery("SELECT g from WikEntry g order by g.dtLastModified DESC");
		if (numberOfrecords > 0) {
			q.setMaxResults(numberOfrecords);
		} else {
			q.setMaxResults(5);
		}
		for (Iterator<WikEntry> i = q.getResultList().iterator(); i.hasNext();) {
			WikEntry entry = (WikEntry) i.next();
			retVal.add(ConverterUtil.convertToWikiEntryDTO(entry));
		}
		LOGGER.log(Level.FINEST, "Found {0} wiki entries.", retVal.size());

		return retVal;
	}

	@Override
	public Set<WikiEntryDTO> getRelatedEntries(String entryID)
			throws QWikiException {
		// get all the tags assigned to this entry
		// for the Tags , get all entries associated with it
		Set<WikiEntryDTO> entries = new HashSet<WikiEntryDTO>();
		WikEntry entity = null;
		try {
			entity = em.getReference(WikEntry.class, entryID);
		} catch (EntityNotFoundException ex) {
			LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			throw new QInvalidObject("Wiki Entry Does not exist");
		}
		Set<WikEntryHasTag> entryHastags = entity.getWikEntryHasTags();
		List<WikTag> wikTags = new ArrayList();
		if (entryHastags != null && !entryHastags.isEmpty()) {
			for (WikEntryHasTag tag : entryHastags) {
				wikTags.add(tag.getWikTagId());
			}
		}
		if (wikTags != null && !wikTags.isEmpty()) {
			for (WikTag tag : wikTags) {
				Set<WikEntryHasTag> tagHasEntries = tag.getWikEntryHasTags();
				for (WikEntryHasTag tagHasWikiEntry : tagHasEntries) {
					if (!tagHasWikiEntry.getWikEntryId().equals(entity)) {
						entries.add(ConverterUtil
								.convertToWikiEntryDTO(tagHasWikiEntry
										.getWikEntryId()));
					}
				}
			}
		}
		return entries;
	}

	@Override
	public Set<WikiEntryDTO> getRelatedEntriesForTag(String entryID,
			String tagName) throws QWikiException {
		Set<WikiEntryDTO> entries = new HashSet<WikiEntryDTO>();
		WikEntry entity = null;
		try {
			entity = em.getReference(WikEntry.class, entryID);
		} catch (EntityNotFoundException enf) {
			throw new QInvalidObject("Wiki Entry Does not exist");
		}
		WikTag tag = getTagByName(tagName);
		if (tag != null) {
			Set<WikEntryHasTag> tagHasEntries = tag.getWikEntryHasTags();
			for (WikEntryHasTag tagHasWikiEntry : tagHasEntries) {
				if (!tagHasWikiEntry.getWikEntryId().equals(entity)) {
					entries.add(ConverterUtil
							.convertToWikiEntryDTO(tagHasWikiEntry
									.getWikEntryId()));
				}
			}
		} else {
			throw new QInvalidObject("Wiki tag Does not exist");
		}
		return entries;
	}

	private WikiEntryDTO findByName(String title) {
		Query query = em
				.createQuery("select object(o) from WikEntry as o where o.title=:entry_title");
		query.setParameter("entry_title", title);
		WikiEntryDTO vo = null;
		WikEntry entry = null;
		List l = query.getResultList();
		if (!l.isEmpty()) {
			entry = (WikEntry) l.get(0);
		}
		if (entry != null) {
			vo = (WikiEntryDTO) ConverterUtil.convertToWikiEntryDTO(entry);
		}

		return vo;
	}

	@Override
	public void unlockEntry(String entryID) {
		WikEntry entity = null;
		entity = em.getReference(WikEntry.class, entryID);
		entity.setLocked(Boolean.FALSE);
		entity.setLockedBy(null);

		em.merge(entity);
	}

	@Override
	public void unlockEntries() {
		Query query = em.createQuery("select object(o) from WikWiki as o");
		boolean invalidated = false;
		List<WikWiki> wikiList = (List<WikWiki>) query.getResultList();
		if (wikiList != null) {
			for (WikWiki wiki : wikiList) {
				Set<WikEntry> entries = wiki.getWikEntries();
				if (entries != null) {
					for (WikEntry entry : entries) {
						unlockEntry(entry.getId());
					}
				}
			}
		}
	}

	@Override
	public boolean isEntryLocked(String entryID) throws QWikiException {
		WikEntry entity = null;
		try {
			entity = em.getReference(WikEntry.class, entryID);
		} catch (EntityNotFoundException enf) {
			throw new QInvalidObject("Wiki Entry Does not exist");
		}
		return entity.getLocked();
	}

	@Override
	public void lockEntry(String entryID, String userId)
			throws QWikiException {
		WikEntry entity = null;
		try {
			entity = em.getReference(WikEntry.class, entryID);
		} catch (EntityNotFoundException enf) {
			throw new QInvalidObject("Wiki Entry Does not exist");
		}
		entity.setLocked(Boolean.TRUE);
		entity.setLockedBy(userId);

		em.merge(entity);
	}

	private WikEntry findByNameEntity(String title) {
		Query query = em
				.createQuery("select object(o) from WikEntry as o where title=:title");
		query.setParameter("title", title);
		WikEntry entry = null;
		List l = query.getResultList();
		if (!l.isEmpty()) {
			entry = (WikEntry) l.get(0);
		}

		return entry;
	}

	private WikTag getTagByName(String tagName) {
		Query query = em
				.createQuery("select object(o) from WikTag as o where o.name=:name");
		query.setParameter("name", tagName);
		WikTag tag = null;
		List l = query.getResultList();
		if (!l.isEmpty()) {
			tag = (WikTag) l.get(0);
		}

		return tag;
	}

	@Override
	public List<WikiEntryDTO> getAllEntriesForWiki(String wikiId,
			PagingParams pagingParams) throws QWikiException {
		List<WikiEntryDTO> retVal = new ArrayList<WikiEntryDTO>();
		Query query = em
				.createQuery("SELECT g from WikEntry g where g.wikiId.id=:wikiId order by g.dtCreated DESC");
		query.setParameter("wikiId", wikiId);
		if (pagingParams != null) {
			query.setFirstResult((pagingParams.getCurrentPage() - 1)
					* pagingParams.getPageSize());
			query.setMaxResults(pagingParams.getPageSize());
		}
		if (query.getResultList() == null) {
			throw new QInvalidObject("Wiki object is invalid");
		}
		for (Iterator<WikEntry> i = query.getResultList().iterator(); i
				.hasNext();) {
			WikEntry entry = (WikEntry) i.next();
			retVal.add(ConverterUtil.convertToWikiEntryDTO(entry));
		}

		return retVal;
	}

	@Override
	public WikiEntryDTO getDefaultEntryForWIki(String wikiId)
			throws QWikiException {
		List<WikiEntryDTO> entries = getAllEntriesForWiki(wikiId, null);
		if (entries != null && !entries.isEmpty()) {
			return entries.get(0);
		} else {
			return null;
		}
	}

	@Override
	public List<WikiEntryDTO> getAllEntriesForTag(String wikiId, String tagId,
			PagingParams pagingParams) throws QWikiException {
		List<WikiEntryDTO> retVal = new ArrayList<WikiEntryDTO>();

		Query query = em
				.createQuery("SELECT p FROM WikEntry p INNER JOIN p.wikEntryHasTags t "
						+ "WHERE t.wikTagId.id= :id and p.wikiId.id=:wikiId order by p.dtCreated DESC");
		query.setParameter("wikiId", wikiId);
		query.setParameter("id", tagId);
		if (pagingParams != null) {
			query.setFirstResult((pagingParams.getCurrentPage() - 1)
					* pagingParams.getPageSize());
			query.setMaxResults(pagingParams.getPageSize());
		}

		if (query.getResultList() == null) {
			throw new QInvalidObject("Wiki object is invalid");
		}
		for (Iterator<WikEntry> i = query.getResultList().iterator(); i
				.hasNext();) {
			WikEntry entry = (WikEntry) i.next();
			retVal.add(ConverterUtil.convertToWikiEntryDTO(entry));
		}

		return retVal;
	}

	private void persistVersions(Set<WikiEntryVersionDTO> wikVersions,
			WikEntry entity) {
		if (wikVersions != null && !wikVersions.isEmpty()) {
			for (WikiEntryVersionDTO versionDTO : wikVersions) {
				if (versionDTO.getId() == null
						|| getVersion(versionDTO.getId()) == null) {
					WikEntryVersion version = new WikEntryVersion();
					version.setComment(versionDTO.getComment());
					version.setCreatedBy(versionDTO.getCreatedBy());
					version.setDtCreated(versionDTO.getDtCreated().getTime());
					version.setEntryVersion(getMaxEntryVersion(entity.getId()) + 1);
					version.setWikEntryId(entity);
					em.persist(version);
				}
			}
		}
	}

	private WikEntryVersion getVersion(String id) {
		WikEntryVersion version = null;
		try {
			version = em.getReference(WikEntryVersion.class, id);
		} catch (EntityNotFoundException enf) {
			LOGGER.log(Level.SEVERE, "Wiki Entry version Does not exist");
		}
		return version;
	}

	private int getMaxEntryVersion(String entryId) {
		Integer max = new Integer(0);
		Query maxVersionQuery = em
				.createQuery("SELECT MAX(v.entryVersion) FROM WikEntryVersion v WHERE v.wikEntryId.id = :entryId");
		maxVersionQuery.setParameter("entryId", entryId);
		max = (Integer) maxVersionQuery.getSingleResult();
		if (max == null) {
			max = new Integer(0);
		}
		return max.intValue();
	}

	@Override
	public List<WikiEntryVersionDTO> getAllVersionsForWikiEntry(String entryId,
			PagingParams pagingParams) throws QWikiException {
		List<WikiEntryVersionDTO> entryVersions = new ArrayList<WikiEntryVersionDTO>();
		WikEntry entity = em.find(WikEntry.class, entryId);
		if (entity == null) {
			throw new QInvalidObject("Wiki Entry Does not exist");
		}
		Query query = em
				.createQuery("SELECT g from WikEntryVersion g where g.wikEntryId.id=:entryId order by g.dtCreated DESC");
		query.setParameter("entryId", entryId);
		if (pagingParams != null) {
			query.setFirstResult((pagingParams.getCurrentPage() - 1)
					* pagingParams.getPageSize());
			query.setMaxResults(pagingParams.getPageSize());
		}
		for (Iterator<WikEntryVersion> i = query.getResultList().iterator(); i
				.hasNext();) {
			WikEntryVersion entryVersion = (WikEntryVersion) i.next();
			entryVersions.add(ConverterUtil
					.convertToWikiVersionDTO(entryVersion));
		}

		return entryVersions;
	}

	@Override
	public List<WikiEntryDTO> listAllWikiPages(String wikiId)
			throws QWikiException {
		List<WikiEntryDTO> retVal = new ArrayList<WikiEntryDTO>();

		Query query = em
				.createQuery("SELECT g from WikEntry g where g.wikiId.id=:wikiId order by g.namespace ASC");
		query.setParameter("wikiId", wikiId);

		if (query.getResultList() == null) {
			throw new QInvalidObject("Wiki object is invalid");
		}
		for (Iterator<WikEntry> i = query.getResultList().iterator(); i
				.hasNext();) {
			WikEntry entry = (WikEntry) i.next();
			retVal.add(ConverterUtil.convertToWikiEntryDTO(entry));
		}

		return retVal;
	}

}
