package com.eurodyn.qlack2.be.rules.api.dto.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XmlLibraryVersionsDTO {
	private List<XmlLibraryVersionDTO> libraryVersions;

	@XmlElement(name = "libraryVersion")
	public List<XmlLibraryVersionDTO> getLibraryVersions() {
		return libraryVersions;
	}

	public void setLibraryVersions(List<XmlLibraryVersionDTO> libraryVersions) {
		this.libraryVersions = libraryVersions;
	}

}
