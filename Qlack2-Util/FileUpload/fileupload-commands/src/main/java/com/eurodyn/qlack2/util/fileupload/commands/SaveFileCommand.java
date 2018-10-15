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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.eurodyn.qlack2.util.fileupload.api.FileUpload;
import com.eurodyn.qlack2.util.fileupload.api.request.FileGetRequest;
import com.eurodyn.qlack2.util.fileupload.api.response.FileGetResponse;

@Command(scope = "qlack", name = "fileupload-save-file",
description = "Reassembles chunks to extract a file into a requested location.")
public final class SaveFileCommand extends OsgiCommandSupport {
	@Argument(index=0, name="fileID", description = "The ID of the file to extract.",
			required = true, multiValued = false)
    private String fileID;
	@Argument(index=1, name="path", description = "The path where to extract the file.",
			required = true, multiValued = false)
    private String path;

	private FileUpload fileUploadService;

	public void setFileUploadService(FileUpload fileUploadService) {
		this.fileUploadService = fileUploadService;
	}

	@Override
	protected Object doExecute() throws IOException {
		System.out.println("Executing save-file command.");
		long starTime = System.currentTimeMillis();

		FileGetResponse res = fileUploadService.getByIDForConsole(new FileGetRequest(fileID));
		File filepath = new File(path + File.separator + res.getFile().getFilename());
		FileUtils.writeByteArrayToFile(filepath, res.getFile().getFileData());

		System.out.println("Extract file to: " + filepath.getAbsolutePath());

		System.out.println("*** Executed in: " + (System.currentTimeMillis() - starTime) + "msec.");
		return null;
	}

}
