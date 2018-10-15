package com.eurodyn.qlack2.be.workflow.web.dto;

import javax.validation.constraints.NotNull;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryRDTO {
	@NotNull
	@NotEmpty
	@Length(min = 1, max = 255)
	private String name;

	@Length(min = 0, max = 1024)
	private String description;

	@NotNull
	@NotEmpty
	private String projectId;

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

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
}
