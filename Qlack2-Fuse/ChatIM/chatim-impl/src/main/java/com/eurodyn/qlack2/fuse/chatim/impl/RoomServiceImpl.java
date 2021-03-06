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
package com.eurodyn.qlack2.fuse.chatim.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;

import com.eurodyn.qlack2.common.search.PagingParams;
import com.eurodyn.qlack2.fuse.chatim.api.RoomService;
import com.eurodyn.qlack2.fuse.chatim.api.dto.RoomDTO;
import com.eurodyn.qlack2.fuse.chatim.api.dto.RoomPropertyDTO;
import com.eurodyn.qlack2.fuse.chatim.api.dto.RoomStatisticsDTO;
import com.eurodyn.qlack2.fuse.chatim.api.dto.RoomUserDTO;
import com.eurodyn.qlack2.fuse.chatim.api.dto.RoomWordFilterDTO;
import com.eurodyn.qlack2.fuse.chatim.api.exception.QChatIMException;
import com.eurodyn.qlack2.fuse.chatim.impl.model.ChaProperties;
import com.eurodyn.qlack2.fuse.chatim.impl.model.ChaRoomHasParticipants;
import com.eurodyn.qlack2.fuse.chatim.impl.model.ChaRooms;
import com.eurodyn.qlack2.fuse.chatim.impl.model.ChaWordFilter;
import com.eurodyn.qlack2.fuse.chatim.impl.util.LookupHelper;

/**
 *
 * @author European Dynamics SA
 */
public class RoomServiceImpl implements RoomService {
	public static final Logger LOGGER = Logger.getLogger(RoomServiceImpl.class
			.getName());
	private EntityManager em;

	public void setEm(EntityManager em) {
		this.em = em;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param roomDTO
	 *            {@inheritDoc}
	 * @return {@inheritDoc}
	 * @throws QChatIMException
	 *             {@inheritDoc}
	 */
	@Override
	public String createRoom(RoomDTO roomDTO) throws QChatIMException {

		// Create the chat room.
		ChaRooms chatRoom = new ChaRooms();
		chatRoom.setCreatedBy(roomDTO.getCreatedByUserID() != null ? roomDTO
				.getCreatedByUserID() : "system");
		chatRoom.setCreatedOn(roomDTO.getCreatedOn() != null ? roomDTO
				.getCreatedOn() : System.currentTimeMillis());
		chatRoom.setTitle(roomDTO.getTitle());
		if (roomDTO.getTargetCommunityID() != null) {
			chatRoom.setGroupId(roomDTO.getTargetCommunityID());
		}
		em.persist(chatRoom);

		return chatRoom.getId();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param roomID
	 *            {@inheritDoc}
	 * @param userID
	 *            {@inheritDoc}
	 * @throws QChatIMException
	 *             {@inheritDoc}
	 */
	@Override
	public void joinRoom(String roomID, String userID) throws QChatIMException {

		if (StringUtils.isEmpty(roomID)) {
			throw new QChatIMException("The ID of the room can not be empty.");
		}
		if (StringUtils.isEmpty(userID)) {
			throw new QChatIMException(
					"The ID of the user roomID can not be empty.");
		}

		// Get the requested room.
		ChaRooms chatRoom = LookupHelper.getRoom(em, roomID);
		Query q = em
				.createQuery("select chp from ChaRoomHasParticipants chp where "
						+ "chp.roomId = :room and chp.userId = :userId");
		q.setParameter("room", chatRoom);
		q.setParameter("userId", userID);
		List l = q.getResultList();
		if (l.isEmpty()) {
			ChaRoomHasParticipants crhp = new ChaRoomHasParticipants();
			crhp.setJoinedOn(System.currentTimeMillis());
			crhp.setRoomId(chatRoom);
			crhp.setUserId(userID);
			em.persist(crhp);
		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * @param roomID
	 *            {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public RoomUserDTO[] getRoomUsers(String roomID) {
		ChaRooms chatRoom = LookupHelper.getRoom(em, roomID);
		Set<ChaRoomHasParticipants> participants = chatRoom.getChaRoomHasParticipantses();
		ArrayList<RoomUserDTO> retVal = new ArrayList<RoomUserDTO>();
		Iterator<ChaRoomHasParticipants> i = participants.iterator();
		while (i.hasNext()) {
			ChaRoomHasParticipants crhp = i.next();
			retVal.add(new RoomUserDTO(crhp.getUserId(), crhp.getJoinedOn()));
		}

		RoomUserDTO[] roomDTOs = (RoomUserDTO[]) retVal
				.toArray(new RoomUserDTO[retVal.size()]);

		return roomDTOs;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param userID
	 *            {@inheritDoc}
	 * @param roomID
	 *            {@inheritDoc}
	 * @throws QChatIMException
	 *             {@inheritDoc}
	 */
	@Override
	public void leaveRoom(String userID, String roomID) throws QChatIMException {
		// Get the requested room.
		ChaRooms chatRoom = LookupHelper.getRoom(em, roomID);

		// Check if the user is already a member of that room (i.e. since when
		// the user
		// leaves the chat page the application automatically removes the user
		// from the room,
		// this is an exceptional case).
		Query q = em
				.createQuery("select chp from ChaRoomHasParticipants chp where "
						+ "chp.roomId = :room and chp.userId = :userId");
		q.setParameter("room", chatRoom);
		q.setParameter("userId", userID);
		List<ChaRoomHasParticipants> l = q.getResultList();
		if (l.size() > 0) {
			Iterator<ChaRoomHasParticipants> i = l.iterator();
			ChaRoomHasParticipants crhp = i.next();
			em.remove(crhp);

		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param userID
	 *            {@inheritDoc}
	 */
	@Override
	public void leaveAllRooms(String userID) {
		Query q = em
				.createQuery("select chp from ChaRoomHasParticipants chp where "
						+ "chp.userId = :userId");
		q.setParameter("userId", userID);
		List<ChaRoomHasParticipants> l = q.getResultList();

		if (l.size() > 0) {
			Iterator<ChaRoomHasParticipants> i = l.iterator();
			while (i.hasNext()) {
				ChaRoomHasParticipants crhp = i.next();
				em.remove(crhp);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param groupIDs
	 *            {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public RoomDTO[] listAvailableRoomsForGroups(String[] groupIDs) {
		RoomDTO[] retVal;

		Query q = em
				.createQuery("select cr from ChaRooms cr where cr.groupId in (:groupIDs)");
		q.setParameter("groupIDs", Arrays.asList(groupIDs));
		retVal = convertChaRoomsToRoomDTOA(q.getResultList());

		return retVal;
	}

	private RoomDTO[] convertChaRoomsToRoomDTOA(List l) {
		RoomDTO[] retVal = new RoomDTO[0];
		if (l != null) {
			retVal = new RoomDTO[l.size()];
			Iterator<ChaRooms> i = l.iterator();
			int counter = 0;
			while (i.hasNext()) {
				ChaRooms chatRoom = i.next();
				RoomDTO roomDTO = new RoomDTO();
				if (chatRoom.getCreatedBy() != null) {
					roomDTO.setCreatedByUserID(chatRoom.getCreatedBy());
				}
				roomDTO.setCreatedOn(chatRoom.getCreatedOn());
				if (chatRoom.getGroupId() != null) {
					roomDTO.setTargetCommunityID(chatRoom.getGroupId());
				}
				roomDTO.setTitle(chatRoom.getTitle());
				roomDTO.setId(chatRoom.getId());
				retVal[counter++] = roomDTO;
			}
		}

		return retVal;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param roomDTO
	 *            {@inheritDoc}
	 * @throws QChatIMException
	 *             {@inheritDoc}
	 */
	@Override
	public void removeRoom(RoomDTO roomDTO) throws QChatIMException {
		// Get the requested room.
		ChaRooms chatRoom = LookupHelper.getRoom(em, roomDTO.getId());

		// remove the room
		em.remove(chatRoom);

	}

	/**
	 * {@inheritDoc}
	 *
	 * @param room
	 *            {@inheritDoc}
	 * @param pagingParams
	 *            {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public RoomDTO[] searchRooms(RoomDTO room, PagingParams pagingParams) {
		// TODO Kaskoura
		// Criteria criteria = CriteriaFactory.createCriteria("ChaRooms");
		// if (room.getId() != null) {
		// criteria.add(Restrictions.eq("id", room.getId()));
		// }
		// if (room.getTitle() != null) {
		// criteria.add(Restrictions.eq("title", room.getTitle()));
		// }
		// if (room.getCreatedByUserID() != null) {
		// criteria.add(Restrictions.eq("createdBy",
		// room.getCreatedByUserID()));
		// }
		// if (room.getTargetCommunityID() != null) {
		// criteria.add(Restrictions.eq("groupId",
		// room.getTargetCommunityID()));
		// }
		// Query query = criteria.prepareQuery(em);
		// if (pagingParams != null) {
		// query = ApplyPagingParams.apply(query, pagingParams);
		// }
		// List<ChaRooms> retVal = query.getResultList();
		//
		// return convertChaRoomsToRoomDTOA(retVal);

		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param roomProperty
	 *            {@inheritDoc}
	 * @throws QChatIMException
	 *             {@inheritDoc}
	 */
	@Override
	public void setRoomProperty(RoomPropertyDTO roomProperty)
			throws QChatIMException {
		ChaRooms chatRoom = LookupHelper.getRoom(em, roomProperty.getRoomId());
		ChaProperties chaProperty = new ChaProperties(chatRoom,
				roomProperty.getPropertyName(), roomProperty.getPropertyValue());
		em.persist(chaProperty);

	}

	/**
	 * {@inheritDoc}
	 *
	 * @param roomId
	 *            {@inheritDoc}
	 * @param propertyName
	 *            {@inheritDoc}
	 * @param recipientUserID
	 *            {@inheritDoc}
	 * @return {@inheritDoc}
	 * @throws QChatIMException
	 *             {@inheritDoc}
	 */
	@Override
	public RoomPropertyDTO getRoomProperty(String roomId, String propertyName,
			String recipientUserID) throws QChatIMException {
		RoomPropertyDTO dto = null;

		ChaRooms chatRoom = LookupHelper.getRoom(em, roomId);
		Set<ChaProperties> properties = chatRoom.getChaPropertieses();
		if (properties != null && !properties.isEmpty()) {
			for (Iterator iter = properties.iterator(); iter.hasNext();) {
				ChaProperties chaProperty = (ChaProperties) iter.next();
				if (chaProperty.getPropertyName().equals(propertyName)) {
					dto = new RoomPropertyDTO(chaProperty.getId(),
							chaProperty.getPropertyName(),
							chaProperty.getPropertyValue());
				}
			}
		}

		return dto;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param roomId
	 *            {@inheritDoc}
	 * @param recipientUserID
	 *            {@inheritDoc}
	 * @return {@inheritDoc}
	 * @throws QChatIMException
	 *             {@inheritDoc}
	 */
	@Override
	public String getRoomStatistics(String roomId, String recipientUserID)
			throws QChatIMException {
		StringWriter xml = new StringWriter();
		try {
			ChaRooms chatRoom = LookupHelper.getRoom(em, roomId);
			RoomStatisticsDTO dto = new RoomStatisticsDTO();
			JAXBContext context;
			context = JAXBContext.newInstance(RoomStatisticsDTO.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			dto.setNumberOfUsers(chatRoom.getChaRoomHasParticipantses().size());
			dto.setNumberOfEntries(chatRoom.getChaRoomHasMessageses().size());
			dto.setTitle(chatRoom.getTitle());
			dto.setCreatedOn(chatRoom.getCreatedOn() + "");
			dto.setCreatedBy(chatRoom.getCreatedBy());
			marshaller.marshal(dto, xml);
			xml.flush();

		} catch (JAXBException ex) {
			LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			throw new QChatIMException(ex.getLocalizedMessage());
		}

		return xml.toString();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param filter
	 *            {@inheritDoc}
	 * @throws QChatIMException
	 *             {@inheritDoc}
	 */
	@Override
	public void setRoomFilter(RoomWordFilterDTO filter) throws QChatIMException {
		ChaWordFilter chaWordFilter;
		ChaRooms chatRoom = LookupHelper.getRoom(em, filter.getRoomId());
		Query q = em
				.createQuery("select cwf from ChaWordFilter cwf where cwf.roomId = :roomID");
		q.setParameter("roomID", chatRoom);
		List result = q.getResultList();
		if (result.size() > 0) {
			chaWordFilter = (ChaWordFilter) result.get(0);
		} else {
			chaWordFilter = new ChaWordFilter(chatRoom, filter.getFilter());
		}
		chaWordFilter.setFilter(filter.getFilter());
		em.persist(chaWordFilter);

	}

	/**
	 * {@inheritDoc}
	 *
	 * @param roomId
	 *            {@inheritDoc}
	 * @param recipientUserID
	 *            {@inheritDoc}
	 * @return {@inheritDoc}
	 * @throws QChatIMException
	 *             {@inheritDoc}
	 */
	@Override
	public RoomWordFilterDTO getRoomFilter(String roomId, String recipientUserID)
			throws QChatIMException {
		RoomWordFilterDTO retVal = new RoomWordFilterDTO();
		retVal.setRoomId(roomId);

		Query q = em
				.createQuery("select cwf from ChaWordFilter cwf where cwf.roomId.id = :roomId");
		q.setParameter("roomId", roomId);

		Object result = q.getSingleResult();
		if (result != null) {
			retVal.setFilter(((ChaWordFilter) result).getFilter());
		}

		return retVal;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param roomId
	 *            {@inheritDoc}
	 * @throws QChatIMException
	 *             {@inheritDoc}
	 */
	@Override
	public void removeRoomFilter(String roomId) throws QChatIMException {
		// TODO since we have a single filter for each room, replace this code
		// with a more efficient version.
		ChaRooms chatRoom = LookupHelper.getRoom(em, roomId);
		Set<ChaWordFilter> filters = chatRoom.getChaWordFilters();
		Iterator<ChaWordFilter> itr = filters.iterator();
		while (itr.hasNext()) {
			ChaWordFilter cf = itr.next();
			em.remove(cf);
		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * @param roomID
	 *            {@inheritDoc}
	 * @param userID
	 *            {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public Long getRoomJoiningTimeForUser(String roomID, String userID) {
		//TODO Kaskoura
		Long retVal = null;
		// Criteria criteria = CriteriaFactory
		// .createCriteria("ChaRoomHasParticipants");
		// criteria.createAlias("roomId", "room");
		// criteria.add(Restrictions.eq("userId", userID));
		// criteria.add(Restrictions.eq("room.id", roomID));
		// Query q = criteria.prepareQuery(em);
		// List<ChaRoomHasParticipants> l = q.getResultList();
		// if ((l != null) && (!l.isEmpty())) {
		// retVal = Long.valueOf(l.get(0).getJoinedOn());
		// }

		return retVal;
	}

}
