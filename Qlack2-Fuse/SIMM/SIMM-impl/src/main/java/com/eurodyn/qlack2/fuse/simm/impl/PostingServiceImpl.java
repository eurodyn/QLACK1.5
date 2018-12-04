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

import java.util.Arrays;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;

import com.eurodyn.qlack2.common.search.PagingParams;
import com.eurodyn.qlack2.fuse.simm.api.PostingService;
import com.eurodyn.qlack2.fuse.simm.api.dto.PostItemDTO;
import com.eurodyn.qlack2.fuse.simm.api.dto.SIMMConstants;
import com.eurodyn.qlack2.fuse.simm.api.exception.QSIMMException;
import com.eurodyn.qlack2.fuse.simm.impl.model.SimHomepageActivity;
import com.eurodyn.qlack2.fuse.simm.impl.model.SimHomepageActivityBin;
import com.eurodyn.qlack2.fuse.simm.impl.util.ConverterUtil;

/**
 * An OSGI service providing methods for Activity operation for Group Home page
 *
 * @author European Dynamics SA
 */
public class PostingServiceImpl implements PostingService {
    private static final Logger LOGGER = Logger.getLogger(PostingServiceImpl.class.getName());
    private EntityManager em;
    public static final String ADD_INDEX_DATA = "INDEX";
    public static final String UPDATE_INDEX = "UPDATE_INDEX";
    public static final String REMOVE_INDEX = "REMOVE_INDEX";

    public void setEm(EntityManager em) {
		this.em = em;
	}

    /**
     * Create a activity
     * @param pi
     * @return
     */
    @Override
        public PostItemDTO createActivity(PostItemDTO pi) throws QSIMMException {
        SimHomepageActivity homepageActivity = new SimHomepageActivity();
        homepageActivity.setCategoryId(StringUtils.isEmpty(pi.getCategoryID()) ? "0" : pi.getCategoryID());
        homepageActivity.setCreatedByUserId(pi.getCreatedByUserID() != null ? pi.getCreatedByUserID() : "system");
        homepageActivity.setCreatedOn(pi.getCreatedOn());
        homepageActivity.setDescription(pi.getDescription());
        homepageActivity.setHomepageId(pi.getHomepageID());
        homepageActivity.setLink(pi.getLink());
        if (pi.getParentHomepageID() != null) {
            homepageActivity.setParentHpageActvtId(
                    (SimHomepageActivity) em.find(SimHomepageActivity.class, pi.getParentHomepageID()));
        }
        homepageActivity.setStatus(pi.getStatus());
        homepageActivity.setTitle(pi.getTitle());
        homepageActivity.setCategoryIcon(pi.getCategoryIcon());
        em.persist(homepageActivity);
        pi.setId(homepageActivity.getId());
        if (pi.getBinContents() != null) {
            for (int i=0; i < pi.getBinContents().length; i++) {
                SimHomepageActivityBin shab = new SimHomepageActivityBin();
                shab.setActivityId(homepageActivity);
                shab.setBinData(pi.getBinContents()[i]);
                shab.setBinOrder(i);
                shab.setCreatedOn(System.currentTimeMillis());
                em.persist(shab);
            }
        }

        return pi;
    }

    /**
     * This method returns DTO of Home Page Activities
     *
     * @param homepageID
     * @param paging
     * @param status
     * @param includeChildren
     * @return
     */
    @Override
        public PostItemDTO[] getHomePageActivities(String homepageID, PagingParams paging,
            byte[] status, boolean includeChildren, boolean includeBinary) {
        return getHomePagesActivities(new String[]{homepageID}, paging, status, includeChildren,
                includeBinary);
    }

    @Override
        public PostItemDTO[] getHomePagesActivities(String[] homepageIDs, PagingParams paging,
            byte[] status, boolean includeChildren, boolean includeBinary) {
        Query q = em.createQuery(
                "select ha from SimHomepageActivity ha "
                + "  where ha.homepageId in (:homepages) "
                + "    and ha.status in (:status) "
                + "and ha.parentHpageActvtId is null"
                + "   order by ha.createdOn desc");
        q.setParameter("status", Arrays.asList(status));
        q.setParameter("homepages", Arrays.asList(homepageIDs));
        
        if ((paging != null) && (paging.getCurrentPage() > -1)) {
        	q.setFirstResult((paging.getCurrentPage() - 1)
        			* paging.getPageSize());
        	q.setMaxResults(paging.getPageSize());
        }

        PostItemDTO[] postItems = ConverterUtil.SIMHomePageActivity_PostItemDTO(q, includeChildren,
                includeBinary);

        return postItems;
    }

    /**
     * Get all children of a activity
     *
     * @param parentId
     * @param paging
     * @param status
     * @param orderAscending
     * @return
     */
    @Override
        public PostItemDTO[] getActivityChildren(String parentId, PagingParams paging, byte[] status, boolean orderAscending) {
        return getActivityChildren(parentId, paging, status, orderAscending, null);
    }

    /**
     * Get Children of a activity in particular category
     *
     * @param parentId
     * @param paging
     * @param status
     * @param orderAscending
     * @param activityCategoryID
     * @return
     */
    @Override
        public PostItemDTO[] getActivityChildren(String parentId, PagingParams paging,
            byte[] status, boolean orderAscending, String activityCategoryID) {
        String queryString = "select ha from SimHomepageActivity ha "
                + "where ha.parentHpageActvtId.id = :parentId "
                + "and ha.status in (:status)";
        if (activityCategoryID != null) {
            queryString = queryString.concat(" and ha.categoryId = :categoryID");
        }
        if (orderAscending) {
            queryString = queryString.concat(" order by ha.createdOn asc");
        } else {
            queryString = queryString.concat(" order by ha.createdOn desc");
        }
        Query q = em.createQuery(queryString);
        q.setParameter("status", Arrays.asList(status));
        q.setParameter("parentId", parentId);
        if (activityCategoryID != null) {
            q.setParameter("categoryID", activityCategoryID);
        }
        
        if ((paging != null) && (paging.getCurrentPage() > -1)) {
            q.setFirstResult((paging.getCurrentPage() - 1)
					* paging.getPageSize());
			q.setMaxResults(paging.getPageSize());

        }

        PostItemDTO[] postItems = ConverterUtil.SIMHomePageActivity_PostItemDTO(q, false, false);

        return postItems;
    }

    /**
     * Get no of children of a activity
     *
     * @param parentId
     * @param status
     * @return
     */
    @Override
        public long getChildrenNumber(String parentId, byte[] status) {
        return getChildrenNumber(parentId, status, null);
    }

    /**
     * Get no of children of a activity in a category
     * @param parentId
     * @param status
     * @param activityCategoryID
     * @return
     */
    @Override
        public long getChildrenNumber(String parentId, byte[] status, String activityCategoryID) {
        String queryString = "select count(ha) from SimHomepageActivity ha "
                + "where ha.parentHpageActvtId.id = :parentId "
                + "and ha.status in (:status)";
        if (activityCategoryID != null) {
            queryString = queryString.concat(" and ha.categoryId = :categoryID");
        }
        Query q = em.createQuery(queryString);
        q.setParameter("status", Arrays.asList(status));
        q.setParameter("parentId", parentId);
        if (activityCategoryID != null) {
            q.setParameter("categoryID", activityCategoryID);
        }

        return ((Long)q.getSingleResult()).longValue();
    }

    /**
     *
     * @param homepageID
     * @param activityCategoryID
     * @param status
     * @return
     */
    @Override
        public PostItemDTO getLastActivityOfType(String homepageID, String activityCategoryID, byte[] status) {
        Query q = em.createQuery(
                "select ha from SimHomepageActivity ha "
                + "  where ha.homepageId = :homepageID "
                + "    and ha.status in (:status)"
                + "    and ha.categoryId = :categoryID "
                + "order by ha.createdOn desc");
        q.setParameter("categoryID", activityCategoryID);
        q.setParameter("status", Arrays.asList(status));
        q.setParameter("homepageID", homepageID);
        q.setMaxResults(1);

        PostItemDTO[] results = ConverterUtil.SIMHomePageActivity_PostItemDTO(q, false, false);

        return (results != null ? results[0] : null);
    }

    /**
     * Approve a activity
     * @param homePageActivityID
     */
    @Override
        public void approveActivity(String homePageActivityID) {
        SimHomepageActivity homePageActivity = findActivityByID(homePageActivityID);
        homePageActivity.setStatus(SIMMConstants.HOME_PAGE_ACTIVITY_STATUS_APPROVED);
        em.persist(homePageActivity);
    }

    /**
     * Update a activity
     * @param pi
     */
    @Override
        public void updateActivity(PostItemDTO pi) throws QSIMMException {
        SimHomepageActivity homepageActivity = findActivityByID(pi.getId());
        homepageActivity.setCategoryId(pi.getCategoryID());
        homepageActivity.setCreatedByUserId(pi.getCreatedByUserID() != null ? pi.getCreatedByUserID() : "system");
        homepageActivity.setCreatedOn(pi.getCreatedOn());
        homepageActivity.setDescription(pi.getDescription());
        homepageActivity.setHomepageId(pi.getHomepageID());
        homepageActivity.setLink(pi.getLink());
        if (pi.getParentHomepageID() != null) {
            homepageActivity.setParentHpageActvtId(
                    (SimHomepageActivity) em.find(SimHomepageActivity.class, pi.getParentHomepageID()));
        }
        homepageActivity.setStatus(pi.getStatus());
        homepageActivity.setTitle(pi.getTitle());

        if (pi.getBinContents() != null) {
            homepageActivity.getSimHomepageActivityBins().clear();
            for (int i=0; i < pi.getBinContents().length; i++) {
                SimHomepageActivityBin shab = new SimHomepageActivityBin();
                shab.setActivityId(homepageActivity);
                shab.setBinData(pi.getBinContents()[i]);
                shab.setBinOrder(i);
                shab.setCreatedOn(System.currentTimeMillis());
                em.persist(shab);
            }
        }

    }

    /**
     * delete a activity
     * @param homePageActivityID
     */
    @Override
        public void deleteActivity(PostItemDTO activity) throws QSIMMException {
        SimHomepageActivity homePageActivity = findActivityByID(activity.getId());

        String parentId = homePageActivity.getParentHpageActvtId() == null ? null : homePageActivity.getParentHpageActvtId().getId();
        String parentTitle = homePageActivity.getParentHpageActvtId() == null ? null : homePageActivity.getParentHpageActvtId().getTitle();

        em.remove(homePageActivity);
    }

    @Override
        public PostItemDTO getActivity(String activityId, boolean includeChildren,
            boolean includeBinary) {
        SimHomepageActivity homePageActivity = findActivityByID(activityId);
        if (homePageActivity == null) {
            return null;
        }

        return ConverterUtil.SIMHomePageActivity_PostItemDTO(homePageActivity, includeChildren,
                includeBinary);
    }

    /**
     * Find a activity
     * @param activityID
     * @return
     */
        private SimHomepageActivity findActivityByID(String activityID) {
        return em.find(SimHomepageActivity.class, activityID);
    }

}
