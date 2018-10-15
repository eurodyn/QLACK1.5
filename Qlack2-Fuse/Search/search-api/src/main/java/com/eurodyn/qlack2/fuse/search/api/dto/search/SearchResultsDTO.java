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
import java.util.Map;

public class SearchResultsDTO implements Serializable {

	private static final long serialVersionUID = 9153188566255747922L;
	private String mainData;
	private String subCollection;
	private String objectId;
	private Map<String, String> referenceData;

	public String getMainData() {
		return mainData;
	}

	public void setMainData(String mainData) {
		this.mainData = mainData;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getSubCollection() {
		return subCollection;
	}

	public void setSubCollection(String subCollection) {
		this.subCollection = subCollection;
	}

	public Map<String, String> getReferenceData() {
		return referenceData;
	}

	public void setReferenceData(Map<String, String> referenceData) {
		this.referenceData = referenceData;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SearchResultsDTO [mainData=").append(mainData)
				.append(", subCollection=").append(subCollection)
				.append(", objectId=").append(objectId)
				.append(", referenceData=").append(referenceData).append("]");
		return builder.toString();
	}

}