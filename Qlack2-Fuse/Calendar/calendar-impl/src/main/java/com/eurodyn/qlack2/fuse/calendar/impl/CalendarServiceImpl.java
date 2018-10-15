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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.eurodyn.qlack2.fuse.calendar.api.CalendarService;
import com.eurodyn.qlack2.fuse.calendar.api.dto.CalendarDTO;
import com.eurodyn.qlack2.fuse.calendar.api.exception.QCalendarException;
import com.eurodyn.qlack2.fuse.calendar.api.exception.QCalendarNotExists;
import com.eurodyn.qlack2.fuse.calendar.impl.model.CalCalendar;
import com.eurodyn.qlack2.fuse.calendar.impl.util.ConverterUtil;

/**
 *
 * @author European Dynamics SA
 */
public class CalendarServiceImpl implements CalendarService {
	private static final Logger LOGGER = Logger
			.getLogger(CalendarServiceImpl.class.getName());
	private EntityManager em;
	public void setEm(EntityManager em) {
		this.em = em;
	}

	private CalCalendar retrieveCalendar(String calendarId)
			throws QCalendarException {
		CalCalendar calendar = em.find(CalCalendar.class, calendarId);
		if (calendar == null) {
			throw new QCalendarNotExists("Calendar with id " + calendarId
					+ " does not exist.");
		}
		return calendar;
	}

	@Override
	public CalendarDTO createCalendar(CalendarDTO calendar) {
		LOGGER.log(Level.FINEST, "Creating new calendar");

		CalCalendar calendarEntity = ConverterUtil
				.convertToCalendarEntity(calendar);
		em.persist(calendarEntity);
		calendar.setId(calendarEntity.getId());

		return calendar;
	}

	@Override
	public void updateCalendar(CalendarDTO calendar) throws QCalendarException {
		LOGGER.log(Level.FINEST, "Updating calendar with id {0}",
				calendar.getId());

		CalCalendar calendarEntity = retrieveCalendar(calendar.getId());
		if (calendar.getOwnerId() != null) {
			calendarEntity.setOwnerId(calendar.getOwnerId());
		}
		if (calendar.getLastModifiedBy() != null) {
			calendarEntity.setLastModifiedBy(calendar.getLastModifiedBy());
		}
		if (calendar.getLastModifiedOn() != null) {
			calendarEntity.setLastModifiedOn(calendar.getLastModifiedOn()
					.getTime());
		}
	}

	@Override
	public void deleteCalendar(CalendarDTO calendar) throws QCalendarException {
		LOGGER.log(Level.FINEST, "Deleting calendar with id {0}",
				calendar.getId());
		em.remove(retrieveCalendar(calendar.getId()));
	}

	@Override
	public CalendarDTO getCalendar(String calendarId) {
		LOGGER.log(Level.FINEST, "Retrieving calendar with id {0}", calendarId);

		CalCalendar calendarEntity = em.find(CalCalendar.class, calendarId);
		return ConverterUtil.convertToCalendarDTO(calendarEntity);
	}

	@Override
	public List<CalendarDTO> getCalendars(boolean includeDisabled) {
		LOGGER.log(Level.FINEST,
				"Retrieving all calendars, includeDisabled = {0}",
				String.valueOf(includeDisabled));

		String queryString = "SELECT c FROM CalCalendar c";
		if (!includeDisabled) {
			queryString = queryString.concat(" WHERE c.active = true");
		}
		Query query = em.createQuery(queryString);
		List<CalCalendar> calendarEntities = query.getResultList();
		return ConverterUtil.convertToCalendarDTOList(calendarEntities);
	}

	@Override
	public List<CalendarDTO> getCalendars(String ownerId) {
		LOGGER.log(Level.FINEST, "Retrieving calendars with owner id {0}",
				ownerId);
		Query query = em
				.createQuery("SELECT c FROM CalCalendar c WHERE c.ownerId = :ownerId");
		query.setParameter("ownerId", ownerId);
		List<CalCalendar> calendarEntities = query.getResultList();
		List<CalendarDTO> list = ConverterUtil
				.convertToCalendarDTOList(calendarEntities);

		return list;
	}

	@Override
	public boolean activateCalendar(String calendarId, String userId)
			throws QCalendarException {
		LOGGER.log(Level.FINEST, "Activating calendar with id {0}", calendarId);

		CalCalendar calendarEntity = retrieveCalendar(calendarId);
		if (calendarEntity.isActive()) {
			return false;
		}
		calendarEntity.setActive(true);
		calendarEntity.setLastModifiedBy(userId);
		calendarEntity.setLastModifiedOn(System.currentTimeMillis());

		return true;
	}

	@Override
	public boolean deactivateCalendar(String calendarId, String userId)
			throws QCalendarException {
		LOGGER.log(Level.FINEST, "Disactivating calendar with id {0}",
				calendarId);

		CalCalendar calendarEntity = retrieveCalendar(calendarId);
		if (!calendarEntity.isActive()) {
			return false;
		}
		calendarEntity.setActive(false);
		calendarEntity.setLastModifiedBy(userId);
		calendarEntity.setLastModifiedOn(System.currentTimeMillis());

		return true;
	}

	@Override
	public boolean isCalendarActive(String calendarId)
			throws QCalendarException {
		LOGGER.log(Level.FINEST, "Checking if calendar with id {0} is active",
				calendarId);

		CalCalendar calendarEntity = retrieveCalendar(calendarId);
		return calendarEntity.isActive();
	}
}
