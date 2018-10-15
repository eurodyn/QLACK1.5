package com.eurodyn.qlack2.webdesktop.apps.sampleapp.api.request;

import com.eurodyn.qlack2.fuse.idm.api.signing.QSignedRequest;

public class SaveUserRequest extends QSignedRequest {
	private String fullname;
	private int age;
	private String gender;
	private Website website;

	public SaveUserRequest() {
		super();
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Website getWebsite() {
		return website;
	}

	public void setWebsite(Website website) {
		this.website = website;
	}


	public static class Website {
		String protocol;
		String address;

		public Website(String address) {
			super();
			this.address = address;
		}
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
	}
}
