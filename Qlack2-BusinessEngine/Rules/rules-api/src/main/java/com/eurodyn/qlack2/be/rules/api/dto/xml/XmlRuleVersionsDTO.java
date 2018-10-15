package com.eurodyn.qlack2.be.rules.api.dto.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XmlRuleVersionsDTO {
	private List<XmlRuleVersionDTO> ruleVersions;

	@XmlElement(name = "ruleVersion")
	public List<XmlRuleVersionDTO> getRuleVersions() {
		return ruleVersions;
	}

	public void setRuleVersions(List<XmlRuleVersionDTO> ruleVersions) {
		this.ruleVersions = ruleVersions;
	}

}
