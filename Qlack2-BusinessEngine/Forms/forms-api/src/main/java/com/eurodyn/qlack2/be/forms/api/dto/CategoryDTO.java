package com.eurodyn.qlack2.be.forms.api.dto;

import java.util.List;

import com.eurodyn.qlack2.webdesktop.api.dto.UserDTO;

public class CategoryDTO {
	private String id;
	private String name;
	private String description;
	private long createdOn;
	private UserDTO createdBy;
	private long lastModifiedOn;
	private UserDTO lastModifiedBy;
	private List<FormDTO> forms;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}

	public UserDTO getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(UserDTO createdBy) {
		this.createdBy = createdBy;
	}

	public long getLastModifiedOn() {
		return lastModifiedOn;
	}

	public void setLastModifiedOn(long lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
	}

	public UserDTO getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(UserDTO lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public List<FormDTO> getForms() {
		return forms;
	}

	public void setForms(List<FormDTO> forms) {
		this.forms = forms;
	}

}
