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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.joda.time.DateTime;

import com.eurodyn.qlack2.fuse.cm.api.VersionService;
import com.eurodyn.qlack2.fuse.cm.api.dto.BinContentDTO;
import com.eurodyn.qlack2.fuse.cm.api.dto.VersionDTO;
import com.eurodyn.qlack2.fuse.cm.api.exception.QIOException;
import com.eurodyn.qlack2.fuse.cm.api.exception.QNodeLockException;
import com.eurodyn.qlack2.fuse.cm.impl.model.Node;
import com.eurodyn.qlack2.fuse.cm.impl.model.NodeAttribute;
import com.eurodyn.qlack2.fuse.cm.impl.model.Version;
import com.eurodyn.qlack2.fuse.cm.impl.model.VersionAttribute;
import com.eurodyn.qlack2.fuse.cm.impl.util.Constants;
import com.eurodyn.qlack2.fuse.cm.impl.util.ConverterUtil;

import eu.medsea.mimeutil.MimeUtil2;

public class VersionServiceImpl implements VersionService {
	private static final Logger LOGGER = Logger.getLogger(VersionServiceImpl.class.getName());
	private EntityManager em;
	
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	public void setEm(EntityManager em) {
		this.em = em;
	}
	
	private String getMimeType(byte[] document) {
		String mimeType = DEFAULT_MIME_TYPE;
		MimeUtil2 mimeUtil = new MimeUtil2();
        mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        @SuppressWarnings("rawtypes")
		Collection mimeTypes = mimeUtil.getMimeTypes(document);
        mimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
		if (mimeTypes.size() == 0) {
			LOGGER.log(Level.WARNING,
					"Image mimetype not found - returning default");
			return mimeType;
		}

		return mimeTypes.iterator().next().toString();
	}

	@Override
	public String createVersion(String fileID, String versionName,
			BinContentDTO content, String userID, String lockToken)
			throws QNodeLockException {
		Node file = Node.findFile(fileID, em);
		if ((file.getLockToken() != null)
				&& (!file.getLockToken().equals(lockToken))) {
			throw new QNodeLockException("File with ID " + file
					+ " is locked and an "
					+ "invalid lock token was passed; a new version cannot"
					+ "be created for this file.");
		}

		Version version = new Version();
		version.setName(versionName);
		version.setNode(file);
		version.setFilename(content.getFilename());
		version.setMimetype(content.getMimetype() != null ? content.getMimetype() : getMimeType(content.getContent()));
		version.setContent(content.getContent());

		// Set created / last modified information
		version.setAttributes(new ArrayList<VersionAttribute>());
		DateTime now = DateTime.now();
		version.setCreatedOn(now.getMillis());
		version.getAttributes()
				.add(new VersionAttribute(Constants.ATTR_CREATED_BY, userID,
						version));
		version.getAttributes().add(
				new VersionAttribute(Constants.ATTR_LAST_MODIFIED_ON, String
						.valueOf(now.getMillis()), version));
		version.getAttributes().add(
				new VersionAttribute(Constants.ATTR_LAST_MODIFIED_BY, userID,
						version));

		em.persist(version);
		return version.getId();
	}

	@Override
	public void updateBinContent(String fileID, BinContentDTO newContent,
			String userID, String lockToken)
			throws QNodeLockException {
		updateBinContent(fileID, null, newContent, userID, lockToken);
	}

	@Override
	public void updateBinContent(String fileID, String versionName,
			BinContentDTO newContent, String userID, String lockToken)
			throws QNodeLockException {
		Node file = Node.findFile(fileID, em);
		if ((file.getLockToken() != null)
				&& (!file.getLockToken().equals(lockToken))) {
			throw new QNodeLockException(
					"File with ID "
							+ file
							+ " is locked and an "
							+ "invalid lock token was passed; the binary content of this file"
							+ "cannot be updated.");
		}

		Version version = Version.find(fileID, versionName, em);
		version.setFilename(newContent.getFilename());
		version.setMimetype(newContent.getMimetype() != null ? newContent.getMimetype() : getMimeType(newContent.getContent()));
		version.setContent(newContent.getContent());

		// Update last modified information
		if (userID != null) {
			DateTime now = DateTime.now();
			version.setAttribute(Constants.ATTR_LAST_MODIFIED_ON,
					String.valueOf(now.getMillis()), em);
			version.setAttribute(Constants.ATTR_LAST_MODIFIED_BY, userID, em);
		}
	}

	@Override
	public List<VersionDTO> getFileVersions(String fileID) {
		Query query = em
				.createQuery("SELECT v FROM Version v WHERE v.node.id = :fileID ORDER BY v.createdOn ASC");
		query.setParameter("fileID", fileID);
		List<Version> versions = query.getResultList();
		return ConverterUtil.versionToVersionDTOList(versions);
	}

	@Override
	public VersionDTO getFileLatestVersion(String fileID) {
		return ConverterUtil.versionToVersionDTO(Version.findLatest(fileID, em));
	}

	@Override
	public BinContentDTO getBinContent(String fileID) {
		return getBinContent(fileID, null);
	}

	@Override
	public BinContentDTO getBinContent(String fileID, String versionName) {
		Version version = Version.find(fileID, versionName, em);
		return ConverterUtil.versionToBinContentDTO(version);
	}

	@Override
	public byte[] getFileAsZip(String fileID, boolean includeProperties) {
		return getFileAsZip(fileID, null, includeProperties);
	}

	@Override
	public byte[] getFileAsZip(String fileID, String versionName,
			boolean includeProperties) {
		Node file = Node.findFile(fileID, em);
		Version version = Version.find(fileID, versionName, em);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ZipOutputStream zipFile = new ZipOutputStream(outStream);

        try {
	        // Write binary content
	        ZipEntry entry = new ZipEntry(version.getFilename());
	        zipFile.putNextEntry(entry);
	        zipFile.write(version.getContent());

	        if (includeProperties) {
	        	// Write file properties
	        	entry = new ZipEntry(file.getAttribute(Constants.ATTR_NAME) + ".properties");
	            zipFile.putNextEntry(entry);
	            StringBuilder buf = new StringBuilder();
	            // Include a created on property
	            buf.append(Constants.CREATED_ON).append(" = ").append(file.getCreatedOn()).append("\n");
	            for (NodeAttribute attribute : file.getAttributes()) {
	                buf.append(attribute.getName());
	                buf.append(" = ");
	                buf.append(attribute.getValue());
	                buf.append("\n");
	            }

	        	// Write version properties	- written in a separate file since there are
	            // some properties which exist both in the file and in the version
	            // (ex. last modified on/by)
	            entry = new ZipEntry(version.getName() + ".properties");
	            zipFile.putNextEntry(entry);
	            buf = new StringBuilder();
	            // Include a created on property
	            buf.append(Constants.CREATED_ON).append(" = ").append(file.getCreatedOn()).append("\n");
	            for (VersionAttribute attribute : version.getAttributes()) {
	                buf.append(attribute.getName());
	                buf.append(" = ");
	                buf.append(attribute.getValue());
	                buf.append("\n");
	            }

	            zipFile.write(buf.toString().getBytes());
	        }
	        zipFile.close();
	        outStream.close();
        } catch (IOException ex) {
        	LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        	throw new QIOException("Error writing ZIP for version " + versionName
        			+ " of file  with ID " + fileID);
        }

        return outStream.toByteArray();
	}

	@Override
	public void updateAttribute(String fileID, String attributeName,
			String attributeValue, String userID, String lockToken)
			throws QNodeLockException {
		updateAttribute(fileID, null, attributeName, attributeValue, userID, lockToken);
	}

	@Override
	public void updateAttribute(String fileID, String versionName,
			String attributeName, String attributeValue, String userID, String lockToken)
			throws QNodeLockException {
		Version version = Version.find(fileID, versionName, em);
		Node file = version.getNode();

		if ((file.getLockToken() != null)
				&& (!file.getLockToken().equals(lockToken))) {
			throw new QNodeLockException("File with ID " + file
					+ " is locked and an "
					+ "invalid lock token was passed; the file version "
					+ "attributes cannot be updated.");
		}

		version.setAttribute(attributeName, attributeValue, em);

		// Update last modified information
		if (userID != null) {
			DateTime now = DateTime.now();
			version.setAttribute(Constants.ATTR_LAST_MODIFIED_ON,
					String.valueOf(now.getMillis()), em);
			version.setAttribute(Constants.ATTR_LAST_MODIFIED_BY, userID, em);
		}
	}

	@Override
	public void updateAttributes(String fileID, Map<String, String> attributes,
			String userID, String lockToken)
			throws QNodeLockException {
		updateAttributes(fileID, null, attributes, userID, lockToken);
	}

	@Override
	public void updateAttributes(String fileID, String versionName,
			Map<String, String> attributes, String userID, String lockToken)
			throws QNodeLockException {
		Version version = Version.find(fileID, versionName, em);
		Node file = version.getNode();

		if ((file.getLockToken() != null)
				&& (!file.getLockToken().equals(lockToken))) {
			throw new QNodeLockException("File with ID " + file
					+ " is locked and an "
					+ "invalid lock token was passed; the file version"
					+ "attributes cannot be updated.");
		}

		for (String attributeName : attributes.keySet()) {
			version.setAttribute(attributeName, attributes.get(attributeName),
					em);
		}

		// Update last modified information
		if (userID != null) {
			DateTime now = DateTime.now();
			version.setAttribute(Constants.ATTR_LAST_MODIFIED_ON,
					String.valueOf(now.getMillis()), em);
			version.setAttribute(Constants.ATTR_LAST_MODIFIED_BY, userID, em);
		}
	}

	@Override
	public void deleteAttribute(String fileID, String attributeName, String userID, String lockToken)
			throws QNodeLockException {
		deleteAttribute(fileID, null, attributeName, userID, lockToken);
	}

	@Override
	public void deleteAttribute(String fileID, String versionName,
			String attributeName, String userID, String lockToken) throws QNodeLockException {
		Version version = Version.find(fileID, versionName, em);
		Node file = version.getNode();

		if ((file.getLockToken() != null)
				&& (!file.getLockToken().equals(lockToken))) {
			throw new QNodeLockException("File with ID " + file
					+ " is locked and an "
					+ "invalid lock token was passed; the file version"
					+ "attributes cannot be deleted.");
		}

		version.removeAttribute(attributeName, em);

		// Update last modified information
		if (userID != null) {
			DateTime now = DateTime.now();
			version.setAttribute(Constants.ATTR_LAST_MODIFIED_ON,
					String.valueOf(now.getMillis()), em);
			version.setAttribute(Constants.ATTR_LAST_MODIFIED_BY, userID, em);
		}
	}

	@Override
	public String copyVersionAsNew(String fileID, String versionName,
			String newVersionName, String userID, String lockToken)
			throws QNodeLockException {
		Version version = Version.find(fileID, versionName, em);
		Node file = version.getNode();

		if ((file.getLockToken() != null)
				&& (!file.getLockToken().equals(lockToken))) {
			throw new QNodeLockException("File with ID " + file
					+ " is locked and an "
					+ "invalid lock token was passed; a new version cannot"
					+ "be created for this file.");
		}

		String newVersionID = createVersion(fileID, newVersionName,
				ConverterUtil.versionToBinContentDTO(version), userID,
				lockToken);
		Version newVersion = Version.find(newVersionID, em);
		for (VersionAttribute attribute : version.getAttributes()) {
			switch (attribute.getName()) {
			// Do not copy created/modified on/by attributes
			case Constants.ATTR_CREATED_BY:
			case Constants.ATTR_LAST_MODIFIED_BY:
			case Constants.ATTR_LAST_MODIFIED_ON:
				break;
			default:
				newVersion.getAttributes().add(attribute);
				break;
			}
		}

		return newVersionID;
	}

}
