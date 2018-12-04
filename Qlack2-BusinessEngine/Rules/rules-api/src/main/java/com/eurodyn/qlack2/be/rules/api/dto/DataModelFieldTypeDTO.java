package com.eurodyn.qlack2.be.rules.api.dto;

public class DataModelFieldTypeDTO {

	private String id;
	private String name;

	// -- Constructors

	public DataModelFieldTypeDTO() {
		//empty no arg DTO Constructor
	}

	// -- Accessors

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

}
