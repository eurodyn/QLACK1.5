package com.eurodyn.qlack2.webdesktop.apps.sampleapp.impl.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="sap_test")
public class SampleAppTable {

	@Id
	private String id;
	@Version
	private long dbversion;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

}
