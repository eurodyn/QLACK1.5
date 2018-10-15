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
package com.eurodyn.qlack2.fuse.cm.impl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.eurodyn.qlack2.fuse.cm.api.dto.BinContentDTO;
import com.eurodyn.qlack2.fuse.cm.api.dto.FileDTO;
import com.eurodyn.qlack2.fuse.cm.api.dto.FolderDTO;
import com.eurodyn.qlack2.fuse.cm.api.dto.NodeDTO;
import com.eurodyn.qlack2.fuse.cm.api.dto.VersionDTO;
import com.eurodyn.qlack2.fuse.cm.api.util.CMConstants;
import com.eurodyn.qlack2.fuse.cm.impl.model.Node;
import com.eurodyn.qlack2.fuse.cm.impl.model.NodeAttribute;
import com.eurodyn.qlack2.fuse.cm.impl.model.NodeType;
import com.eurodyn.qlack2.fuse.cm.impl.model.Version;
import com.eurodyn.qlack2.fuse.cm.impl.model.VersionAttribute;

public class ConverterUtil {
	public static FolderDTO nodeToFolderDTO(Node entity, boolean lazyRelatives) {
		if (entity == null) {
			return null;
		}
		
		FolderDTO dto = new FolderDTO();
		initNodeDTO(dto, entity, lazyRelatives);
		if (!lazyRelatives) {
			dto.setChildren(new HashSet<NodeDTO>());
    		for (Node child : entity.getChildren()) {
    			switch (child.getType()) {
    			case FOLDER:
    				dto.getChildren().add(nodeToFolderDTO(child, lazyRelatives));
    				break;
    			case FILE:
    				dto.getChildren().add(nodeToFileDTO(child, lazyRelatives));
    				break;
    			}
    		}
		}
		return dto;
	}

	/**
	 * Convert a Node entity to a FileDTO; this method <b>does not</b> convert
	 * also the file versions.
	 * @param entity
	 * @param lazyRelatives
	 * @return
	 */
	public static FileDTO nodeToFileDTO(Node entity, boolean lazyRelatives) {
		if (entity == null) {
			return null;
		}
		
		FileDTO dto = new FileDTO();
		initNodeDTO(dto, entity, lazyRelatives);
		
		return dto;
	}
	
	public static NodeDTO nodeToNodeDTO(Node entity) {
		NodeDTO dto = new NodeDTO();
		initNodeDTO(dto, entity, false);
		return dto;
	}

	private static void initNodeDTO(NodeDTO dto, Node entity, boolean lazyRelatives) {
		dto.setId(entity.getId());
		
		if (!lazyRelatives) {
			if(entity.getParent() != null) {
			dto.setParentId(entity.getParent().getId());}
		}
		dto.setLocked(entity.getLockToken() != null);
		dto.setCreatedOn(entity.getCreatedOn());
	
		for (NodeAttribute attrEntity : entity.getAttributes()) {
			switch (attrEntity.getName()) {

			case CMConstants.ATTR_NAME:
				dto.setName(attrEntity.getValue());
				break;

			case CMConstants.ATTR_CREATED_BY:
				dto.setCreatedBy(attrEntity.getValue());
				break;

			case CMConstants.ATTR_LAST_MODIFIED_ON:
				dto.setCreatedOn(Long.parseLong(attrEntity.getValue()));
				break;

			case CMConstants.ATTR_LAST_MODIFIED_BY:
				dto.setCreatedBy(attrEntity.getValue());
				break;

			case CMConstants.ATTR_LOCKED_ON:
				dto.setCreatedOn(Long.parseLong(attrEntity.getValue()));
				break;

			case CMConstants.ATTR_LOCKED_BY:
				dto.setCreatedBy(attrEntity.getValue());
				break;
			default:
				if (dto.getAttributes() == null) {
					dto.setAttributes(new HashMap<String, String>());
				}
				dto.getAttributes().put(attrEntity.getName(), attrEntity.getValue());
				break;
			}
		}
	}
	
	public static VersionDTO versionToVersionDTO(Version entity) {
		if (entity == null) {
			return null;
		}
		
		VersionDTO dto = new VersionDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setCreatedOn(entity.getCreatedOn());
		
		for (VersionAttribute attrEntity : entity.getAttributes()) {
			switch (attrEntity.getName()) {

			case CMConstants.ATTR_NAME:
				dto.setName(attrEntity.getValue());
				break;

			case CMConstants.ATTR_CREATED_BY:
				dto.setCreatedBy(attrEntity.getValue());
				break;

			case CMConstants.ATTR_LAST_MODIFIED_ON:
				dto.setCreatedOn(Long.parseLong(attrEntity.getValue()));
				break;

			case CMConstants.ATTR_LAST_MODIFIED_BY:
				dto.setCreatedBy(attrEntity.getValue());
				break;

			case CMConstants.ATTR_LOCKED_ON:
				dto.setCreatedOn(Long.parseLong(attrEntity.getValue()));
				break;

			case CMConstants.ATTR_LOCKED_BY:
				dto.setCreatedBy(attrEntity.getValue());
				break;
			default:
				if (dto.getAttributes() == null) {
					dto.setAttributes(new HashMap<String, String>());
				}
				dto.getAttributes().put(attrEntity.getName(), attrEntity.getValue());
				break;
			}
		}
		return dto;
	}
	
	public static List<VersionDTO> versionToVersionDTOList(List<Version> entities) {
		if (entities == null) {
			return null;
		}
		
		List<VersionDTO> dtos = new ArrayList<>();
		for (Version entity : entities) {
			dtos.add(versionToVersionDTO(entity));
		}
		return dtos;
	}

	public static Node nodeDTOToNode(NodeDTO dto) {
		if (dto == null) {
			return null;
		}
		
		Node entity = new Node();
		if (dto instanceof FolderDTO) {
			entity.setType(NodeType.FOLDER);
		} else if (dto instanceof FileDTO) {
			entity.setType(NodeType.FILE);
		}
		entity.setAttributes(new ArrayList<NodeAttribute>());

		entity.getAttributes().add(new NodeAttribute(CMConstants.ATTR_NAME, dto.getName(), entity));
		if (dto.getAttributes() != null) {
			for (String attrName : dto.getAttributes().keySet()) {
				entity.getAttributes().add(new NodeAttribute(attrName, dto.getAttributes().get(attrName), entity));
			}
		}
		return entity;
	}

	public static BinContentDTO versionToBinContentDTO(Version entity) {
		if (entity == null) {
			return null;
		}
		
		BinContentDTO dto = new BinContentDTO();
		dto.setFilename(entity.getFilename());
		dto.setMimetype(entity.getMimetype());
		dto.setContent(entity.getContent());
		return dto;
	}
}
