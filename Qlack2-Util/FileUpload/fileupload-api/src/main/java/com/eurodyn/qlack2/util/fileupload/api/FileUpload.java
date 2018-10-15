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
package com.eurodyn.qlack2.util.fileupload.api;

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

public interface FileUpload {
	CheckChunkResponse checkChunk(CheckChunkRequest req);
	FileUploadResponse upload(FileUploadRequest req);

	FileDeleteResponse deleteByID(FileDeleteRequest req);
	FileDeleteResponse deleteByIDForConsole(FileDeleteRequest req);

	FileGetResponse getByID(FileGetRequest req);
	FileGetResponse getByIDForConsole(FileGetRequest req);

	FileListResponse listFiles(FileListRequest req);
	FileListResponse listFilesForConsole(FileListRequest req);
	
	VirusScanResponse virusScan(VirusScanRequest req);
}
