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
import java.util.ArrayList;
import java.util.List;

public class IndexDTO implements Serializable {

	private static final long serialVersionUID = 2522996499843083999L;
	private String collection;
	private String subCollection;
	private String objectId;
	private String mainData;
	private List<ReferenceDataDTO> referenceDataList;

	public IndexDTO() {
	};

	public IndexDTO(String collection, String subCollection, String objectId,
			String mainData, List<ReferenceDataDTO> referenceDataList) {
		this.collection = collection;
		this.subCollection = subCollection;
		this.objectId = objectId;
		this.mainData = mainData;
		this.referenceDataList = referenceDataList;
	};

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getMainData() {
		return mainData;
	}

	public void setMainData(String mainData) {
		this.mainData = mainData;
	}

	public void addReferenceData(ReferenceDataDTO referenceData) {
		if (this.referenceDataList == null) {
			this.referenceDataList = new ArrayList<ReferenceDataDTO>();
			this.referenceDataList.add(referenceData);
		} else {
			this.referenceDataList.add(referenceData);
		}
	}

	public String getSubCollection() {
		return subCollection;
	}

	public void setSubCollection(String subCollection) {
		this.subCollection = subCollection;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public List<ReferenceDataDTO> getReferenceDataList() {
		return referenceDataList;
	}

	public void setReferenceDataList(List<ReferenceDataDTO> referenceDataList) {
		this.referenceDataList = referenceDataList;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IndexDTO [collection=").append(collection)
				.append(", subCollection=").append(subCollection)
				.append(", objectId=").append(objectId).append(", mainData=")
				.append(mainData).append(", referenceDataList=")
				.append(referenceDataList).append("]");
		return builder.toString();
	}

}