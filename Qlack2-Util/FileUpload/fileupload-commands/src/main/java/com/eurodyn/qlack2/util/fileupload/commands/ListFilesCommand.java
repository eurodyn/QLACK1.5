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
package com.eurodyn.qlack2.util.fileupload.commands;

import java.util.Date;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.eurodyn.qlack2.util.fileupload.api.FileUpload;
import com.eurodyn.qlack2.util.fileupload.api.dto.DBFileDTO;
import com.eurodyn.qlack2.util.fileupload.api.request.FileListRequest;
import com.eurodyn.qlack2.util.fileupload.api.response.FileListResponse;

@Command(scope = "qlack", name = "fileupload-list-files",
description = "List all files submitted.")
public final class ListFilesCommand extends OsgiCommandSupport {
	@Argument(index=0, name="includeBinary", description = "A boolean value to "
			+ "indicate whether the binary content of each file should also be "
			+ "retrieved.", required = false, multiValued = false)
    private boolean includeBinary = false;

	private FileUpload fileUploadService;

	public void setFileUploadService(FileUpload fileUploadService) {
		this.fileUploadService = fileUploadService;
	}

	@Override
	protected Object doExecute() {
		System.out.println("Executing list-files command [includeBinary=" + includeBinary + "].");
		long starTime = System.currentTimeMillis();
		FileListResponse res = fileUploadService.listFilesForConsole(new FileListRequest(includeBinary));
		for (DBFileDTO f : res.getFiles()) {
			System.out.println("**********************************************************************************************");
			System.out.println("File: " + f.getFilename());
			System.out.println("  id: " + f.getId());
			System.out.println("**********************************************************************************************");
			System.out.println("         Uploaded by: " + f.getUploadedBy());
			System.out.println("         Uploaded at: " + new Date(f.getUploadedAt()) + " [" + f.getUploadedAt() + "]");
			System.out.println("No of Chunks (total): " + f.getTotalChunks());
			System.out.println("No of Chunks (recvd): " + f.getReceivedChunks());
			System.out.println("                Size: " + f.getTotalSize());
			System.out.println();
		}

		System.out.println("*** Executed in: " + (System.currentTimeMillis() - starTime) + "msec.");
		return null;
	}

}
