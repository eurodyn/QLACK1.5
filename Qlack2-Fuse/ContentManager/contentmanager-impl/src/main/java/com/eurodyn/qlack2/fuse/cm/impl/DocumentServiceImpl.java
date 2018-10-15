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
package com.eurodyn.qlack2.fuse.cm.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.EntityManager;

import org.joda.time.DateTime;

import com.eurodyn.qlack2.fuse.cm.api.DocumentService;
import com.eurodyn.qlack2.fuse.cm.api.VersionService;
import com.eurodyn.qlack2.fuse.cm.api.dto.FileDTO;
import com.eurodyn.qlack2.fuse.cm.api.dto.FolderDTO;
import com.eurodyn.qlack2.fuse.cm.api.dto.NodeDTO;
import com.eurodyn.qlack2.fuse.cm.api.exception.QIOException;
import com.eurodyn.qlack2.fuse.cm.api.exception.QInvalidPathException;
import com.eurodyn.qlack2.fuse.cm.api.exception.QNodeLockException;
import com.eurodyn.qlack2.fuse.cm.impl.model.Node;
import com.eurodyn.qlack2.fuse.cm.impl.model.NodeAttribute;
import com.eurodyn.qlack2.fuse.cm.impl.model.NodeType;
import com.eurodyn.qlack2.fuse.cm.impl.util.Constants;
import com.eurodyn.qlack2.fuse.cm.impl.util.ConverterUtil;

public class DocumentServiceImpl implements DocumentService {
	private static final Logger LOGGER = Logger.getLogger(DocumentServiceImpl.class.getName());
	private EntityManager em;
	private VersionService versionService;

	public void setEm(EntityManager em) {
		this.em = em;
	}

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

	@Override
	public String createFolder(FolderDTO folder, String userID, String lockToken)
			throws QNodeLockException {
		Node parent = null;
		if (folder.getParentId() != null) {
			parent = Node.findFolder(folder.getParentId(), em);
		}

		// Check parent's lock status
		if ((parent != null) && (parent.getLockToken() != null)
				&& (!parent.getLockToken().equals(lockToken))) {
			throw new QNodeLockException(
					"Node with ID "
							+ parent.getId()
							+ " is locked and an "
							+ "invalid lock token was passed; a new child folder cannot be created");
		}

		Node folderEntity = ConverterUtil.nodeDTOToNode(folder);
		folderEntity.setParent(parent);

		// Set created / last modified information
		DateTime now = DateTime.now();
		folderEntity.setCreatedOn(now.getMillis());
		folderEntity.getAttributes().add(
				new NodeAttribute(Constants.ATTR_CREATED_BY, userID,
						folderEntity));
		folderEntity.getAttributes().add(
				new NodeAttribute(Constants.ATTR_LAST_MODIFIED_ON, String
						.valueOf(now.getMillis()), folderEntity));
		folderEntity.getAttributes().add(
				new NodeAttribute(Constants.ATTR_LAST_MODIFIED_BY, userID,
						folderEntity));

		em.persist(folderEntity);
		return folderEntity.getId();
	}

	@Override
	public void deleteFolder(String folderID, String lockToken)
			throws QNodeLockException {
		Node folder = Node.findFolder(folderID, em);

		// Check lock status of the folder being deleted as well as its parent
		if ((folder.getLockToken() != null)
				&& (!folder.getLockToken().equals(lockToken))) {
			throw new QNodeLockException(
					"Folder with ID "
							+ folderID
							+ " is locked and an "
							+ "invalid lock tokwn was passed; the folder cannot be deleted.");
		}
		Node parent = folder.getParent();
		if ((parent != null) && (parent.getLockToken() != null)
				&& (!parent.getLockToken().equals(lockToken))) {
			throw new QNodeLockException(
					"Folder with ID "
							+ parent.getId()
							+ " is locked and an "
							+ "invalid lock token was passed; a child folder cannot be deleted.");
		}

		em.remove(folder);
	}

	@Override
	public void renameFolder(String folderID, String newName, String userID,
			String lockToken) throws QNodeLockException {
		Node folder = Node.findFolder(folderID, em);
		if ((folder.getLockToken() != null)
				&& (!folder.getLockToken().equals(lockToken))) {
			throw new QNodeLockException(
					"Folder with ID "
							+ folderID
							+ " is locked and an "
							+ "invalid lock token was passed; the folder cannot be renamed.");
		}
		folder.setAttribute(Constants.ATTR_NAME, newName, em);

		// Update last modified information
		if (userID != null) {
			DateTime now = DateTime.now();
			folder.setAttribute(Constants.ATTR_LAST_MODIFIED_ON,
					String.valueOf(now.getMillis()), em);
			folder.setAttribute(Constants.ATTR_LAST_MODIFIED_BY, userID, em);
		}
	}

	@Override
	public FolderDTO getFolderByID(String folderID, boolean lazyRelatives) {
		return ConverterUtil.nodeToFolderDTO(Node.findFolder(folderID, em),
				lazyRelatives);
	}

	@Override
	public byte[] getFolderAsZip(String folderID, boolean includeProperties,
			boolean isDeep) {
		byte[] retVal = null;

		Node folder = Node.findFolder(folderID, em);
		String nodeName = folder.getAttribute(Constants.ATTR_NAME).getValue();

		boolean hasEntries = false;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ZipOutputStream out = new ZipOutputStream(outStream);

		try {
			for (Node child : folder.getChildren()) {
				if (child.getType() == NodeType.FILE) {
					byte[] fileRetVal = versionService.getFileAsZip(
							child.getId(), includeProperties);
					if (fileRetVal != null) {
						hasEntries = true;
						ZipEntry entry = new ZipEntry(child.getAttribute(
								Constants.ATTR_NAME).getValue()
								+ ".zip");
						out.putNextEntry(entry);
						out.write(fileRetVal, 0, fileRetVal.length);
					}
				} else if ((child.getType() == NodeType.FOLDER) && isDeep) {
					byte[] folderRetVal = getFolderAsZip(child.getId(),
							includeProperties, isDeep);
					if (folderRetVal != null) {
						hasEntries = true;
						ZipEntry entry = new ZipEntry(child.getAttribute(
								Constants.ATTR_NAME).getValue()
								+ ".zip");
						out.putNextEntry(entry);
						out.write(folderRetVal, 0, folderRetVal.length);
					}
				}
			}

			if (includeProperties) {
				hasEntries = true;
				ZipEntry entry = new ZipEntry(nodeName + ".properties");
				out.putNextEntry(entry);
				StringBuilder buf = new StringBuilder();
				// Include a created on property
				buf.append(Constants.CREATED_ON).append(" = ")
						.append(folder.getCreatedOn()).append("\n");
				for (NodeAttribute attribute : folder.getAttributes()) {
					buf.append(attribute.getName());
					buf.append(" = ");
					buf.append(attribute.getValue());
					buf.append("\n");
				}
				out.write(buf.toString().getBytes());
			}
			if (hasEntries) {
				out.close();
				retVal = outStream.toByteArray();
			}
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			throw new QIOException("Error writing ZIP for folder  with ID "
					+ folderID);
		}

		return retVal;
	}

	@Override
	public String createFile(FileDTO file, String userID, String lockToken)
			throws QNodeLockException {
		Node parent = null;
		if (file.getParentId() != null) {
			parent = Node.findFolder(file.getParentId(), em);
		}

		// Check parent's lock status
		if ((parent != null) && (parent.getLockToken() != null)
				&& (!parent.getLockToken().equals(lockToken))) {
			throw new QNodeLockException(
					"Node with ID "
							+ parent.getId()
							+ " is locked and an "
							+ "invalid lock token was passed; a new child file cannot be created");
		}

		Node fileEntity = ConverterUtil.nodeDTOToNode(file);
		fileEntity.setParent(parent);

		// Set created / last modified information
		DateTime now = DateTime.now();
		fileEntity.setCreatedOn(now.getMillis());
		fileEntity.getAttributes()
				.add(new NodeAttribute(Constants.ATTR_CREATED_BY, userID,
						fileEntity));
		fileEntity.getAttributes().add(
				new NodeAttribute(Constants.ATTR_LAST_MODIFIED_ON, String
						.valueOf(now.getMillis()), fileEntity));
		fileEntity.getAttributes().add(
				new NodeAttribute(Constants.ATTR_LAST_MODIFIED_BY, userID,
						fileEntity));

		em.persist(fileEntity);
		return fileEntity.getId();
	}

	@Override
	public void deleteFile(String fileID, String lockToken)
			throws QNodeLockException {
		Node file = Node.findFile(fileID, em);

		// Check lock status of the file being deleted as well as its parent
		if ((file.getLockToken() != null)
				&& (!file.getLockToken().equals(lockToken))) {
			throw new QNodeLockException(
					"File with ID "
							+ fileID
							+ " is locked and an "
							+ "invalid lock tokwn was passed; the file cannot be deleted.");
		}
		Node parent = file.getParent();
		if ((parent != null) && (parent.getLockToken() != null)
				&& (!parent.getLockToken().equals(lockToken))) {
			throw new QNodeLockException(
					"Folder with ID "
							+ parent.getId()
							+ " is locked and an "
							+ "invalid lock token was passed; a child file cannot be deleted.");
		}

		em.remove(file);
	}

	@Override
	public void renameFile(String fileID, String newName, String userID,
			String lockToken) throws QNodeLockException {
		Node file = Node.findFile(fileID, em);
		if ((file.getLockToken() != null)
				&& (!file.getLockToken().equals(lockToken))) {
			throw new QNodeLockException(
					"File with ID "
							+ fileID
							+ " is locked and an "
							+ "invalid lock token was passed; the file cannot be renamed.");
		}
		file.setAttribute(Constants.ATTR_NAME, newName, em);

		// Update last modified information
		if (userID != null) {
			DateTime now = DateTime.now();
			file.setAttribute(Constants.ATTR_LAST_MODIFIED_ON,
					String.valueOf(now.getMillis()), em);
			file.setAttribute(Constants.ATTR_LAST_MODIFIED_BY, userID, em);
		}
	}

	@Override
	public FileDTO getFileByID(String fileID, boolean includeVersions) {
		FileDTO retVal = ConverterUtil.nodeToFileDTO(Node.findFile(fileID, em),
				false);

		if (includeVersions) {
			retVal.setVersions(versionService.getFileVersions(fileID));
		}

		return retVal;
	}

	@Override
	public NodeDTO getNodeByID(String nodeID) {
		return ConverterUtil.nodeToNodeDTO(Node.findNode(nodeID, em));
	}

	@Override
	public FolderDTO getParent(String nodeID, boolean lazyRelatives) {
		Node node = Node.findNode(nodeID, em);
		return ConverterUtil.nodeToFolderDTO(node.getParent(), lazyRelatives);
	}

	@Override
	public List<FolderDTO> getAncestors(String nodeID) {
		List<FolderDTO> retVal = null;
		Node node = Node.findNode(nodeID, em);
		if (node.getParent() == null) {
			return new ArrayList<>();
		} else {
			retVal = getAncestors(node.getParent().getId());
		}
		retVal.add(ConverterUtil.nodeToFolderDTO(node.getParent(), true));
		return retVal;

	}

	@Override
	public void updateAttribute(String nodeID, String attributeName,
			String attributeValue, String userID, String lockToken)
			throws QNodeLockException {
		Node node = Node.findNode(nodeID, em);
		if ((node.getLockToken() != null)
				&& (!node.getLockToken().equals(lockToken))) {
			throw new QNodeLockException(
					"Node with ID "
							+ nodeID
							+ " is locked and an "
							+ "invalid lock token was passed; the file attributes cannot be updated.");
		}
		node.setAttribute(attributeName, attributeValue, em);

		// Update last modified information
		if (userID != null) {
			DateTime now = DateTime.now();
			node.setAttribute(Constants.ATTR_LAST_MODIFIED_ON,
					String.valueOf(now.getMillis()), em);
			node.setAttribute(Constants.ATTR_LAST_MODIFIED_BY, userID, em);
		}
	}

	@Override
	public void updateAttributes(String nodeID, Map<String, String> attributes,
			String userID, String lockToken) throws QNodeLockException {
		Node node = Node.findNode(nodeID, em);
		if ((node.getLockToken() != null)
				&& (!node.getLockToken().equals(lockToken))) {
			throw new QNodeLockException(
					"Node with ID "
							+ nodeID
							+ " is locked and an "
							+ "invalid lock token was passed; the file attributes cannot be updated.");
		}

		for (String attributeName : attributes.keySet()) {
			node.setAttribute(attributeName, attributes.get(attributeName), em);
		}

		// Update last modified information
		if (userID != null) {
			DateTime now = DateTime.now();
			node.setAttribute(Constants.ATTR_LAST_MODIFIED_ON,
					String.valueOf(now.getMillis()), em);
			node.setAttribute(Constants.ATTR_LAST_MODIFIED_BY, userID, em);
		}
	}

	@Override
	public void deleteAttribute(String nodeID, String attributeName,
			String userID, String lockToken) throws QNodeLockException {
		Node node = Node.findNode(nodeID, em);
		if ((node.getLockToken() != null)
				&& (!node.getLockToken().equals(lockToken))) {
			throw new QNodeLockException(
					"Node with ID "
							+ nodeID
							+ " is locked and an "
							+ "invalid lock token was passed; the file attributes cannot be deleted.");
		}
		node.removeAttribute(attributeName, em);

		// Update last modified information
		if (userID != null) {
			DateTime now = DateTime.now();
			node.setAttribute(Constants.ATTR_LAST_MODIFIED_ON,
					String.valueOf(now.getMillis()), em);
			node.setAttribute(Constants.ATTR_LAST_MODIFIED_BY, userID, em);
		}
	}

	@Override
	public String copy(String nodeID, String newParentID, String userID,
			String lockToken) {
		Node node = Node.findNode(nodeID, em);
		Node newParent = Node.findFolder(newParentID, em);

		if ((newParent.getLockToken() != null)
				&& (!newParent.getLockToken().equals(lockToken))) {
			throw new QNodeLockException(
					"Node with ID "
							+ newParentID
							+ " is locked and an "
							+ "invalid lock token was passed; a new node cannot be copied into it.");
		}

		checkCyclicPath(nodeID, newParent);

		return copyNode(node, newParent, userID);
	}

	private String copyNode(Node node, Node newParent, String userID) {
		Node newNode = new Node();
		newNode.setType(node.getType());
		newNode.setParent(newParent);
		List<NodeAttribute> newAttributes = new ArrayList<NodeAttribute>();
		newNode.setAttributes(newAttributes);

		// Copy attributes except created/modified/locked information
		for (NodeAttribute attribute : node.getAttributes()) {
			switch (attribute.getName()) {
			case Constants.ATTR_CREATED_BY:
			case Constants.ATTR_LAST_MODIFIED_BY:
			case Constants.ATTR_LAST_MODIFIED_ON:
			case Constants.ATTR_LOCKED_BY:
			case Constants.ATTR_LOCKED_ON:
				break;
			default:
				newNode.getAttributes().add(
						new NodeAttribute(attribute.getName(), attribute.getValue(), newNode));
				break;

			}
		}

		// Set created / last modified information
		DateTime now = DateTime.now();
		newNode.setCreatedOn(now.getMillis());
		newNode.getAttributes().add(
				new NodeAttribute(Constants.ATTR_CREATED_BY, userID, newNode));
		newNode.getAttributes().add(
				new NodeAttribute(Constants.ATTR_LAST_MODIFIED_ON, String
						.valueOf(now.getMillis()), newNode));
		newNode.getAttributes().add(
				new NodeAttribute(Constants.ATTR_LAST_MODIFIED_BY, userID,
						newNode));
		em.persist(newNode);

		for (Node child : node.getChildren()) {
			copyNode(child, newNode, userID);
		}

		return newNode.getId();
	}

	@Override
	public void move(String nodeID, String newParentID, String userID,
			String lockToken) {
		Node node = Node.findNode(nodeID, em);
		Node newParent = Node.findFolder(newParentID, em);

		if ((node.getLockToken() != null)
				&& (!node.getLockToken().equals(lockToken))) {
			throw new QNodeLockException("Node with ID " + nodeID
					+ " is locked and an "
					+ "invalid lock token was passed; it cannot be moved.");
		}
		if ((newParent.getLockToken() != null)
				&& (!newParent.getLockToken().equals(lockToken))) {
			throw new QNodeLockException(
					"Node with ID "
							+ newParentID
							+ " is locked and an "
							+ "invalid lock token was passed; a new node cannot be moved into it.");
		}

		checkCyclicPath(nodeID, newParent);

		node.setParent(newParent);
	}

	private void checkCyclicPath(String nodeID, Node newParent) {
		Node checkedNode = newParent;
		while (checkedNode != null) {
			if (checkedNode.getId().equals(nodeID)) {
				throw new QInvalidPathException("Cannot move node with ID "
						+ nodeID + " under node with ID " + newParent.getId()
						+ " since this will create a cyclic path.");
			}
			checkedNode = checkedNode.getParent();
		}
	}

}
