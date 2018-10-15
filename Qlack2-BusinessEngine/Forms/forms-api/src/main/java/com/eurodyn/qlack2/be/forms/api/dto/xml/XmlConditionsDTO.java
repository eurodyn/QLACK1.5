package com.eurodyn.qlack2.be.forms.api.dto.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XmlConditionsDTO {
	private List<XmlConditionDTO> conditions;

	@XmlElement(name = "condition")
	public List<XmlConditionDTO> getConditions() {
		return conditions;
	}

	public void setConditions(List<XmlConditionDTO> conditions) {
		this.conditions = conditions;
	}

}
