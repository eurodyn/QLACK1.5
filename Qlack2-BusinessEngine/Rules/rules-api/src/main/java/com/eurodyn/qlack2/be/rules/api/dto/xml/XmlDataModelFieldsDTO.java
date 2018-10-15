package com.eurodyn.qlack2.be.rules.api.dto.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XmlDataModelFieldsDTO {
	private List<XmlDataModelFieldDTO> fields;

	@XmlElement(name = "field")
	public List<XmlDataModelFieldDTO> getFields() {
		return fields;
	}

	public void setFields(List<XmlDataModelFieldDTO> fields) {
		this.fields = fields;
	}

}
