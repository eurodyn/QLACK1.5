package com.eurodyn.qlack2.be.rules.api.dto;

import java.util.ArrayList;
import java.util.List;

import com.eurodyn.qlack2.webdesktop.api.dto.UserDTO;

public class DataModelVersionDTO {

	private String dataModelId;

	private String id;
	private String name;
	private String description;

	private String modelPackage;

	private String parentModelId; // read-only
	private String parentModelVersionId;

	private List<DataModelFieldDTO> fields = new ArrayList<>();

	private VersionState state;
	private long createdOn;
	private UserDTO createdBy;
	private long lastModifiedOn;
	private UserDTO lastModifiedBy;
	private Long lockedOn;
	private UserDTO lockedBy;

	// -- Constructors

	public DataModelVersionDTO() {
		//empty no arg DTO Constructor
	}

	// -- Accessors

	public String getDataModelId() {
		return dataModelId;
	}

	public void setDataModelId(String modelId) {
		this.dataModelId = modelId;
	}

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

	public String getModelPackage() {
		return modelPackage;
	}

	public void setModelPackage(String modelPackage) {
		this.modelPackage = modelPackage;
	}

	public String getParentModelId() {
		return parentModelId;
	}

	public void setParentModelId(String parentModelId) {
		this.parentModelId = parentModelId;
	}

	public String getParentModelVersionId() {
		return parentModelVersionId;
	}

	public void setParentModelVersionId(String parentModelVersionId) {
		this.parentModelVersionId = parentModelVersionId;
	}

	public List<DataModelFieldDTO> getFields() {
		return fields;
	}

	public void setFields(List<DataModelFieldDTO> fields) {
		this.fields = fields;
	}

	public VersionState getState() {
		return state;
	}

	public void setState(VersionState state) {
		this.state = state;
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

	public Long getLockedOn() {
		return lockedOn;
	}

	public void setLockedOn(Long lockedOn) {
		this.lockedOn = lockedOn;
	}

	public UserDTO getLockedBy() {
		return lockedBy;
	}

	public void setLockedBy(UserDTO lockedBy) {
		this.lockedBy = lockedBy;
	}

}
