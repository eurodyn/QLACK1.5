package com.eurodyn.qlack2.webdesktop.apps.sampleapp.api.response;

public class SaveUserResponse {
	private String userID;

	public SaveUserResponse() {
	}

	public SaveUserResponse(String userID) {
		this.userID = userID;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}


}
