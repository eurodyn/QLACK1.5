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
package com.eurodyn.qlack2.util.fileupload.impl.rest;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;

import com.eurodyn.qlack2.fuse.idm.api.signing.SignedTicket;
import com.eurodyn.qlack2.util.fileupload.api.FileUpload;
import com.eurodyn.qlack2.util.fileupload.api.request.CheckChunkRequest;
import com.eurodyn.qlack2.util.fileupload.api.request.FileDeleteRequest;
import com.eurodyn.qlack2.util.fileupload.api.request.FileUploadRequest;
import com.eurodyn.qlack2.util.fileupload.api.response.CheckChunkResponse;
import com.eurodyn.qlack2.util.fileupload.api.response.FileDeleteResponse;
import com.eurodyn.qlack2.util.fileupload.api.rest.FileUploadRest;
import com.eurodyn.qlack2.util.fileupload.api.rest.rdto.FileDeleteRDTO;

public class FileUploadRestImpl implements FileUploadRest {
	private static final Logger LOGGER = Logger.getLogger(FileUploadRestImpl.class.getName());
	private FileUpload fileUpload;
	private String ticketHeaderName;

	public void setTicketHeaderName(String ticketHeaderName) {
		this.ticketHeaderName = ticketHeaderName;
	}

	public void setFileUpload(FileUpload fileUpload) {
		this.fileUpload = fileUpload;
	}

	private SignedTicket getSignedTicket(HttpHeaders headers) {
		if (headers.getRequestHeaders() != null
				&& headers.getRequestHeaders().getFirst(ticketHeaderName) != null) {
			return SignedTicket.fromVal(headers.getRequestHeaders().getFirst(
					ticketHeaderName));
		} else {
			return null;
		}
	}

	private SignedTicket getSignedTicket(String header) {
		return SignedTicket.fromVal(header);
	}

	private byte[] getBin(String fieldName, MultipartBody body)
			throws IOException {
		return IOUtils.readBytesFromStream(body.getAttachment(fieldName)
				.getDataHandler().getInputStream());
	}

	private String getString(String fieldName, MultipartBody body)
			throws IOException {
		return IOUtils.toString(body.getAttachment(fieldName).getDataHandler()
				.getInputStream());
	}

	private long getLong(String fieldName, MultipartBody body)
			throws IOException {
		return Long.parseLong(IOUtils.toString(body.getAttachment(fieldName)
				.getDataHandler().getInputStream()));
	}

	@Override
	public CheckChunkResponse checkChunk(long chunkNumber, long chunkSize,
			long totalSize, String alias, String filename, long totalChunks,
			HttpHeaders headers, MessageContext msgContext) {
		CheckChunkRequest req = new CheckChunkRequest();
		req.setChunkNumber(chunkNumber);
		req.setFileAlias(alias);
		req.setSignedTicket(getSignedTicket(headers));
		CheckChunkResponse res = fileUpload.checkChunk(req);
		// Flow.js requires a non-200 status code if the chunk does not exist.
		if (!res.isChunkExists()) {
			msgContext.getHttpServletResponse().setStatus(
					HttpServletResponse.SC_ACCEPTED);
		}

		return res;
	}

	@Override
	public String upload(MultipartBody body, HttpHeaders headers) {
		try {
			FileUploadRequest fur = new FileUploadRequest();
			fur.setAlias(getString("flowIdentifier", body));
			fur.setAutoDelete(true);
			if (body.getAttachment("flowChunkNumber") != null) {
				fur.setChunkNumber(getLong("flowChunkNumber", body));
			} else {
				// support for older browsers, where there is always one chunk
				fur.setChunkNumber(1);
			}
			if (body.getAttachment("flowChunkSize") != null) {
				fur.setChunkSize(getLong("flowChunkSize", body));
			}
			fur.setFilename(getString("flowFilename", body));
			if (body.getAttachment("flowTotalChunks") != null) {
				fur.setTotalChunks(getLong("flowTotalChunks", body));
			} else {
				// support for older browsers, where there is always one chunk
				fur.setTotalChunks(1);
			}
			if (body.getAttachment("flowTotalSize") != null) {
				fur.setTotalSize(getLong("flowTotalSize", body));
			}
			fur.setSignedTicket(getSignedTicket(headers));
			if (fur.getSignedTicket() == null) {
				fur.setSignedTicket(getSignedTicket(getString("sHeader", body)));
			}
			if (fur.getSignedTicket() != null) {
				fur.setUploadedBy(fur.getSignedTicket().getUserID());
			}
			fur.setData(getBin("file", body));

			fileUpload.upload(fur);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Could not process file upload.", e);

		}
		return "";
	}

	@Override
	public FileDeleteResponse deleteByID(FileDeleteRDTO req, HttpHeaders headers) {
		FileDeleteRequest fdr = new FileDeleteRequest();
		fdr.setSignedTicket(getSignedTicket(headers));
		fdr.setId(req.getId());

		return fileUpload.deleteByID(fdr);
	}

}
