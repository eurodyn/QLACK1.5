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
package com.eurodyn.qlack2.fuse.blog.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;

import com.eurodyn.qlack2.common.exception.QException;
import com.eurodyn.qlack2.common.search.PagingParams;
import com.eurodyn.qlack2.fuse.blog.api.PostService;
import com.eurodyn.qlack2.fuse.blog.api.dto.BlogCategoryDTO;
import com.eurodyn.qlack2.fuse.blog.api.dto.BlogDTO;
import com.eurodyn.qlack2.fuse.blog.api.dto.BlogPostDTO;
import com.eurodyn.qlack2.fuse.blog.api.dto.BlogTagDTO;
import com.eurodyn.qlack2.fuse.blog.api.exception.QInvalidBlog;
import com.eurodyn.qlack2.fuse.blog.api.exception.QInvalidCategory;
import com.eurodyn.qlack2.fuse.blog.api.exception.QInvalidPost;
import com.eurodyn.qlack2.fuse.blog.api.exception.QInvalidTag;
import com.eurodyn.qlack2.fuse.blog.impl.model.BlgBlog;
import com.eurodyn.qlack2.fuse.blog.impl.model.BlgCategory;
import com.eurodyn.qlack2.fuse.blog.impl.model.BlgComment;
import com.eurodyn.qlack2.fuse.blog.impl.model.BlgPost;
import com.eurodyn.qlack2.fuse.blog.impl.model.BlgTag;
import com.eurodyn.qlack2.fuse.blog.impl.model.BlgTrackbacks;
import com.eurodyn.qlack2.fuse.blog.impl.util.ConverterUtil;

/**
 * @author European Dynamics SA.
 */
public class PostServiceImpl implements PostService {
	private static final Logger LOGGER = Logger.getLogger(PostServiceImpl.class.getName());

	private EntityManager em;
	
	public void setEm(EntityManager em) {
		this.em = em;
	}

	@Override
	public String createPost(BlogPostDTO dto) throws QException {
		LOGGER.log(Level.FINE, "Creating post with name {0}", dto.getName());

		BlgPost blgPost = new BlgPost();

		blgPost.setBlogId(BlgBlog.find(em, dto.getBlogId()));
		blgPost.setBody(dto.getBody());
		blgPost.setCommentsEnabled(dto.isCommentsEnabled());
		blgPost.setDatePosted(new Date().getTime());
		blgPost.setName(dto.getName());
		blgPost.setPublished(dto.isPublished());
		blgPost.setTrackbackPingUrl(dto.getTrackbackPingUrl());

		// archived value will be always set as false during post creation.
		blgPost.setArchived(false);
		
		List<BlgCategory> categories = new ArrayList<>();
		if (dto.getBlgCategories() != null) {
			for (String categoryId : dto.getBlgCategories()) {
				BlgCategory category = em.getReference(BlgCategory.class, categoryId);
				categories.add(category);
			}
		}
		blgPost.setBlgPostHasCategories(categories);
		
		List<BlgTag> tags = new ArrayList<>();
		if (dto.getBlgTags() != null) {
			for (String tagId : dto.getBlgTags()) {
				BlgTag tag = em.getReference(BlgTag.class, tagId);
				tags.add(tag);
			}
		}
		blgPost.setBlgPostHasTags(tags);
		
		// generate the trackback ping url
		blgPost.setTrackbackPingUrl(blgPost.getTrackbackPingUrl() + blgPost.getId());

		em.persist(blgPost);

		List<BlgTrackbacks> blgTrackbacksList = null;
		if (dto.getTrackBackPostIds() != null) {
			blgTrackbacksList = sendTrackback(dto.getTrackBackPostIds(), blgPost.getId(),
					dto.getExcerpt(), dto.getUserId());
		}
		
		blgPost.setBlgTrackbacksesForPostId(blgTrackbacksList);	

		return blgPost.getId();
	}

	@Override
	public void editPost(BlogPostDTO dto) throws QException {
		LOGGER.log(Level.FINE, "Updating post with name {0}", dto.getName());
		
		BlgPost blgPost = BlgPost.find(em, dto.getId());
		
		if (blgPost == null) {
			return;
		}
		
		blgPost.setArchived(dto.isArchived());
		blgPost.setBlogId(BlgBlog.find(em, dto.getBlogId()));
		blgPost.setBody(dto.getBody());
		blgPost.setCommentsEnabled(dto.isCommentsEnabled());
		blgPost.setDatePosted(new Date().getTime());
		blgPost.setName(dto.getName());
		blgPost.setPublished(dto.isPublished());
		blgPost.setTrackbackPingUrl(dto.getTrackbackPingUrl());
		
		List<BlgCategory> categories = new ArrayList<>();
		if (dto.getBlgCategories() != null) {
			for (String categoryId : dto.getBlgCategories()) {
				BlgCategory category = em.getReference(BlgCategory.class, categoryId);
				categories.add(category);
			}
		}
		blgPost.setBlgPostHasCategories(categories);
		
		List<BlgTag> tags = new ArrayList<>();
		if (dto.getBlgTags() != null) {
			for (String tagId : dto.getBlgTags()) {
				BlgTag tag = em.getReference(BlgTag.class, tagId);
				tags.add(tag);
			}
		}
		blgPost.setBlgPostHasTags(tags);
		
		// generate the trackback ping url
		blgPost.setTrackbackPingUrl(blgPost.getTrackbackPingUrl() + blgPost.getId());

		List<BlgTrackbacks> blgTrackbacksList = null;
		if (dto.getTrackBackPostIds() != null) {
			blgTrackbacksList = sendTrackback(dto.getTrackBackPostIds(), blgPost.getId(),
					dto.getExcerpt(), dto.getUserId());
		}
		
		blgPost.setBlgTrackbacksesForPostId(blgTrackbacksList);	
	}

	@Override
	public void deletePost(String postId) throws QException {
		LOGGER.log(Level.FINE, "Deleting post with id {0}", postId);
		BlgPost blgPost = BlgPost.find(em, postId);
		
		if (blgPost == null) {
			return;
		}

		em.remove(blgPost);
	}

	@Override
	public void archivePost(String postId) throws QException {
		LOGGER.log(Level.FINE, "Archiving post with id {0}", postId);
		BlgPost blgPost = BlgPost.find(em, postId);
		
		if (blgPost == null) {
			return;
		}

		blgPost.setArchived(true);
	}

	@Override
	public BlogPostDTO findPost(String postId) {
		LOGGER.log(Level.FINE, "Finding post with id {0}", postId);
		return ConverterUtil.postToPostDTO(BlgPost.find(em, postId));
	}

	@Override
	public List<BlogPostDTO> getPostsForBlog(String blogId,
			boolean includeNotPublished) throws QException {
		LOGGER.log(Level.FINEST,
				"Retrieving posts of blog with id {0} without paging params",
				blogId);
		return getPostsForBlog(blogId, null, includeNotPublished);
	}

	@Override
	public List<BlogPostDTO> getPostsForBlog(String blogId,
			PagingParams pagingParams, boolean includeNotPublished)
			throws QException {
		LOGGER.log(Level.FINEST,
				"Retrieving posts of blog with id {0} with paging params",
				blogId);

		List<BlogPostDTO> result = new ArrayList();

		List<BlgPost> queryResults = BlgPost.getPostsByBlog(em, blogId, includeNotPublished, pagingParams);

		for (BlgPost blgPost : queryResults)
			result.add(ConverterUtil.postToPostDTO(blgPost));

		return result;
	}

	@Override
	public List<BlogPostDTO> getBlogPostsByCategory(String blogId,
			String categoryId, boolean includeNotPublished) throws QException {
		LOGGER.log(
				Level.FINEST,
				"Retrieving posts of blog with id {0} having category with id {1}",
				new String[] { blogId, categoryId });

		List<BlogPostDTO> result = new ArrayList();

		List<BlgPost> queryResults = BlgPost.getPostsByBlogAndCategory(em, blogId, categoryId, includeNotPublished);

		for (BlgPost blgPost : queryResults)
			result.add(ConverterUtil.postToPostDTO(blgPost));

		return result;
	}

	@Override
	public List<BlogPostDTO> getBlogPostsByTag(String blogId, String tagId,
			boolean includeNotPublished) throws QException {
		LOGGER.log(Level.FINEST,
				"Retrieving posts of blog with id {0} having tag with id {1}",
				new String[] { blogId, tagId });

		List<BlogPostDTO> result = new ArrayList();

		List<BlgPost> queryResults = BlgPost.getPostsByBlogAndTag(em, blogId, tagId, includeNotPublished);

		for (BlgPost blgPost : queryResults)
			result.add(ConverterUtil.postToPostDTO(blgPost));

		return result;
	}

	@Override
	public List<BlogPostDTO> getBlogPostsByDate(String blogId, long startDate, boolean includeNotPublished) {

		List<BlogPostDTO> result = new ArrayList();

		List<BlgPost> queryResults = BlgPost.getPostsByBlogAndStartDate(em, blogId, startDate, includeNotPublished);

		for (BlgPost blgPost : queryResults)
			result.add(ConverterUtil.postToPostDTO(blgPost));

		return result;
	}
	
	@Override
	public BlogPostDTO getPostByName(String name) {
		LOGGER.log(Level.FINEST, "Retrieving post with name {0}", name);

		return ConverterUtil.postToPostDTO(BlgPost.findByName(em, name));
	}

	public List<BlgTrackbacks> sendTrackback(List<String> trackbackPostIds, String postId, String excerpt, String userId) {
		LOGGER.log(Level.FINE, "Creating trackback for post {0}", postId);
		List<BlgTrackbacks> blgTrackbacksList = null;
		if (!trackbackPostIds.isEmpty()) {
			blgTrackbacksList = new ArrayList<>();
			BlgPost post = BlgPost.find(em, postId);
			for (String trackbackPostId : trackbackPostIds) {
				BlgPost trackBackPost =  BlgPost.find(em, trackbackPostId);
				LOGGER.log(Level.FINEST, "trackbackPostId {0}", trackbackPostId);
				// invalid trackback url , so ignore it.
				if (trackBackPost != null && trackBackPost.isCommentsEnabled()) {
					BlgComment comment = new BlgComment();
					comment.setBody(excerpt);
					comment.setDateCommented(new Date().getTime());
					comment.setPostId(trackBackPost);
					comment.setUserId(userId);
					em.persist(comment);
					LOGGER.log(Level.FINEST, "comment id:{0}", comment.getId());
					
					BlgTrackbacks blgTrackbacks = new BlgTrackbacks();
					blgTrackbacks.setTrackbackPostId(trackBackPost);
					blgTrackbacks.setPostId(post);
					blgTrackbacks.setBlgCommentId(comment);
					em.persist(blgTrackbacks);
					blgTrackbacksList.add(blgTrackbacks);
				}
			}// end for
		} // end if trackbackPostIds.isEmpty()
		return blgTrackbacksList;
	}
}
