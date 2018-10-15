package com.eurodyn.qlack2.webdesktop.apps.sampleapp.web.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;

public class CarRDTO {
	@NotEmpty
	@Pattern(regexp = ".*?ed.*")
	private String make;
	@NotEmpty
	private String model;
	@Min(1940)
	private int year;
	public String getMake() {
		return make;
	}
	public void setMake(String make) {
		this.make = make;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
}
