package com.eurodyn.qlack2.webdesktop.apps.sampleapp.web.dto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

public class UserRDTO {
	@NotEmpty
	@Length(min = 5, max = 64)
	@Pattern(regexp = ".*?ed.*")
	private String fullname;

	@Min(value = 18)
	private int age;

	@NotEmpty
	private String gender;

	@Valid
	@NotNull
	private WebsiteRDTO website;

	@Valid
	@NotNull
	@NotEmpty
	private CarRDTO[] cars;

	@Min(0)
	private int sum;

	public int getSum() {
		return sum;
	}

	public void setSum(int sum) {
		this.sum = sum;
	}

	public CarRDTO[] getCars() {
		return cars;
	}

	public void setCars(CarRDTO[] cars) {
		this.cars = cars;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public WebsiteRDTO getWebsite() {
		return website;
	}

	public void setWebsite(WebsiteRDTO website) {
		this.website = website;
	}

}
