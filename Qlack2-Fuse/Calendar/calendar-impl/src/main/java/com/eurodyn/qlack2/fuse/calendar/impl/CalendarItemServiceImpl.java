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
package com.eurodyn.qlack2.fuse.calendar.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.eurodyn.qlack2.fuse.calendar.api.CalendarItemService;
import com.eurodyn.qlack2.fuse.calendar.api.dto.CalendarItemDTO;
import com.eurodyn.qlack2.fuse.calendar.api.dto.ParticipantDTO;
import com.eurodyn.qlack2.fuse.calendar.api.dto.SupportingObjectDTO;
import com.eurodyn.qlack2.fuse.calendar.api.exception.QCalendarException;
import com.eurodyn.qlack2.fuse.calendar.api.exception.QParticipantNotExists;
import com.eurodyn.qlack2.fuse.calendar.api.exception.QSupportingObjectNotExists;
import com.eurodyn.qlack2.fuse.calendar.impl.model.CalCalendar;
import com.eurodyn.qlack2.fuse.calendar.impl.model.CalItem;
import com.eurodyn.qlack2.fuse.calendar.impl.model.CalParticipant;
import com.eurodyn.qlack2.fuse.calendar.impl.model.CalSupportingObject;
import com.eurodyn.qlack2.fuse.calendar.impl.util.ConverterUtil;

/**
 *
 * @author European Dynamics SA
 */
public class CalendarItemServiceImpl implements CalendarItemService {
	private static final Logger LOGGER = Logger
			.getLogger(CalendarItemServiceImpl.class.getName());
	private EntityManager em;

	public void setEm(EntityManager em) {
		this.em = em;
	}

	private CalItem retrieveItem(String itemId) throws QCalendarException {
		CalItem item = em.find(CalItem.class, itemId);
		if (item == null) {
			throw new QSupportingObjectNotExists("Calendar item with id "
					+ itemId + " does not exist.");
		}
		return item;
	}

	private CalSupportingObject retrieveSupportingObject(String objectId)
			throws QCalendarException {
		CalSupportingObject object = em.find(CalSupportingObject.class,
				objectId);
		if (object == null) {
			throw new QSupportingObjectNotExists(
					"Calendar supporting object with id " + objectId
							+ " does not exist.");
		}
		return object;
	}

	private CalParticipant retrieveParticipant(String participantId)
			throws QCalendarException {
		CalParticipant participant = em.find(CalParticipant.class,
				participantId);
		if (participant == null) {
			throw new QParticipantNotExists("Calendar participant with id "
					+ participantId + " does not exist.");
		}
		return participant;
	}

	private String createSupportingObject(SupportingObjectDTO object,
			CalItem itemEntity) {
		object.setCreatedOn(new Date());
		object.setLastModifiedOn(new Date());
		CalSupportingObject objectEntity = ConverterUtil
				.convertToSupportingObjectEntity(object);
		objectEntity.setItemId(itemEntity);
		objectEntity.setSupportingObjectCategoryId(object.getCategoryId());
		em.persist(objectEntity);

		return objectEntity.getId();
	}

	private List<CalendarItemDTO> getUserCalendarItems(String userId,
			String calendarId, String[] categoryIds, Date startDate,
			Date endDate) {
		String queryString = "SELECT DISTINCT i FROM CalParticipant p LEFT OUTER JOIN p.itemId i "
				+ "WHERE (i.createdBy = :userId OR p.participantId = :userId)";
		if (calendarId != null) {
			queryString = queryString
					.concat(" AND i.calendarId.id = :calendarId");
		}
		if (categoryIds != null) {
			queryString = queryString
					.concat(" AND i.categoryId in (:categories)");
		}
		if (startDate != null) {
			queryString = queryString.concat(" AND i.startTime >= :startDate");
		}
		if (endDate != null) {
			queryString = queryString.concat(" AND i.startTime <= :endDate");
		}

		Query query = em.createQuery(queryString);
		query.setParameter("userId", userId);
		if (calendarId != null) {
			query.setParameter("calendarId", calendarId);
		}
		if (categoryIds != null) {
			// TODO Check that this conversion works.
			query.setParameter("categories", Arrays.asList(categoryIds));
			// ArraysHelper.arrayToList(categoryIds));
		}
		if (startDate != null) {
			query.setParameter("startDate", startDate.getTime());
		}
		if (endDate != null) {
			query.setParameter("endDate", endDate.getTime());
		}

		List<CalItem> itemEntities = query.getResultList();
		List<CalendarItemDTO> calDTOs = ConverterUtil
				.convertToItemDTOList(itemEntities);

		return calDTOs;
	}

	private List<String> getDistinctParticipantIds(String itemId)
			throws QCalendarException {
		List<String> result = new ArrayList<String>();

		result.add(retrieveItem(itemId).getCreatedBy());
		Query q = em
				.createQuery("SELECT DISTINCT p.participantId FROM CalParticipant p LEFT OUTER JOIN p.itemId i WHERE i.id = :id");
		q.setParameter("id", itemId);
		result.addAll(q.getResultList());

		return result;
	}

	@Override
	public CalendarItemDTO createItem(CalendarItemDTO item)
			throws QCalendarException {
		LOGGER.log(Level.FINEST, "Creating new calendar item");

		item.setCreatedOn(new Date());
		item.setLastModifiedOn(new Date());

		CalItem itemEntity = ConverterUtil.convertToItemEntity(item);
		itemEntity.setCalendarId(em.find(CalCalendar.class,
				item.getCalendarId()));
		itemEntity.setCategoryId(item.getCategoryId());

		em.persist(itemEntity);
		item.setId(itemEntity.getId());

		if (item.getParticipants() != null) {
			for (ParticipantDTO participant : item.getParticipants()) {
				CalParticipant participantEntity = ConverterUtil
						.convertToParticipantEntity(participant);
				participantEntity.setItemId(itemEntity);
				em.persist(participantEntity);
			}
		}
		if (item.getObjects() != null) {
			for (SupportingObjectDTO object : item.getObjects()) {
				createSupportingObject(object, itemEntity);
			}
		}

		return item;
	}

	@Override
	public void updateItem(CalendarItemDTO item) throws QCalendarException {
		LOGGER.log(Level.FINEST, "Updating calendar item with id {0}",
				item.getId());

		CalItem itemEntity = retrieveItem(item.getId());
		if (item.getCategoryId() != null) {
			itemEntity.setCategoryId(item.getCategoryId());
		}
		if (item.getName() != null) {
			itemEntity.setName(item.getName());
		}
		if (item.getDescription() != null) {
			itemEntity.setDescription(item.getDescription());
		}
		if (item.getLocation() != null) {
			itemEntity.setLocation(item.getLocation());
		}
		if (item.getContactId() != null) {
			itemEntity.setContactId(item.getContactId());
		}
		if (item.getStartTime() != null) {
			itemEntity.setStartTime(item.getStartTime().getTime());
		}
		if (item.getEndTime() != null) {
			itemEntity.setEndTime(item.getEndTime().getTime());
		}
		if (item.getAllDay() != null) {
			itemEntity.setAllDay(item.getAllDay());
		}
		if (item.getLastModifiedBy() != null) {
			itemEntity.setLastModifiedBy(item.getLastModifiedBy());
		}
		if (item.getLastModifiedOn() != null) {
			itemEntity.setLastModifiedOn(item.getLastModifiedOn().getTime());
		}

	}

	@Override
	public void deleteItem(CalendarItemDTO item) throws QCalendarException {
		LOGGER.log(Level.FINEST, "Deleting calendar item with id {0}",
				item.getId());

		CalItem itemEntity = retrieveItem(item.getId());
		String itemName = itemEntity.getName();
		String categoryId = itemEntity.getCategoryId();
		String calendarId = itemEntity.getCalendarId().getId();
		List<String> distinctParticipantIds = getDistinctParticipantIds(item
				.getId());

		em.remove(itemEntity);
	}

	@Override
	public CalendarItemDTO getItem(String itemId) {
		LOGGER.log(Level.FINEST, "Retrieving calendar item with id {0}", itemId);

		CalItem itemEntity = em.find(CalItem.class, itemId);
		return ConverterUtil.convertToItemDTO(itemEntity);
	}

	@Override
	public List<CalendarItemDTO> getCalendarItems(String calendarId,
			String[] categoryIds, Date startDate, Date endDate) {
		LOGGER.log(
				Level.FINEST,
				"Retrieving items of calendar with id {0}, startDate = {1}, endDate = {2}",
				new String[] { calendarId,
						(startDate != null ? startDate.toString() : "null"),
						(endDate != null ? endDate.toString() : "null") });

		String queryString = "SELECT i FROM CalItem i WHERE i.calendarId.id = :calendarId";
		if (categoryIds != null) {
			queryString = queryString
					.concat(" AND i.categoryId in (:categories)");
		}
		if (startDate != null) {
			queryString = queryString.concat(" AND i.startTime >= :startDate");
		}
		if (endDate != null) {
			queryString = queryString.concat(" AND i.startTime <= :endDate");
		}

		Query query = em.createQuery(queryString);
		query.setParameter("calendarId", calendarId);
		if (categoryIds != null) {
			// TODO check this conversion works.
			query.setParameter("categories", Arrays.asList(categoryIds));
			// ArraysHelper.arrayToList(categoryIds));
		}
		if (startDate != null) {
			query.setParameter("startDate", startDate.getTime());
		}
		if (endDate != null) {
			query.setParameter("endDate", endDate.getTime());
		}

		List<CalItem> itemEntities = query.getResultList();
		List<CalendarItemDTO> itemDTOs = ConverterUtil
				.convertToItemDTOList(itemEntities);

		return itemDTOs;
	}

	@Override
	public List<CalendarItemDTO> getItemsForUser(String userId,
			String[] categoryIds, Date startDate, Date endDate) {
		LOGGER.log(
				Level.FINEST,
				"Retrieving calendar items for user with id {0}, startDate = {1}, endDate = {2}",
				new String[] { userId,
						(startDate != null ? startDate.toString() : "null"),
						(endDate != null ? endDate.toString() : "null") });

		return getUserCalendarItems(userId, null, categoryIds, startDate,
				endDate);
	}

	@Override
	public List<CalendarItemDTO> getItemsForUser(String userId,
			String calendarId, String[] categoryIds, Date startDate,
			Date endDate) {
		LOGGER.log(
				Level.FINEST,
				"Retrieving items of calendar with id {0} for user with id {1}, startDate = {2}, endDate = {3}",
				new String[] { calendarId, userId,
						(startDate != null ? startDate.toString() : "null"),
						(endDate != null ? endDate.toString() : "null") });

		return getUserCalendarItems(userId, calendarId, categoryIds, startDate,
				endDate);
	}

	@Override
	public SupportingObjectDTO addItemSupportingObject(
			SupportingObjectDTO object, boolean updateItem)
			throws QCalendarException {
		LOGGER.log(Level.FINEST,
				"Adding supporting object to item with id {0}",
				object.getItemId());

		CalItem itemEntity = retrieveItem(object.getItemId());
		object.setId(createSupportingObject(object, itemEntity));
		if (updateItem) {
			CalendarItemDTO item = new CalendarItemDTO();
			item.setId(object.getItemId());
			item.setSrcUserId(object.getSrcUserId());
			item.setLastModifiedBy(object.getSrcUserId());
			item.setLastModifiedOn(new Date());
			updateItem(item);
		}

		return object;
	}

	@Override
	public void updateItemSupportingObject(SupportingObjectDTO object,
			boolean updateItem) throws QCalendarException {
		LOGGER.log(Level.FINEST, "Updating supporting object with id {0}",
				object.getId());

		CalSupportingObject objectEntity = retrieveSupportingObject(object
				.getId());

		objectEntity.setObjectId(object.getObjectId());
		objectEntity.setLink(object.getLink());
		objectEntity.setFilename(object.getFilename());
		objectEntity.setMimetype(object.getMimetype());
		objectEntity.setObjectData(object.getObjectData());
		objectEntity.setLastModifiedBy(object.getLastModifiedBy());
		objectEntity.setLastModifiedOn(System.currentTimeMillis());

		if (updateItem) {
			CalendarItemDTO item = new CalendarItemDTO();
			item.setId(objectEntity.getItemId().getId());
			item.setSrcUserId(object.getSrcUserId());
			item.setLastModifiedBy(object.getSrcUserId());
			item.setLastModifiedOn(new Date());
			updateItem(item);
		}
	}

	@Override
	public void removeItemSupportingObject(SupportingObjectDTO object,
			boolean updateItem) throws QCalendarException {
		LOGGER.log(Level.FINEST, "Removing supporting object with id {0}",
				object.getId());

		CalSupportingObject objectEntity = retrieveSupportingObject(object
				.getId());

		if (updateItem) {
			CalendarItemDTO item = new CalendarItemDTO();
			item.setId(objectEntity.getItemId().getId());
			item.setSrcUserId(object.getSrcUserId());
			item.setLastModifiedBy(object.getSrcUserId());
			item.setLastModifiedOn(new Date());
			updateItem(item);
		}

		em.remove(objectEntity);
	}

	@Override
	public SupportingObjectDTO getItemSupportingObject(String objectId)
			throws QCalendarException {
		LOGGER.log(Level.FINEST, "Retrieving supporting object with id {0}",
				objectId);

		CalSupportingObject object = retrieveSupportingObject(objectId);
		return ConverterUtil.convertToSupportingObjectDTO(object);
	}

	@Override
	public List<SupportingObjectDTO> getItemSupportingObjects(String itemId,
			String[] categoryIds) throws QCalendarException {
		LOGGER.log(Level.FINEST,
				"Retrieving supporting objects of item with id {0}", itemId);

		String queryString = "SELECT o FROM CalSupportingObject o WHERE o.itemId.id = :itemId";
		if (categoryIds != null) {
			queryString = queryString
					.concat(" AND o.supportingObjectCategoryId IN (:categories)");
		}
		Query q = em.createQuery(queryString);
		q.setParameter("itemId", itemId);
		if (categoryIds != null) {
			// TODO Check this conversion works.
			q.setParameter("categories", Arrays.asList(categoryIds));
			// ArraysHelper.arrayToList(categoryIds));
		}
		List<CalSupportingObject> queryResult = q.getResultList();
		List<SupportingObjectDTO> itemDTOs = ConverterUtil
				.convertToSupportingObjectDTOList(queryResult);

		return itemDTOs;
	}

	@Override
	public ParticipantDTO addItemParticipant(ParticipantDTO participant,
			boolean updateItem) throws QCalendarException {
		LOGGER.log(Level.FINEST, "Adding participant to item with id {0}",
				participant.getItemId());

		CalItem itemEntity = retrieveItem(participant.getItemId());
		CalParticipant participantEntity = ConverterUtil
				.convertToParticipantEntity(participant);
		participantEntity.setItemId(itemEntity);
		em.persist(participantEntity);
		participant.setId(participantEntity.getId());
		if (updateItem) {
			itemEntity.setLastModifiedBy(participant.getSrcUserId());
			itemEntity.setLastModifiedOn(System.currentTimeMillis());
		}

		return participant;
	}

	@Override
	public void updateItemParticipant(ParticipantDTO participant,
			boolean updateItem) throws QCalendarException {
		LOGGER.log(Level.FINEST, "Updating participant with id {0}",
				participant.getId());

		CalParticipant participantEntity = retrieveParticipant(participant
				.getId());
		CalItem itemEntity = participantEntity.getItemId();

		if (participant.getParticipantId() != null) {
			participantEntity.setParticipantId(participant.getParticipantId());
		}

		if (updateItem) {
			itemEntity.setLastModifiedBy(participant.getSrcUserId());
			itemEntity.setLastModifiedOn(System.currentTimeMillis());
		}
	}

	@Override
	public void removeItemParticipant(ParticipantDTO participant,
			boolean updateItem) throws QCalendarException {
		LOGGER.log(Level.FINEST, "Removing participant with id {0}",
				participant.getId());

		CalParticipant participantEntity = retrieveParticipant(participant
				.getId());
		CalItem itemEntity = participantEntity.getItemId();
		if (updateItem) {
			itemEntity.setLastModifiedBy(participant.getSrcUserId());
			itemEntity.setLastModifiedOn(System.currentTimeMillis());
		}
		em.remove(participantEntity);
	}

	@Override
	public ParticipantDTO getItemParticipant(String participantId)
			throws QCalendarException {
		LOGGER.log(Level.FINEST, "Retrieving participant with id {0}",
				participantId);

		CalParticipant participant = retrieveParticipant(participantId);
		return ConverterUtil.convertToParticipantDTO(participant);
	}

	@Override
	public List<ParticipantDTO> getItemParticipants(String itemId)
			throws QCalendarException {
		LOGGER.log(Level.FINEST, "Retrieving participants of item with id {0}",
				itemId);
		Query q = em
				.createQuery("SELECT p FROM CalParticipant p WHERE p.itemId.id = :itemId");
		q.setParameter("itemId", itemId);
		List<CalParticipant> queryResult = q.getResultList();
		List<ParticipantDTO> itemDTOs = ConverterUtil
				.convertToParticipantDTOList(queryResult);

		return itemDTOs;
	}

	@Override
	public List<ParticipantDTO> getParticipantsForUser(String userId) {
		LOGGER.log(Level.FINEST, "Retrieving participants with user id {0}",
				userId);

		Query q = em
				.createQuery("SELECT p FROM CalParticipant p WHERE p.participantId = :participantId");
		q.setParameter("participantId", userId);
		List<CalParticipant> queryResult = q.getResultList();
		return ConverterUtil.convertToParticipantDTOList(queryResult);
	}

	@Override
	public List<ParticipantDTO> getParticipantsForUser(String userId,
			String itemId) {
		LOGGER.log(
				Level.FINEST,
				"Retrieving participants with user id {0} for item with id {1}",
				new String[] { userId, itemId });

		Query q = em
				.createQuery("SELECT p FROM CalParticipant p WHERE p.participantId = :participantId AND p.itemId.id = :itemId");
		q.setParameter("participantId", userId);
		q.setParameter("itemId", itemId);
		List<CalParticipant> queryResult = q.getResultList();
		return ConverterUtil.convertToParticipantDTOList(queryResult);
	}

	@Override
	public short getParticipantStatus(String participantId)
			throws QCalendarException {
		LOGGER.log(Level.FINEST,
				"Retrieving status of participant with id {0}", participantId);

		CalParticipant participantEntity = retrieveParticipant(participantId);
		return participantEntity.getStatus();
	}

	@Override
	public boolean updateParticipantStatus(String participantId, short status)
			throws QCalendarException {
		LOGGER.log(Level.FINEST,
				"Updating status of participant with id {0}, new status = {1}",
				new String[] { participantId, String.valueOf(status) });

		CalParticipant participantEntity = retrieveParticipant(participantId);

		if (status == participantEntity.getStatus()) {
			return false;
		}

		participantEntity.setStatus(status);

		return true;
	}

}
