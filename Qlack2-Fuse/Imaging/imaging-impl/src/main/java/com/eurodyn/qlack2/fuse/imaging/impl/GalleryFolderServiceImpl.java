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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.eurodyn.qlack2.common.search.PagingParams;
import com.eurodyn.qlack2.fuse.imaging.api.GalleryFolderService;
import com.eurodyn.qlack2.fuse.imaging.api.GalleryImageService;
import com.eurodyn.qlack2.fuse.imaging.api.dto.FolderDTO;
import com.eurodyn.qlack2.fuse.imaging.api.dto.ImageAttributeDTO;
import com.eurodyn.qlack2.fuse.imaging.api.exception.QFolderCannotBeRetrievedException;
import com.eurodyn.qlack2.fuse.imaging.impl.model.Folder;
import com.eurodyn.qlack2.fuse.imaging.impl.model.FolderAttribute;
import com.eurodyn.qlack2.fuse.imaging.impl.model.Image;
import com.eurodyn.qlack2.fuse.imaging.impl.util.ConverterUtil;

/**
 *
 * @author European Dynamics SA
 */
public class GalleryFolderServiceImpl implements GalleryFolderService {
	private static final Logger LOGGER = Logger
			.getLogger(GalleryFolderServiceImpl.class.getName());
	@PersistenceContext(unitName = "QlackFuse-Imaging-PU")
	private EntityManager em;
	private GalleryImageService galleryImageService;

	public void setEm(EntityManager em) {
		this.em = em;
	}

	public void setGalleryImageService(GalleryImageService galleryImageService) {
		this.galleryImageService = galleryImageService;
	}

	@Override
	public FolderDTO createFolder(FolderDTO folder) {
		LOGGER.log(Level.FINEST, "Creating folder with name {0}",
				folder.getName());

		Folder folderEntity = new Folder();
		folderEntity.setName(folder.getName());
		folderEntity.setDescription(folder.getDescription());
		folderEntity.setOwnerId(folder.getOwnerId());
		if (folder.getParentId() != null) {
			Folder parentFolder = em.find(Folder.class, folder.getParentId());
			folderEntity.setParentId(parentFolder);
		}

		em.persist(folderEntity);
		folder.setId(folderEntity.getId());

		if (folder.getAttributes() != null) {
			for (ImageAttributeDTO ImageAttributeDTO : folder.getAttributes()) {
				FolderAttribute attributeEntity = new FolderAttribute();
				attributeEntity.setFolderId(folderEntity);
				attributeEntity.setName(ImageAttributeDTO.getName());
				attributeEntity.setValue(ImageAttributeDTO.getValue());
				em.persist(attributeEntity);
			}
		}

		return folder;
	}

	@Override
	public void updateFolder(FolderDTO folder) {
		LOGGER.log(Level.FINEST, "Updating folder with id {0}", folder.getId());

		Folder folderEntity = em.find(Folder.class, folder.getId());
		String originalOwner = folderEntity.getOwnerId();

		if (folder.getName() != null) {
			folderEntity.setName(folder.getName());
		}
		if (folder.getDescription() != null) {
			folderEntity.setDescription(folder.getDescription());
		}
		if (folder.getOwnerId() != null) {
			folderEntity.setOwnerId(folder.getOwnerId());
		}
	}

	@Override
	public void deleteFolder(FolderDTO folder) {
		LOGGER.log(Level.FINEST, "Deleting folder with id {0}", folder.getId());

		Folder folderEntity = em.find(Folder.class, folder.getId());
		String parentId = null;
		if (folderEntity.getParentId() != null) {
			parentId = folderEntity.getParentId().getId();
		}
		em.remove(folderEntity);
	}

	@Override
	public ImageAttributeDTO addFolderAttribute(String folderId,
			ImageAttributeDTO attribute) {
		LOGGER.log(Level.FINEST, "Adding attribute {0} to folder with id {0}",
				new String[] { attribute.getName(), folderId });

		Folder folder = em.find(Folder.class, folderId);

		FolderAttribute attributeEntity = new FolderAttribute();
		attributeEntity.setName(attribute.getName());
		attributeEntity.setValue(attribute.getValue());
		attributeEntity.setFolderId(folder);
		em.persist(attributeEntity);

		attribute.setId(attributeEntity.getId());

		return attribute;
	}

	@Override
	public void updateFolderAttribute(ImageAttributeDTO attribute) {
		LOGGER.log(Level.FINEST, "Updating folder attribute with id {0}",
				attribute.getId());

		FolderAttribute attributeEntity = em.find(FolderAttribute.class,
				attribute.getId());
		if (attribute.getName() != null) {
			attributeEntity.setName(attribute.getName());
		}
		if (attribute.getValue() != null) {
			attributeEntity.setValue(attribute.getValue());
		}
	}

	@Override
	public void deleteFolderAttribute(ImageAttributeDTO attribute) {
		LOGGER.log(Level.FINEST, "Deleting folder attribute with id {0}",
				attribute.getId());

		FolderAttribute attributeEntity = em.find(FolderAttribute.class,
				attribute.getId());

		em.remove(attributeEntity);
	}

	@Override
	public ImageAttributeDTO getFolderAttribute(String attributeId) {
		LOGGER.log(Level.FINEST, "Retrieving folder attribute with id {0}",
				attributeId);
		return ConverterUtil.convertToImageAttributeDTO(em.find(
				FolderAttribute.class, attributeId));
	}

	@Override
	public ImageAttributeDTO getFolderAttribute(String folderId,
			String attributeName) {
		LOGGER.log(Level.FINEST,
				"Retrieving attribute of folder {0} with name {1}",
				new String[] { folderId, attributeName });

		return ConverterUtil.convertToImageAttributeDTO(FolderAttribute
				.findByNameAndFolderID(em, attributeName, folderId));
	}

	@Override
	public FolderDTO getFolder(String folderId) {
		LOGGER.log(Level.FINEST, "Retrieving folder with id {0}", folderId);

		Folder folder = em.find(Folder.class, folderId);
		return ConverterUtil.convertToFolderDTO(folder);
	}

	@Override
	public FolderDTO getFolderByName(String name) {
		LOGGER.log(Level.FINEST, "Retrieving folder with name {0}", name);

		return getFolderByName(name, null);
	}

	@Override
	public FolderDTO getFolderByName(String name, String parentId) {
		LOGGER.log(Level.FINEST,
				"Retrieving folder with name {0}, parentId = {1}",
				new String[] { name, parentId });

		return  ConverterUtil.convertToFolderDTO(Folder.findByName(em, name, parentId));
	}

	private List<FolderDTO> getChildFolders(FolderDTO folder,
			PagingParams pagingParams, String searchTerm,
			ImageAttributeDTO searchAttribute) {
		List<FolderDTO> result = new ArrayList<FolderDTO>();

		String query = "SELECT f FROM Folder f";
		if (searchAttribute != null) {
			query = query.concat(" JOIN f.folderAttributes a");
		}
		query = query.concat(" WHERE f.parentId");
		if (folder.getParentId() == null) {
			query = query.concat(" IS NULL ");
		} else {
			query = query.concat(".id = :parentId ");
		}
		if (folder.getOwnerId() != null) {
			query = query.concat("AND f.ownerId = :ownerId ");
		}
		if (searchTerm != null) {
			query = query.concat("AND f.name like :searchTerm ");
		}
		if (searchAttribute != null) {
			query = query
					.concat("AND a.name like :attributeName AND a.value like :attributeValue");
		}

		Query q = em.createQuery(query);
		if (folder.getParentId() != null) {
			q.setParameter("parentId", folder.getParentId());
		}
		if (folder.getOwnerId() != null) {
			q.setParameter("ownerId", folder.getOwnerId());
		}
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

		List<Folder> queryResult = q.getResultList();
		if ((queryResult != null) && (queryResult.size() > 0)) {
			for (Folder folderEntity : queryResult) {
				result.add(ConverterUtil.convertToFolderDTO(folderEntity));
			}
		}

		return result;
	}

	@Override
	public List<FolderDTO> getChildFolders(FolderDTO folder,
			PagingParams pagingParams) {
		LOGGER.log(
				Level.FINEST,
				"Retrieving folders contained in folder with id {0} "
						+ "having user with id {1} as owner - using paging parameters",
				new String[] { folder.getParentId(), folder.getOwnerId() });

		return getChildFolders(folder, pagingParams, null, null);
	}

	@Override
	public List<FolderDTO> getChildFolders(FolderDTO folder,
			PagingParams pagingParams, String searchTerm) {
		LOGGER.log(Level.FINEST,
				"Retrieving folders contained in folder with id {0} "
						+ "having user with id {1} as owner, searchTerm = {2}",
				new String[] { folder.getParentId(), folder.getOwnerId(),
						searchTerm });

		return getChildFolders(folder, pagingParams, searchTerm, null);
	}

	@Override
	public List<FolderDTO> getChildFolders(FolderDTO folder,
			PagingParams pagingParams, ImageAttributeDTO searchAttribute) {
		LOGGER.log(
				Level.FINEST,
				"Retrieving folders contained in folder with id {0} "
						+ "having user with id {1} as owner, searchAttribute = [{2}, {3}]",
				new String[] { folder.getParentId(), folder.getOwnerId(),
						searchAttribute.getName(), searchAttribute.getValue() });

		return getChildFolders(folder, pagingParams, null, searchAttribute);
	}

	@Override
	public byte[] getFolderAsZip(String folderId, boolean includeAttributes,
			boolean isDeep) throws QFolderCannotBeRetrievedException {
		LOGGER.log(Level.FINEST,
				"Retrieving contents of folder with id {0} as zip. includeAttributes = {1}, "
						+ "isDeep = {2}",
				new String[] { folderId, String.valueOf(includeAttributes),
						String.valueOf(isDeep) });

		byte[] retVal = null;
		Folder folder = em.find(Folder.class, folderId);
		String folderName = folder.getName();

		boolean hasEntries = false;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ZipOutputStream out = new ZipOutputStream(outStream);

		try {
			Set<Image> images = folder.getImgImages();
			if (images != null) {
				for (Image image : images) {
					byte[] imageRetVal = galleryImageService.getImageAsZip(
							image.getId(), includeAttributes);
					if (imageRetVal != null) {
						hasEntries = true;
						ZipEntry entry = new ZipEntry(image.getName() + ".zip");
						out.putNextEntry(entry);
						out.write(imageRetVal, 0, imageRetVal.length);
					}
				}
			}

			if (isDeep) {
				if (folder.getChildren() != null) {
					for (Folder childFolder : folder.getChildren()) {
						byte[] folderRetVal = getFolderAsZip(
								childFolder.getId(), includeAttributes, isDeep);
						if (folderRetVal != null) {
							hasEntries = true;
							ZipEntry entry = new ZipEntry(childFolder.getName()
									+ ".zip");
							out.putNextEntry(entry);
							out.write(folderRetVal, 0, folderRetVal.length);
						}
					}
				}
			}

			if (includeAttributes) {
				hasEntries = true;
				ZipEntry entry = new ZipEntry(folderName + ".properties");
				out.putNextEntry(entry);
				String properties = new String();
				Iterator pi = folder.getFolderAttributes().iterator();
				while (pi.hasNext()) {
					FolderAttribute attribute = (FolderAttribute) pi.next();
					properties += attribute.getName() + " = "
							+ attribute.getValue() + "\n";
				}
				out.write(properties.getBytes());
			}
			if (hasEntries) {
				out.close();
				retVal = outStream.toByteArray();
			}
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			throw new QFolderCannotBeRetrievedException(
					ex.getLocalizedMessage());
		}

		return retVal;
	}

}
