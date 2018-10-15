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
package com.eurodyn.qlack2.fuse.imaging.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;

import com.eurodyn.qlack2.common.search.PagingParams;
import com.eurodyn.qlack2.fuse.imaging.api.GalleryImageService;
import com.eurodyn.qlack2.fuse.imaging.api.ImageService;
import com.eurodyn.qlack2.fuse.imaging.api.criteria.ImageSearchCriteria;
import com.eurodyn.qlack2.fuse.imaging.api.criteria.ImageSearchCriteria.ImageAttributeCriteria;
import com.eurodyn.qlack2.fuse.imaging.api.dto.ImageAttributeDTO;
import com.eurodyn.qlack2.fuse.imaging.api.dto.ImageDTO;
import com.eurodyn.qlack2.fuse.imaging.api.dto.ImageInfo;
import com.eurodyn.qlack2.fuse.imaging.api.exception.QImageCannotBeRetrievedException;
import com.eurodyn.qlack2.fuse.imaging.impl.model.Folder;
import com.eurodyn.qlack2.fuse.imaging.impl.model.Image;
import com.eurodyn.qlack2.fuse.imaging.impl.model.ImageAttribute;
import com.eurodyn.qlack2.fuse.imaging.impl.util.ConverterUtil;
import com.eurodyn.qlack2.fuse.imaging.impl.util.ImageSearchHelper;

/**
 *
 * @author European Dynamics SA
 */
public class GalleryImageServiceImpl implements GalleryImageService {
	private static final Logger LOGGER = Logger
			.getLogger(GalleryImageServiceImpl.class.getName());
	@PersistenceContext(unitName = "QlackFuse-Imaging-PU")
	private EntityManager em;
	private ImageService imageService;
	private int thumbnailHeight;
	private int thumbnailWidth;

	public void setThumbnailHeight(int thumbnailHeight) {
		this.thumbnailHeight = thumbnailHeight;
	}

	public void setThumbnailWidth(int thumbnailWidth) {
		this.thumbnailWidth = thumbnailWidth;
	}

	public void setEm(EntityManager em) {
		this.em = em;
	}

	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}

	private byte[] getThumbnail(byte[] image) {
		byte[] thumbnail = image;

		ImageInfo imageInfo = imageService.getImageInfo(image);
		int imageWidth = imageInfo.getWidth();
		int imageHeight = imageInfo.getHeight();
		if ((imageWidth > thumbnailWidth) || (imageHeight > thumbnailHeight)) {
			// Scale the image so that its dimensions are smaller or equal than
			// the specified
			// thumbnail dimensions, while preserving the image aspect ratio
			float xFactor = (float) thumbnailWidth / (float) imageWidth;
			float yFactor = (float) thumbnailHeight / (float) imageHeight;
			float scaleFactor = Math.min(xFactor, yFactor);
			thumbnail = imageService.scaleImage(image, scaleFactor);
		}

		return thumbnail;
	}

	@Override
	public ImageDTO createImage(ImageDTO image) {
		LOGGER.log(Level.FINEST, "Creating image with name {0}",
				image.getName());

		Image imageEntity = ConverterUtil.convertToImageEntity(image);
		Folder folder = em.find(Folder.class, image.getFolderId());
		imageEntity.setFolderId(folder);
		imageEntity.setThumbnail(getThumbnail(image.getImage()));

		em.persist(imageEntity);
		image.setId(imageEntity.getId());

		if (image.getAttributes() != null) {
			for (ImageAttributeDTO ImageAttributeDTO : image.getAttributes()) {
				ImageAttribute attributeEntity = new ImageAttribute();
				attributeEntity.setImageId(imageEntity);
				attributeEntity.setName(ImageAttributeDTO.getName());
				attributeEntity.setValue(ImageAttributeDTO.getValue());
				em.persist(attributeEntity);
			}
		}

		return image;
	}

	@Override
	public void updateImage(ImageDTO image) {
		LOGGER.log(Level.FINEST, "Updating image with id {0}", image.getId());

		Image imageEntity = em.find(Image.class, image.getId());
		if (image.getName() != null) {
			imageEntity.setName(image.getName());
		}
		if (image.getDescription() != null) {
			imageEntity.setDescription(image.getDescription());
		}
		if (image.getOwnerId() != null) {
			imageEntity.setOwnerId(image.getOwnerId());
		}
		if (image.getFilename() != null) {
			imageEntity.setFilename(image.getFilename());
		}
		if (image.getMimetype() != null) {
			imageEntity.setMimetype(image.getMimetype());
		}
		if (image.getImage() != null) {
			imageEntity.setThumbnail(getThumbnail(image.getImage()));
			imageEntity.setContent(image.getImage());
		}
	}

	@Override
	public void deleteImage(ImageDTO image) {
		LOGGER.log(Level.FINEST, "Deleting image with id {0}", image.getId());
		Image imageEntity = em.find(Image.class, image.getId());
		em.remove(imageEntity);
	}

	@Override
	public ImageAttributeDTO addImageAttribute(String imageId,
			ImageAttributeDTO attribute) {
		LOGGER.log(Level.FINEST, "Adding attribute {0} to image with id {1}",
				new String[] { attribute.getName(), imageId });

		Image image = em.find(Image.class, imageId);

		ImageAttribute attributeEntity = new ImageAttribute();
		attributeEntity.setName(attribute.getName());
		attributeEntity.setValue(attribute.getValue());
		attributeEntity.setImageId(image);
		em.persist(attributeEntity);

		attribute.setId(attributeEntity.getId());

		return attribute;
	}

	@Override
	public void updateImageAttribute(ImageAttributeDTO attribute) {
		LOGGER.log(Level.FINEST, "Updating image attribute with id {0}",
				attribute.getId());

		ImageAttribute attributeEntity = em.find(ImageAttribute.class,
				attribute.getId());
		if (attribute.getName() != null) {
			attributeEntity.setName(attribute.getName());
		}
		if (attribute.getValue() != null) {
			attributeEntity.setValue(attribute.getValue());
		}
	}

	@Override
	public void deleteImageAttribute(ImageAttributeDTO attribute) {
		LOGGER.log(Level.FINEST, "Deleting image attribute with id {0}",
				attribute.getId());

		ImageAttribute attributeEntity = em.find(ImageAttribute.class,
				attribute.getId());

		em.remove(attributeEntity);
	}

	@Override
	public ImageAttributeDTO getImageAttribute(String attributeId) {
		LOGGER.log(Level.FINEST, "Retrieving image attribute with id {0}",
				attributeId);
		return ConverterUtil.convertToImageAttributeDTO(em.find(
				ImageAttribute.class, attributeId));
	}

	@Override
	public ImageAttributeDTO getImageAttribute(String imageId,
			String attributeName) {
		LOGGER.log(Level.FINEST,
				"Retrieving attribute of image {0} with name {1}",
				new String[] { imageId, attributeName });

		return ConverterUtil.convertToImageAttributeDTO(ImageAttribute.findByNameAndImageID(em, attributeName, imageId));
	}

	@Override
	public ImageDTO getImage(String imageId, int imageContent) {
		LOGGER.log(Level.FINEST,
				"Retrieving image with id {0}, imageContent = {1}",
				new String[] { imageId, String.valueOf(imageContent) });

		Image imageEntity = em.find(Image.class, imageId);
		ImageDTO image = ConverterUtil.convertToImageDTO(imageEntity, imageContent);

		return image;
	}

	@Override
	public byte[] getImageAsZip(String imageId, boolean includeAttributes)
			throws QImageCannotBeRetrievedException {
		LOGGER.log(
				Level.FINEST,
				"Retrieving image with id {0} as zip. includeAttributes = {1}.",
				new String[] { imageId, String.valueOf(includeAttributes) });

		byte[] retVal = null;
		Image image = em.find(Image.class, imageId);

		try {
			ByteArrayOutputStream fileStream = new ByteArrayOutputStream();
			ZipOutputStream file = new ZipOutputStream(fileStream);
			byte[] binContent = image.getContent();
			ZipEntry entry = new ZipEntry(image.getFilename());
			file.putNextEntry(entry);
			file.write(binContent);

			if (includeAttributes) {
				ZipEntry attributeEntry = new ZipEntry(image.getName()
						+ ".properties");
				file.putNextEntry(attributeEntry);
				Iterator pi = image.getImageAttributes().iterator();
				StringBuffer buf = new StringBuffer();
				while (pi.hasNext()) {
					ImageAttribute attribute = (ImageAttribute) pi.next();
					buf.append(attribute.getName() + " = "
							+ attribute.getValue() + "\n");
				}
				String attributes = buf.toString();
				file.write(attributes.getBytes());
			}
			file.close();
			retVal = fileStream.toByteArray();
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			throw new QImageCannotBeRetrievedException(ex.getLocalizedMessage());
		}

		return retVal;
	}

	@Override
	public ImageInfo getImageInfo(String imageId) throws QImageCannotBeRetrievedException {
		LOGGER.log(Level.FINEST,
				"Retrieving the information of image with id {0}", imageId);

		Image imageEntity = em.find(Image.class, imageId);
		if (imageEntity == null) {
			return null;
		}
		return imageService.getImageInfo(imageEntity.getContent());
	}

	private List<ImageDTO> getImagesInFolder(String folderId, int imageContent,
			PagingParams pagingParams, String searchTerm,
			ImageAttributeDTO searchAttribute) {
		List<ImageDTO> result = new ArrayList<ImageDTO>();

		String query = "SELECT i FROM Image i";
		if (searchAttribute != null) {
			query = query.concat(" JOIN i.imageAttributes a");
		}
		query = query.concat(" WHERE i.folderId.id = :folderId ");
		if (searchTerm != null) {
			query = query.concat("AND i.name like :searchTerm");
		}
		if (searchAttribute != null) {
			query = query
					.concat("AND a.name like :attributeName AND a.value like :attributeValue");
		}

		Query q = em.createQuery(query);
		q.setParameter("folderId", folderId);
		if (searchTerm != null) {
			q.setParameter("searchTerm", "%" + searchTerm + "%");
		}
		if (searchAttribute != null) {
			q.setParameter("attributeName", "%" + searchAttribute.getName()
					+ "%");
			q.setParameter("attributeValue", "%" + searchAttribute.getValue()
					+ "%");
		}
		if ((pagingParams != null) && (pagingParams.getCurrentPage() > -1)) {
			q.setFirstResult((pagingParams.getCurrentPage() - 1)
					* pagingParams.getPageSize());
			q.setMaxResults(pagingParams.getPageSize());
		}
		List<Image> queryResult = q.getResultList();
		if (queryResult != null) {
			for (Image imageEntity : queryResult) {
				result.add(ConverterUtil.convertToImageDTO(imageEntity, imageContent));
			}
		}

		return result;
	}

	@Override
	public List<ImageDTO> getImagesInFolder(String folderId, int imageContent,
			PagingParams pagingParams) {
		LOGGER.log(Level.FINEST,
				"Retrieving images in folder with id {0}, imageContent = {1}",
				new String[] { folderId, String.valueOf(imageContent) });

		return getImagesInFolder(folderId, imageContent, pagingParams, null,
				null);
	}

	@Override
	public List<ImageDTO> getImagesInFolder(String folderId, int imageContent,
			PagingParams pagingParams, String searchTerm) {
		LOGGER.log(Level.FINEST,
				"Retrieving images in folder with id {0}, imageContent = {1}"
						+ " searchTerm = {2}",
				new String[] { folderId, String.valueOf(imageContent),
						searchTerm });

		return getImagesInFolder(folderId, imageContent, pagingParams,
				searchTerm, null);
	}

	@Override
	public List<ImageDTO> getImagesInFolder(String folderId, int imageContent,
			PagingParams pagingParams, ImageAttributeDTO searchAttribute) {
		LOGGER.log(Level.FINEST,
				"Retrieving images in folder with id {0}, imageContent = {1}"
						+ " searchAttribute = [{2}, {3}]",
				new String[] { folderId, String.valueOf(imageContent),
						searchAttribute.getName(), searchAttribute.getValue() });

		return getImagesInFolder(folderId, imageContent, pagingParams, null,
				searchAttribute);
	}

	@Override
	public List<ImageDTO> findImages(ImageSearchCriteria criteria,
			int imageContent) {
		List<ImageDTO> result = new ArrayList<>();
		
		// ImageSearchHelper used in order to allow including the sort criterion
		// in the select clause which is required by certain DBs such as PostgreSQL and H2.
		// Using a separate class for the CriteriaQuery allows us to to a cq.multiselect when
		// applying sorting criteria (see below) since this requires the object being returned by
		// the query to have a two-argument constructor accepting the values passed in the
		// multiselect. Additionally, the ImageSearchHelper allows us to also pass the imageContent
		// flag directly to the converter simply by including it in the multiselect.
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<ImageSearchHelper> cq = cb.createQuery(
				ImageSearchHelper.class).distinct(true);
		Root<Image> root = cq.from(Image.class);
		
		cq = applySearchCriteria(cb, cq, root, criteria);
		
		// Apply sorting. The sort criterion should be included in the select
		// clause in order to support certain DBs such as PostgreSQL and H2
		Expression orderExpr = null;
		if (criteria.getSortColumn() != null) {
			cq.multiselect(root, root.get(criteria.getSortColumn()), cb.literal(imageContent));
			orderExpr = root.get(criteria.getSortColumn());
		} else if (criteria.getSortAttribute() != null) {
			Join<Image, ImageAttribute> join = root.joinSet("imageAttributes",
					JoinType.LEFT);
			cq.multiselect(root, join.get("data"), cb.literal(imageContent));
			Predicate pr = cb.equal(join.get("name"),
					criteria.getSortAttribute());
			cq = addPredicate(cq, cb, pr);
			orderExpr = join.get("value");
		}
		Order order = null;
		if (criteria.isAscending()) {
			order = cb.asc(orderExpr);
		} else {
			order = cb.desc(orderExpr);
		}
		cq = cq.orderBy(order);
		
		TypedQuery<ImageSearchHelper> query = em.createQuery(cq);

		// Apply pagination
		if (criteria.getPaging() != null
				&& criteria.getPaging().getCurrentPage() > -1) {
			query.setFirstResult((criteria.getPaging().getCurrentPage() - 1)
					* criteria.getPaging().getPageSize());
			query.setMaxResults(criteria.getPaging().getPageSize());
		}

		for (ImageSearchHelper helper : query.getResultList()) {
			result.add(helper.getImageDTO());
		}
		
		return result;
	}

	@Override
	public String copyImage(ImageDTO image) {
		LOGGER.log(Level.FINEST,
				"Copying image with id {0} to folder with id {1}",
				new String[] { image.getId(), image.getFolderId() });

		ImageDTO oldImage = getImage(image.getId(), IMAGE_CONTENT_NONE);
		Image oldImageEntity = em.find(Image.class, image.getId());

		Image newImageEntity = ConverterUtil.convertToImageEntity(oldImage);
		if (image.getName() != null) {
			newImageEntity.setName(image.getName());
		} 
		if (image.getDescription() != null) {
			newImageEntity.setDescription(image.getDescription());
		}

		Folder newFolder = em.find(Folder.class, image.getFolderId());
		newImageEntity.setFolderId(newFolder);

		newImageEntity.setContent(oldImageEntity.getContent());
		newImageEntity.setThumbnail(oldImageEntity.getThumbnail());

		em.persist(newImageEntity);

		return newImageEntity.getId();
	}

	@Override
	public void moveImage(ImageDTO image) {
		LOGGER.log(Level.FINEST,
				"Moving image with id {0} to folder with id {1}", new String[] {
						image.getId(), image.getFolderId() });

		Image imageEntity = em.find(Image.class, image.getId());
		Folder newFolder = em.find(Folder.class, image.getFolderId());
		if (image.getName() != null) {
			imageEntity.setName(image.getName());
		}
		if (image.getDescription() != null) {
			imageEntity.setDescription(image.getDescription());
		}
		imageEntity.setFolderId(newFolder);
	}
	
	private <T> CriteriaQuery<T> applySearchCriteria(CriteriaBuilder cb,
			CriteriaQuery<T> query, Root<Image> root, ImageSearchCriteria criteria) {
		CriteriaQuery<T> cq = query;

		// Include/exclude user folder IDs
		if (criteria.getIncludeFolderIds() != null) {
			Predicate pr = root.get("folderId.id").in(criteria.getIncludeFolderIds());
			cq = addPredicate(cq, cb, pr);
		}
		if (criteria.getExcludeFolderIds() != null) {
			Predicate pr = cb.not(root.get("folderId.id").in(criteria.getExcludeFolderIds()));
			cq = addPredicate(cq, cb, pr);
		}
		// Filter by name
		if (criteria.getName() != null) {
			Predicate pr = cb.like(root.<String>get("name"), criteria.getName());
			cq = addPredicate(cq, cb, pr);
		}
		// Filter by filename
		if (criteria.getFilename() != null) {
			Predicate pr = cb.like(root.<String>get("filename"), criteria.getFilename());
			cq = addPredicate(cq, cb, pr);
		}
		// Filter by attributes
		if (criteria.getAttributeCriteria() != null) {
			cq = addPredicate(
					cq,
					cb,
					getAttributePredicate(cb, cq, root,
							criteria.getAttributeCriteria()));
		}
		return cq;
	}
	
	private <T> Predicate getAttributePredicate(CriteriaBuilder cb,
			CriteriaQuery<T> cq, Root<Image> root,
			ImageAttributeCriteria attCriteria) {
		Predicate attributePredicate = null;

		// Please note that the way UserAttributeCriteria are constructed by the
		// ImageSearchCriteriaBuilder they are guranteed to only have not null 
		// attributes OR not null attCriteria (not both).
		// The code below however uses two separate if statements instead of an
		// if/else in order to be able to handle a future modification of the
		// UserAttributeCriteria which will allow both properties to be not null.
		if (attCriteria.getAttributes() != null) {
			// For each attribute construct a predicate of the type:
			// image.id IN (SELECT image FROM img_image_attribute WHERE name = ...
			// and value = ...
			// and join it with AND/OR (depending on the type of the attribute
			// criteria) with
			// other such predicates constructed in previous loops.
			for (ImageAttributeDTO attribute : attCriteria.getAttributes()) {
				Subquery<String> sq = cq.subquery(String.class);
				Root<ImageAttribute> sqRoot = sq.from(ImageAttribute.class);
				sq = sq.select(sqRoot.get("imageId").<String> get("id")); // field
																		// to
																		// map
																		// with
																		// main-query

				// Construct the WHERE clause of the subquery
				if (StringUtils.isNotBlank(attribute.getName())) {
					addPredicate(sq, cb,
							cb.equal(sqRoot.get("name"), attribute.getName()));
				}
				if (StringUtils.isNotBlank(attribute.getValue())) {
					addPredicate(sq, cb,
							cb.equal(sqRoot.get("value"), attribute.getValue()));
				}

				Predicate pr = root.get("id").in(sq);
				if (attributePredicate == null) {
					attributePredicate = pr;
				} else {
					switch (attCriteria.getType()) {
					case AND:
						attributePredicate = cb.and(attributePredicate, pr);
						break;
					case OR:
						attributePredicate = cb.or(attributePredicate, pr);
						break;
					}
				}
			}
		}
		if (attCriteria.getAttCriteria() != null) {
			// For each attribute criterion get the predicate corresponding to
			// it recursively
			// and then join it with AND/OR (depending on the type of the
			// attribute criteria) with
			// other such predicates retrieved in previous loops.
			for (ImageAttributeCriteria nestedCriteria : attCriteria
					.getAttCriteria()) {
				Predicate nestedPredicate = getAttributePredicate(cb, cq, root,
						nestedCriteria);
				if (attributePredicate == null) {
					attributePredicate = nestedPredicate;
				} else {
					switch (attCriteria.getType()) {
					case AND:
						attributePredicate = cb.and(attributePredicate,
								nestedPredicate);
						break;
					case OR:
						attributePredicate = cb.or(attributePredicate,
								nestedPredicate);
						break;
					}
				}
			}
		}

		return attributePredicate;
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
	
	private <T> Subquery<T> addPredicate(Subquery<T> query, CriteriaBuilder cb,
			Predicate pr) {
		Subquery<T> sq = query;
		if (sq.getRestriction() != null) {
			sq = sq.where(cb.and(sq.getRestriction(), pr));
		} else {
			sq = sq.where(pr);
		}
		return sq;
	}

}
