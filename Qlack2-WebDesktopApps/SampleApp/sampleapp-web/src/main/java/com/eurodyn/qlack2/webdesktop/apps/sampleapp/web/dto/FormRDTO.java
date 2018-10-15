package com.eurodyn.qlack2.webdesktop.apps.sampleapp.web.dto;

import java.util.List;

public class FormRDTO {
	private String id;
	private String title;
	private String description;
	List<FormVersionNameRDTO> versions;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<FormVersionNameRDTO> getVersions() {
		return versions;
	}

	public void setVersions(List<FormVersionNameRDTO> versions) {
		this.versions = versions;
	}
}
