package com.eurodyn.qlack2.be.rules.api.request.library.version;

import com.eurodyn.qlack2.fuse.idm.api.signing.QSignedRequest;

public class EnableTestingLibraryVersionRequest extends QSignedRequest {

	private String id;

	private boolean enableTesting;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isEnableTesting() {
		return enableTesting;
	}

	public void setEnableTesting(boolean enableTesting) {
		this.enableTesting = enableTesting;
	}

}
