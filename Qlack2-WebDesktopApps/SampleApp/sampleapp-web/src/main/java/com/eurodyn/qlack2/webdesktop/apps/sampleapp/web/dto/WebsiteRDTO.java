package com.eurodyn.qlack2.webdesktop.apps.sampleapp.web.dto;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotEmpty;


public class WebsiteRDTO {
	@NotEmpty
	private String protocol;
	@NotEmpty
	private String address;
	@Min(1)
	private int port;

	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

}
