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
package com.eurodyn.qlack2.util.fileupload.impl;

import com.eurodyn.qlack2.fuse.idm.api.IDMService;
import com.eurodyn.qlack2.fuse.idm.api.annotations.ValidateTicket;
import com.eurodyn.qlack2.util.fileupload.api.FileUpload;
import com.eurodyn.qlack2.util.fileupload.api.dto.DBFileDTO;
import com.eurodyn.qlack2.util.fileupload.api.exception.QFileNotCompletedException;
import com.eurodyn.qlack2.util.fileupload.api.exception.QFileNotFoundException;
import com.eurodyn.qlack2.util.fileupload.api.exception.QFileUploadException;
import com.eurodyn.qlack2.util.fileupload.api.exception.QVirusScanException;
import com.eurodyn.qlack2.util.fileupload.api.request.CheckChunkRequest;
import com.eurodyn.qlack2.util.fileupload.api.request.FileDeleteRequest;
import com.eurodyn.qlack2.util.fileupload.api.request.FileGetRequest;
import com.eurodyn.qlack2.util.fileupload.api.request.FileListRequest;
import com.eurodyn.qlack2.util.fileupload.api.request.FileUploadRequest;
import com.eurodyn.qlack2.util.fileupload.api.request.VirusScanRequest;
import com.eurodyn.qlack2.util.fileupload.api.response.CheckChunkResponse;
import com.eurodyn.qlack2.util.fileupload.api.response.FileDeleteResponse;
import com.eurodyn.qlack2.util.fileupload.api.response.FileGetResponse;
import com.eurodyn.qlack2.util.fileupload.api.response.FileListResponse;
import com.eurodyn.qlack2.util.fileupload.api.response.FileUploadResponse;
import com.eurodyn.qlack2.util.fileupload.api.response.VirusScanResponse;
import com.eurodyn.qlack2.util.fileupload.impl.model.DBFile;
import com.eurodyn.qlack2.util.fileupload.impl.model.DBFilePK;
import io.sensesecure.clamav4j.ClamAV;
import io.sensesecure.clamav4j.ClamAVException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;

public class FileUploadImpl implements FileUpload {
	private static final Logger LOGGER = Logger.getLogger(FileUploadImpl.class
			.getName());
	private EntityManager em;
	@SuppressWarnings("unused")
	private IDMService idmService;
	private String clamAV;
	private static final int CLAMAV_SOCKET_TIMEOUT = 10000;
		
	public void setIdmService(IDMService idmService) {
		this.idmService = idmService;
	}

	public void setEm(EntityManager em) {
		this.em = em;
	}

	public void setClamAV(String clamAV) {
		this.clamAV = clamAV;
	}

	@Override
	@ValidateTicket
	public CheckChunkResponse checkChunk(CheckChunkRequest req) {
		CheckChunkResponse res = new CheckChunkResponse();
		res.setChunkExists(DBFile.getChunk(req.getFileAlias(),
				req.getChunkNumber(), em) != null);

		return res;
	}

	@Override
	@ValidateTicket
	public FileUploadResponse upload(FileUploadRequest req) {
		FileUploadResponse res = new FileUploadResponse();

		// Check if this chunk has already been uploaded, so that we can support
		// updating existing chunks.
		DBFile file = DBFile.getChunk(req.getAlias(), req.getChunkNumber(), em);
		if (file == null) {
			file = new DBFile(
					new DBFilePK(req.getAlias(), req.getChunkNumber()));
			res.setChunkExists(false);
		} else {
			res.setChunkExists(true);
		}
		file.setExpectedChunks(req.getTotalChunks());
		file.setFileName(req.getFilename());
		if (req.getTotalSize() == 0) {
			file.setFileSize(req.getTotalSize());
		} else {
			file.setFileSize(req.getData().length);
		}
		file.setUploadedAt(System.currentTimeMillis());
		file.setUploadedBy(req.getSignedTicket().getUserID());
		file.setChunkData(req.getData());
		file.setChunkSize(req.getData().length);

		em.persist(file);

		return res;
	}

	@Override
	@ValidateTicket
	public FileDeleteResponse deleteByID(FileDeleteRequest req) {
		return deleteByIDForConsole(req);
	}

	@Override
	public FileDeleteResponse deleteByIDForConsole(FileDeleteRequest req) {
		return new FileDeleteResponse(DBFile.delete(req.getId(), em));
	}

	@Override
	public FileGetResponse getByIDForConsole(FileGetRequest req) {
		return new FileGetResponse(getByID(req.getId(), true));
	}

	@Override
	@ValidateTicket
	public FileGetResponse getByID(FileGetRequest req) {
		return getByIDForConsole(req);
	}

	@Override
	public FileListResponse listFilesForConsole(FileListRequest req) {
		List<DBFileDTO> retVal = new ArrayList<>();

		// First find all unique IDs for file chunks.
		Query q = em
				.createQuery("select distinct f.id.id from DBFile f order by f.uploadedAt");
		@SuppressWarnings("unchecked")
		List<String> chunks = q.getResultList();
		for (String id : chunks) {
			retVal.add(getByID(id, req.isIncludeBinary()));
		}

		return new FileListResponse(retVal);
	}

	@Override
	@ValidateTicket
	public FileListResponse listFiles(FileListRequest req) {
		return listFilesForConsole(req);
	}

	private DBFileDTO getByID(String fileID, boolean includeBinary) {
		// Find all chunks of the requested file.
		Query q = em
				.createQuery(
						"select f from DBFile f where f.id.id = :id order by f.id.chunkOrder")
				.setParameter("id", fileID);
		@SuppressWarnings("unchecked")
		List<DBFile> results = q.getResultList();

		// Check if any chunk for the requested file has been found.
		if (results == null || (results != null && !results.isEmpty())) {
			throw new QFileNotFoundException();
		}

		// Get a random chunk to obtain information for the underlying file
		// (i.e. all chunks contain replicated information about the file from
		// which they were decomposed).
		DBFile randomChunk = results.get(0);

		// Prepare the return value.
		DBFileDTO dto = new DBFileDTO();

		// Check if all expected chunks of this file are available. This check
		// is performed only when the caller has requested the binary
		// representation  of the file in order not to return a corrupted file.
		if (includeBinary) {
			long startTime = System.currentTimeMillis();
			if (randomChunk.getExpectedChunks() != results.size()) {
				throw new QFileNotCompletedException();
			}
			// Assemble the original file out of its chunks.
			try {
				ByteArrayOutputStream bOut = new ByteArrayOutputStream(
						(int) randomChunk.getFileSize());
				for (DBFile f : results) {
					bOut.write(f.getChunkData());
				}
				dto.setFileData(bOut.toByteArray());

			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Could not reassemble file " + fileID,
						e);
				throw new QFileUploadException("Could not reassemble file "
						+ fileID);
			}
			dto.setReassemblyTime(System.currentTimeMillis() - startTime);
		} else {
			dto.setReassemblyTime(-1);
		}

		// Further compose the return value.
		dto.setFilename(randomChunk.getFileName());
		dto.setId(fileID);
		dto.setReceivedChunks(results.size());
		dto.setTotalChunks(randomChunk.getExpectedChunks());
		dto.setUploadedAt(randomChunk.getUploadedAt());
		dto.setUploadedBy(randomChunk.getUploadedBy());
		dto.setTotalSize(randomChunk.getFileSize());

		return dto;

	}

	@Override
	@ValidateTicket
	public VirusScanResponse virusScan(VirusScanRequest req) {
		// Check if a custom address for ClamAV has been provided or use the
		// default.
		InetSocketAddress clamAVAddress;
		if (StringUtils.isBlank(req.getClamAVHost())) {
			clamAVAddress = new InetSocketAddress(clamAV.substring(0,
					clamAV.indexOf(":")), Integer.parseInt(clamAV.substring(clamAV
					.indexOf(":") + 1)));
		} else {
			clamAVAddress = new InetSocketAddress(req.getClamAVHost(), req.getClamAVPort());
		}
		LOGGER.log(Level.FINE, "Contacting ClamAV at: {0}.", clamAVAddress);
		ClamAV clamAV = new ClamAV(clamAVAddress, CLAMAV_SOCKET_TIMEOUT);
		DBFileDTO fileDTO = getByID(req.getId(), true);
		String scanResult = null;
		try {
			scanResult = clamAV.scan(new ByteArrayInputStream(fileDTO.getFileData()));
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Could not check file for virus, file ID=" + req.getId(), e);
			throw new QVirusScanException("Could not check file for virus, file ID=" + req.getId());
		} catch (ClamAVException e) {
			LOGGER.log(Level.SEVERE, "Could not check file for virus, file ID=" + req.getId(), e);
			throw new QVirusScanException("Could not check file for virus, file ID=" + req.getId());
		}

		VirusScanResponse res = new VirusScanResponse();
		res.setId(req.getId());
		res.setVirusFree(scanResult.equals("OK") ? true : false);
		res.setVirusScanDescription(scanResult);

		return res;
	}

}
