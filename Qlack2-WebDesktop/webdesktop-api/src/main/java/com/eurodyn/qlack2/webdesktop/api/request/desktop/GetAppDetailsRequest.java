package com.eurodyn.qlack2.webdesktop.api.request.desktop;

import com.eurodyn.qlack2.fuse.idm.api.signing.QSignedRequest;

public class GetAppDetailsRequest extends QSignedRequest {
	private String appID;

	public GetAppDetailsRequest(String appID) {
		this.appID = appID;
	}

	public String getAppID() {
		return appID;
	}

	public void setAppID(String appID) {
		this.appID = appID;
	}
}
