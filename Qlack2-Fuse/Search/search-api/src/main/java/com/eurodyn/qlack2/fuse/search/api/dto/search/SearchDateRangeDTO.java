/*
* Copyright 2014 EUROPEAN DYNAMICS SA <info@eurodyn.com>
*
* Licensed under the EUPL, Version 1.1 only (the "License").
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
* https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and
* limitations under the Licence.
*/
package com.eurodyn.qlack2.fuse.search.api.dto.search;

import java.io.Serializable;
import java.util.Date;

public class SearchDateRangeDTO implements Serializable {

	private static final long serialVersionUID = -3049207990667753725L;
	private String name;
	private Date startDate;
	private Date endDate;

	public SearchDateRangeDTO() {
	}

	public SearchDateRangeDTO(String name, Date dateBefore, Date dateAfter) {
		this.name = name;
		this.startDate = dateBefore;
		this.endDate = dateAfter;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDateBefore() {
		return startDate;
	}

	public void setDateBefore(Date dateBefore) {
		this.startDate = dateBefore;
	}

	public Date getDateAfter() {
		return endDate;
	}

	public void setDateAfter(Date dateAfter) {
		this.endDate = dateAfter;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SearchDateRangeDTO [name=").append(name)
				.append(", startDate=").append(startDate).append(", endDate=")
				.append(endDate).append("]");
		return builder.toString();
	}

}