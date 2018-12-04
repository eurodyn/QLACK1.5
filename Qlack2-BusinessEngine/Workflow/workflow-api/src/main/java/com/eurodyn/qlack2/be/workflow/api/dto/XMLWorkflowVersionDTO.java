package com.eurodyn.qlack2.be.workflow.api.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "workflowVersion")
public class XMLWorkflowVersionDTO {

	private String name;
	private String description;
	private String content;
	private String processId;
	private XMLConditionsDTO conditions;

	public XMLWorkflowVersionDTO() {
		//empty no arg DTO Constructor
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
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}
	
	public XMLConditionsDTO getConditions() {
        return this.conditions;
	}
	
	public void setConditions(XMLConditionsDTO conditions) {
		this.conditions = conditions;
	}
}
