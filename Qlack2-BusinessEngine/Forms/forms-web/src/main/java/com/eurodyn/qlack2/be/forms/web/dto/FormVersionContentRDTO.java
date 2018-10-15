package com.eurodyn.qlack2.be.forms.web.dto;

import org.hibernate.validator.constraints.NotEmpty;

import com.sun.istack.NotNull;

public class FormVersionContentRDTO {

	@NotNull
	@NotEmpty
	private String file;

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

}
