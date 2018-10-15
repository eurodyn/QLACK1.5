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
package com.eurodyn.qlack2.fuse.imaging.impl.util;

import java.util.ArrayList;
import java.util.List;

import com.eurodyn.qlack2.fuse.imaging.api.GalleryImageService;
import com.eurodyn.qlack2.fuse.imaging.api.dto.FolderDTO;
import com.eurodyn.qlack2.fuse.imaging.api.dto.ImageAttributeDTO;
import com.eurodyn.qlack2.fuse.imaging.api.dto.ImageDTO;
import com.eurodyn.qlack2.fuse.imaging.impl.model.Folder;
import com.eurodyn.qlack2.fuse.imaging.impl.model.FolderAttribute;
import com.eurodyn.qlack2.fuse.imaging.impl.model.Image;
import com.eurodyn.qlack2.fuse.imaging.impl.model.ImageAttribute;

/**
 * 
 * @author European Dynamics SA
 */
public class ConverterUtil {

	private ConverterUtil() {
	}

	/**
	 * Converts an ImgFolder object to a FolderDTO object carrying the same
	 * information
	 * 
	 * @param entity
	 *            The ImgFolder object to convert
	 * @return The new FolderDTO object
	 */
	public static FolderDTO convertToFolderDTO(Folder entity) {
		if (entity == null) {
			return null;
		}

		FolderDTO dto = new FolderDTO();

		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setDescription(entity.getDescription());
		dto.setOwnerId(entity.getOwnerId());
		if (entity.getParentId() != null) {
			dto.setParentId(entity.getParentId().getId());
		}
		if (entity.getFolderAttributes() != null) {
			List<ImageAttributeDTO> attributes = new ArrayList<ImageAttributeDTO>();
			for (FolderAttribute attributeEntity : entity
					.getFolderAttributes()) {
				ImageAttributeDTO ImageAttributeDTO = new ImageAttributeDTO(
						attributeEntity.getName(), attributeEntity.getValue());
				ImageAttributeDTO.setId(attributeEntity.getId());
				attributes.add(ImageAttributeDTO);
			}
			dto.setAttributes(attributes);
		}

		return dto;
	}

	/**
	 * Converts a FolderDTO object to an ImgFolder object. This method does not
	 * set the foreign key attributes of the resulting ImgFolder object
	 * (parentId and imgFolderAttributes).
	 * 
	 * @param dto
	 *            The FolderDTO object to convert
	 * @return The resulting ImgFolder object
	 */
	public static Folder convertToFolderEntity(FolderDTO dto) {
		if (dto == null) {
			return null;
		}

		Folder entity = new Folder();

		entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setOwnerId(dto.getOwnerId());

		return entity;
	}

	/**
	 * Converts an ImgImage object to an ImageDTO object carrying the same
	 * information. This method does not set the image property of the ImageDTO
	 * object.
	 * 
	 * @param entity
	 *            The ImgImage object to convert
	 * @return The new ImageDTO object
	 */
	public static ImageDTO convertToImageDTO(Image entity, int imageContent) {
		if (entity == null) {
			return null;
		}

		ImageDTO dto = new ImageDTO();

		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setDescription(entity.getDescription());
		dto.setFilename(entity.getFilename());
		dto.setMimetype(entity.getMimetype());
		dto.setFolderId(entity.getFolderId().getId());
		dto.setOwnerId(entity.getOwnerId());
		if (entity.getImageAttributes() != null) {
			List<ImageAttributeDTO> attributes = new ArrayList<ImageAttributeDTO>();
			for (ImageAttribute attributeEntity : entity
					.getImageAttributes()) {
				ImageAttributeDTO ImageAttributeDTO = new ImageAttributeDTO(
						attributeEntity.getName(), attributeEntity.getValue());
				ImageAttributeDTO.setId(attributeEntity.getId());
				attributes.add(ImageAttributeDTO);
			}
			dto.setAttributes(attributes);
		}
		
		switch (imageContent) {
		case GalleryImageService.IMAGE_CONTENT_NONE:
			break;
		case GalleryImageService.IMAGE_CONTENT_THUMBNAIL:
			dto.setImage(entity.getThumbnail());
			break;
		case GalleryImageService.IMAGE_CONTENT_FULLSIZE:
			dto.setImage(entity.getContent());
			break;
		}
		dto.setSize(entity.getContent().length);

		return dto;
	}

	/**
	 * Converts an ImageDTO object to an ImgImage object. This method does not
	 * set the foreign key attributes of the resulting ImgImage object (folderId
	 * and imgImageAttributes) or the thumbnail attribute.
	 * 
	 * @param dto
	 *            The ImageDTO object to convert
	 * @return The resulting ImgImage object
	 */
	public static Image convertToImageEntity(ImageDTO dto) {
		if (dto == null) {
			return null;
		}

		Image entity = new Image();
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setOwnerId(dto.getOwnerId());
		entity.setFilename(dto.getFilename());
		if (dto.getMimetype() != null) {
			entity.setMimetype(dto.getMimetype());
		} else {
			entity.setMimetype(ImageIOUtil.getMimeType(dto.getImage()));
		}
		entity.setContent(dto.getImage());

		return entity;
	}

	/**
	 * Converts an ImgFolderAttribute object to an ImageAttributeDTO object
	 * carrying the same information
	 * 
	 * @param entity
	 *            The ImgFoldereAttribute object to convert
	 * @return The new ImageAttributeDTO object
	 */
	public static ImageAttributeDTO convertToImageAttributeDTO(
			FolderAttribute entity) {
		if (entity == null) {
			return null;
		}

		ImageAttributeDTO dto = new ImageAttributeDTO();

		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setValue(entity.getValue());

		return dto;
	}

	/**
	 * Converts an ImgImageAttribute object to an ImageAttributeDTO object
	 * carrying the same information
	 * 
	 * @param entity
	 *            The ImgImageAttribute object to convert
	 * @return The new ImageAttributeDTO object
	 */
	public static ImageAttributeDTO convertToImageAttributeDTO(
			ImageAttribute entity) {
		if (entity == null) {
			return null;
		}

		ImageAttributeDTO dto = new ImageAttributeDTO();

		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setValue(entity.getValue());

		return dto;
	}
}
