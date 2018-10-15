package com.eurodyn.qlack2.be.forms.api.dto.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XmlTranslationValuesDTO {

	private List<XmlTranslationValueDTO> values;

	@XmlElement(name = "value")
	public List<XmlTranslationValueDTO> getValues() {
		return values;
	}

	public void setValues(List<XmlTranslationValueDTO> values) {
		this.values = values;
	}


}
