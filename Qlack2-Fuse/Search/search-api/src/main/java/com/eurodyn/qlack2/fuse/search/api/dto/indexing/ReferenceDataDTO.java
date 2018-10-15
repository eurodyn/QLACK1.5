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
package com.eurodyn.qlack2.fuse.search.api.dto.indexing;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * This class contains whether the reference field is to indexed or stored.
 * Field should be stored if its required in the results and indexed for adding
 * in search query.
 * 
 **/
public class ReferenceDataDTO implements Serializable {

	private static final long serialVersionUID = -3257649035551870404L;
	private String name;
	private Object value;

	public ReferenceDataDTO() {
	};

	public ReferenceDataDTO(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public ReferenceDataDTO(String name, Date date) {
		this.name = name;
		this.value = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStringValue() {
		return value.toString();
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReferenceDataDTO [name=").append(name)
				.append(", value=").append(value).append("]");
		return builder.toString();
	}

}
