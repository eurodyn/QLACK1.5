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
package com.eurodyn.qlack2.fuse.simm.impl;

import static com.eurodyn.qlack2.fuse.simm.api.dto.SIMMConstants.GROUP_PRIVACY_INVITED;
import static com.eurodyn.qlack2.fuse.simm.api.dto.SIMMConstants.GROUP_PRIVACY_PRIVATE;
import static com.eurodyn.qlack2.fuse.simm.api.dto.SIMMConstants.GROUP_PRIVACY_PUBLIC;
import static com.eurodyn.qlack2.fuse.simm.api.dto.SIMMConstants.GROUP_USER_STATUS_ACCEPTED;
import static com.eurodyn.qlack2.fuse.simm.api.dto.SIMMConstants.GROUP_USER_STATUS_INVITED;
import static com.eurodyn.qlack2.fuse.simm.api.dto.SIMMConstants.GROUP_USER_STATUS_REQUESTED_NEW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.eurodyn.qlack2.common.search.PagingParams;
import com.eurodyn.qlack2.fuse.simm.api.GroupUserService;
import com.eurodyn.qlack2.fuse.simm.api.dto.SocialGroupDTO;
import com.eurodyn.qlack2.fuse.simm.api.dto.SocialGroupUserDTO;
import com.eurodyn.qlack2.fuse.simm.api.exception.QAlreadyMember;
import com.eurodyn.qlack2.fuse.simm.api.exception.QInvalidID;
import com.eurodyn.qlack2.fuse.simm.api.exception.QNotInvited;
import com.eurodyn.qlack2.fuse.simm.api.exception.QNotUnique;
import com.eurodyn.qlack2.fuse.simm.api.exception.QSIMMException;
import com.eurodyn.qlack2.fuse.simm.impl.model.SimGroup;
import com.eurodyn.qlack2.fuse.simm.impl.model.SimGroupHasUser;
import com.eurodyn.qlack2.fuse.simm.impl.util.ConverterUtil;
import com.eurodyn.qlack2.fuse.simm.impl.util.SimValidationUtil;

/**
 *
 * @author Shambhu
 */
public class GroupUserServiceImpl implements GroupUserService {
    private static final Logger LOGGER = Logger.getLogger(GroupUserServiceImpl.class.getName());
    private EntityManager em;

    public void setEm(EntityManager em) {
		this.em = em;
	}

    /**
     * Check whether user has been invited
     *
     * @param userID
     * @param groupID
     * @return
     * @throws QSIMMException
     */
    @Override
        public boolean isInvited(String userID, String groupID) throws QSIMMException {
        SimGroupHasUser groupUser = findByIDs(userID, groupID);
        return groupUser != null ? groupUser.getStatus() == GROUP_USER_STATUS_INVITED
                : false;
    }

    /**
     * This method joins the user in a group according to the group type.
     * @param userId user ID
     * @param groupId group ID
     * @throws QSIMMException Throws exception if provided userID is null or
     * provided groupID is null or if user has already joined the group
     */
    @Override
        public SocialGroupDTO requestToJoinGroup(String userId, String groupId) throws QSIMMException {
        SimValidationUtil.validateUserIdAndGroupId(userId, groupId);
        SimGroup group = null;
        try {
            group = em.find(SimGroup.class, groupId);
            SimGroupHasUser groupUser = null;
            if (group.getPrivacy()!= null){
            switch (group.getPrivacy()) {
                case GROUP_PRIVACY_PUBLIC:
                    if (isInvited(userId, groupId)) {
                        groupUser = findByIDs(userId, groupId);
                        groupUser.setStatus(GROUP_USER_STATUS_ACCEPTED);
                        groupUser.setJoinedOnDate(System.currentTimeMillis());
                        em.persist(groupUser);
                    }
                    else {
                        groupUser =
                                new SimGroupHasUser(group, userId, GROUP_USER_STATUS_ACCEPTED, System.currentTimeMillis());
                        em.persist(groupUser);
                    }
                    break;
                case GROUP_PRIVACY_INVITED:
                    if (isInvited(userId, groupId)) {
                        groupUser = findByIDs(userId, groupId);
                        groupUser.setStatus(GROUP_USER_STATUS_ACCEPTED);
                        groupUser.setJoinedOnDate(System.currentTimeMillis());
                        em.persist(groupUser);
                    } else {
                        groupUser =
                                new SimGroupHasUser(group, userId, GROUP_USER_STATUS_REQUESTED_NEW, 0);
                        em.persist(groupUser);
                    }
                    break;
                case GROUP_PRIVACY_PRIVATE:
                    if (isInvited(userId, groupId)) {
                        groupUser = findByIDs(userId, groupId);
                        groupUser.setStatus(GROUP_USER_STATUS_ACCEPTED);
                        groupUser.setJoinedOnDate(System.currentTimeMillis());
                        em.persist(groupUser);
                    } else {
                        LOGGER.log(Level.INFO, "User ''{0}'' is not invited in group ''{1}''.",
                                new String[]{userId, groupId});
                        throw new QNotInvited(
                                "User can not join this group unless an invitation has been received first.");
                    }
                    break;
            }
          }
        } catch (EntityExistsException e) {
            throw new QAlreadyMember("User has been already joined in Group.");
        } catch (IllegalArgumentException e) {
            throw new QInvalidID("Group does not exist with the provided groupId.");
        }

        return ConverterUtil.convertGroupModelToDTO(group);
    }

    /**
     * This method find a SimGroupHasUser model object for provided group ID and User ID.
     * @param userID User ID
     * @param groupID group ID
     * @return SimGroupHasUser Model object for group and user.
     * @throws QSIMMException Throws exception if provided userID is null or
     * provided groupID is null
     */
        private SimGroupHasUser findByIDs(String userID, String groupID) throws QSIMMException {
        SimValidationUtil.validateUserIdAndGroupId(userID, groupID);

        StringBuilder quString = new StringBuilder("select gu from SimGroupHasUser gu ");
        quString.append("where gu.groupId.id = :groupID and gu.userId = :userID ");

        Query query = em.createQuery(quString.toString());
        query.setParameter("groupID", groupID);
        query.setParameter("userID", userID);
        SimGroupHasUser groupUser = null;
        int result = 0;
        for (@SuppressWarnings("unchecked") Iterator<SimGroupHasUser> itr = query.getResultList().iterator(); itr.hasNext();) {
            groupUser = itr.next();
            result++;
        }
        if (result > 1) {
            throw new QNotUnique("More than one GroupUser found with the provided userId and groupId.");
        }
        return groupUser;
    }

    /**
     * This method changes status of user in group as accepted (GROUP_USER_STATUS_ACCEPTED = 1)
     * to join the group.
     *
     * @param userID User ID
     * @param groupID group ID
     * @throws QSIMMException Throws exception if user has not been joined the group.
     */
    @Override
        public void acceptUserJoin(String userID, String groupID) throws QSIMMException {
        LOGGER.log(Level.FINEST, "Accepting user ''{0}'' join to group ''{1}''.", new String[]{userID, groupID});
        SimValidationUtil.validateUserIdAndGroupId(userID, groupID);
        SimGroup group = em.find(SimGroup.class, groupID);
        SimGroupHasUser groupUser = findByIDs(userID, groupID);
        SimValidationUtil.validateGroupUserModelObject(groupUser);
        groupUser.setJoinedOnDate(System.currentTimeMillis());
        groupUser.setStatus(GROUP_USER_STATUS_ACCEPTED);
        em.merge(groupUser);

    }

    /**
     * This method rejects a request of a user to join a group. Note that the actual user request
     * is **deleted** from the database.
     *
     * @param userID User ID
     * @param groupID group ID
     * @param byUser true if reject by user and false if rejected by moderator
     * @throws QSIMMException Throws exception if user has not been joined the group.
     */
    @Override
        public void rejectUserJoin(String userID, String groupID) throws QSIMMException {
        SimValidationUtil.validateUserIdAndGroupId(userID, groupID);
        SimGroup group = em.find(SimGroup.class, groupID);
        SimGroupHasUser groupUser = findByIDs(userID, groupID);
        em.remove(groupUser);

    }

    /**
     * This method removes the user for provided group,
     * it physically removes the user from group.
     * @param userID User ID
     * @param groupID group ID
     * @throws QSIMMException Throws exception if user has not been joined the group.
     */
    @Override
        public void leaveGroup(String userID, String groupID) throws QSIMMException {
        SimValidationUtil.validateUserIdAndGroupId(userID, groupID);
        SimGroupHasUser groupUser = findByIDs(userID, groupID);
        SimValidationUtil.validateGroupUserModelObject(groupUser);

        em.remove(groupUser);
    }

    /**
     * This method is not implemented yet.
     * @param userID User ID
     * @param groupID group ID
     */
    @Override
    public void shareGroup(String userID, String groupID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * This method returns SocialGroupUserDTO for supplied group ID and User ID.
     * @param userID User ID
     * @param groupID group ID
     * @return SocialGroupUserDTO
     * @throws QSIMMException Throws exception if provided userID or groupID is null.
     */
    @Override
        public SocialGroupUserDTO getGroupUser(String userID, String groupID) throws QSIMMException {
        SimValidationUtil.validateUserIdAndGroupId(userID, groupID);
        SimGroupHasUser groupUser = findByIDs(userID, groupID);

        if (groupUser == null) {
            return null;
        } else {
            return ConverterUtil.convertGroupUserModelToDTO(groupUser);
        }
    }

    /**
     * This method returns a list with all the users which are members in the same groups
     * as the passed-in user.
     * @param userID
     * @return array of userIds
     * @throws QSIMMException Throws exception if provided userID is null.
     */
    @Override
        public String[] getMembersForUserGroups(String userID) throws QSIMMException {
        SimValidationUtil.validateNullForuserID(userID);
        StringBuilder quString = new StringBuilder("select sghu1.userId from SimGroupHasUser sghu1 ");
        quString.append("where sghu1.groupId in (");
        quString.append("  select sghu2.groupId from SimGroupHasUser sghu2 ");
        quString.append("  where sghu2.userId = :userID) and sghu1.userId <> :userID and sghu1.status = :status");

        Query query = em.createQuery(quString.toString());
        query.setParameter("userID", userID);
        query.setParameter("status", GROUP_USER_STATUS_ACCEPTED);
        @SuppressWarnings("unchecked")
        List<String> results = query.getResultList();
        String[] retVal = new String[results.size()];
        int counter = 0;
        for (String i : results) {
            retVal[counter++] = i;
        }

        return retVal;
    }

    /**
     * This method returns array of SocialGroupUserDTO for provided group ID.
     * @param groupID
     * @param status byte array of status
     * @param paging
     * @return array of GroupDTOs
     * @throws QSIMMException Throws exception if provided groupID is null.
     */
    @Override
        public SocialGroupUserDTO[] listContactsForGroup(String groupID, byte[] status, PagingParams paging) throws QSIMMException {
        SimValidationUtil.validateNullForGroupIDForGroup(groupID);
        StringBuilder quString = new StringBuilder("select gu from SimGroupHasUser gu, SimGroup g ");
        quString.append("where gu.groupId.id = :groupID ");
        if (status != null && status.length > 0) {
            quString.append(" and gu.status in (:status) ");
        }
        quString.append("and g.id = gu.groupId.id order by gu.userId");
        Query query = em.createQuery(quString.toString());
        query.setParameter("groupID", groupID);
        if (status != null && status.length > 0) {
            query.setParameter("status", Arrays.asList(status));
        }
        if ((paging != null) && (paging.getCurrentPage() > -1)) {
        	query.setFirstResult((paging.getCurrentPage() - 1)
        			* paging.getPageSize());
        	query.setMaxResults(paging.getPageSize());
        }
        
        SocialGroupUserDTO[] groupUsers = ConverterUtil.sortGroupUserDTOs(query);

        return groupUsers;
    }

    /**
     * This method returns array of Group DTOs for provided user ID.
     * @param userID
     * @param status byte array, if status is null then this will return for all status
     * @return array of GroupDTOs
     * @throws QSIMMException Throws exception if provided userID is null.
     */
    @Override
        public SocialGroupDTO[] listGroupsForUser(String userID, String searchTerm, byte[] status, PagingParams paging) throws QSIMMException {
        SimValidationUtil.validateNullForuserID(userID);
        List<Byte> statusList = new ArrayList<Byte>();
        if (status != null && status.length > 0) {
            for (byte s : status) {
                statusList.add(s);
            }
        }

        StringBuilder quString = new StringBuilder("select g from SimGroup g, SimGroupHasUser gu ");
        quString.append("where g.id=gu.groupId.id and gu.userId = :userID ");
        if (searchTerm != null) {
            quString.append(" and upper(g.name) like :searchTerm");
        }
        if (statusList.size() > 0) {
            quString.append(" and gu.status in (:status) ");
        }

        Query query = em.createQuery(quString.toString());
        query.setParameter("userID", userID);
        if (searchTerm != null) {
            query.setParameter("searchTerm", "%" + searchTerm.toUpperCase() + "%");
        }
        if (statusList.size() > 0) {
            query.setParameter("status", statusList);
        }
        if ((paging != null) && (paging.getCurrentPage() > -1)) {
	        query.setFirstResult((paging.getCurrentPage() - 1)
					* paging.getPageSize());
			query.setMaxResults(paging.getPageSize());
        }

        SocialGroupDTO[] groupDTOs = ConverterUtil.getGroupDTOArrayForGroupQuery(query);

        return groupDTOs;
    }

    /**
     * Returns all Contacts from all groups of provided user ID in which this user has been joined
     *
     * @param userID
     * @param status byte array of status
     * @param paging
     * @return array of GroupDTOs
     * @throws QSIMMException Throws exception if Provided userID is null.
     */
    @Override
        public SocialGroupUserDTO[] listContactsForUser(String userID, byte[] status, PagingParams paging) throws QSIMMException {
        SimValidationUtil.validateNullForuserID(userID);
        List<Byte> statusList = new ArrayList<Byte>();
        if (status != null && status.length > 0) {
            for (byte s : status) {
                statusList.add(s);
            }
        }
        StringBuilder quString = new StringBuilder("select gu from SimGroupHasUser gu, SimGroup g where ");
        quString.append("g.id = gu.groupId.id and gu.userId <> :userID ");
        if (statusList.size() > 0) {
            quString.append(" and gu.status in (:status) ");
        }
        quString.append("and gu.groupId.id in (select guin.groupId.id from SimGroupHasUser guin where guin.userId = :userIdIn )");
        quString.append(" order by gu.userId");

        Query query = em.createQuery(quString.toString());
        query.setParameter("userID", userID);
        query.setParameter("userIdIn", userID);
        if (statusList.size() > 0) {
            query.setParameter("status", statusList);
        }
        
        if ((paging != null) && (paging.getCurrentPage() > -1)) {
        	query.setFirstResult((paging.getCurrentPage() - 1)
        			* paging.getPageSize());
        	query.setMaxResults(paging.getPageSize());
        }

        SocialGroupUserDTO[] groupUserDTOs = ConverterUtil.sortGroupUserDTOs(query);

        return groupUserDTOs;
    }

    /**
     * This method returns array of available Groups as Groups DTOs for
     * User to join for provided user ID.
     * @param searchTerm if searchTerm is null then searchTerm does not apply as condition
     *                  and method will return values without searchTerm
     * @param userID
     * @param status
     * @param paging
     * @return array of GroupDTOs
     * @throws QSIMMException Throws exception if Provided userID is null.
     */
    @Override
        public SocialGroupDTO[] listAvailableGroupsForJoining(String searchTerm, String userID, byte[] status, PagingParams paging)
            throws QSIMMException {
        SimValidationUtil.validateNullForuserID(userID);
        StringBuilder quString = new StringBuilder("select g from SimGroup g where g.id not in ");
        quString.append("(select gu.groupId.id from SimGroupHasUser gu where gu.userId = :userID) and g.privacy in (:privacy) ");
        if (searchTerm != null) {
            quString.append(" and upper(g.name) like :searchTerm");
        }
        if (status != null & status.length > 0) {
            quString.append(" and g.status in (:status) ");
        }

        Query query = em.createQuery(quString.toString());
        query.setParameter("userID", userID);
        query.setParameter("privacy", Arrays.asList(new byte[]{GROUP_PRIVACY_PUBLIC, GROUP_PRIVACY_INVITED}));
        if ((searchTerm != null) && (!searchTerm.trim().equals(""))) {
            query.setParameter("searchTerm", "%" + searchTerm.toUpperCase() + "%");
        }
        if (status != null && status.length > 0) {
            query.setParameter("status", Arrays.asList(status));
        }
        
        if ((paging != null) && (paging.getCurrentPage() > -1)) {
        	 query.setFirstResult((paging.getCurrentPage() - 1)
     				* paging.getPageSize());
     		query.setMaxResults(paging.getPageSize());	
        }

        SocialGroupDTO[] groupDTOs = ConverterUtil.getGroupDTOArrayForGroupQuery(query);

        return groupDTOs;
    }

    /**
     * This method returns all group users for provided userID and status
     * for eg. status = GROUP_USER_STATUS_INVITED implies all the
     * group invitations to a user that are not yet responded by the user.
     * @param userID
     * @param status
     * @return array of SocialGroupUserDTO
     * @throws QSIMMException
     */
    @Override
        public SocialGroupUserDTO[] getAllContactsForStatus(String userID, byte status) throws QSIMMException {
        SimValidationUtil.validateNullForUserIdAndStatus(userID, status);
        StringBuilder quString = new StringBuilder("select gu from SimGroupHasUser gu, SimGroup g where ");
        quString.append("g.id = gu.groupId.id and gu.userId = :userID and gu.status = :status");

        Query query = em.createQuery(quString.toString());
        query.setParameter("userID", userID);
        query.setParameter("status", status);
        SocialGroupUserDTO[] groupUserDTOs = ConverterUtil.getGroupUserDTOArrayForGroupQuery(query);

        return groupUserDTOs;
    }

    /**
     * Invite user map the user with group and status as invited
     *
     * @param userID
     * @param groupID
     */
    @Override
        public void inviteUser(String userID, SocialGroupDTO groupDTO) throws QSIMMException {
        LOGGER.log(Level.INFO, "Inviting user ''{0}'' to group ''{1}''.", new String[]{userID, groupDTO.getId()});
        SimGroupHasUser groupUser = new SimGroupHasUser();
        SimGroup group = em.find(SimGroup.class, groupDTO.getId());
        groupUser.setGroupId(group);
        groupUser.setUserId(userID);
        groupUser.setStatus(GROUP_USER_STATUS_INVITED);
        em.persist(groupUser);
        
    }

}
