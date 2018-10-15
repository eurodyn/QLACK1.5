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
import java.util.List;

import com.eurodyn.qlack2.fuse.search.api.dto.indexing.ReferenceDataDTO;

public class SearchParametersDTO implements Serializable {

	private static final long serialVersionUID = 5733458612061963580L;

	// Name of the collection to search in (ex. Blog, SIMM, etc.)
	private String collection;

	// Name of the sub collection to search in (ex. Blog post, SIMM activity,
	// etc.)
	private String subCollection;

	// Restrict searches to only return results from a limited subset of
	// documents
	// Map contains column name as key and condition as value.
	private List<ReferenceDataDTO> filterParameters;

	// The additional data of the results to return.Without this parameter only
	// the
	// main data, subcollection id and object id will be returned for each
	// result.
	private List<String> returnedDataNames;

	private SearchDateRangeDTO dateRange;

	// User search query
	private String searchQuery;

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getSearchQuery() {
		return searchQuery;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

	public List<ReferenceDataDTO> getFilterParameters() {
		return filterParameters;
	}

	public void setFilterParameters(List<ReferenceDataDTO> filterParameters) {
		this.filterParameters = filterParameters;
	}

	public List<String> getReturnedDataNames() {
		return returnedDataNames;
	}

	public void setReturnedDataNames(List<String> returnedDataNames) {
		this.returnedDataNames = returnedDataNames;
	}

	public String getSubCollection() {
		return subCollection;
	}

	public void setSubCollection(String subCollection) {
		this.subCollection = subCollection;
	}

	public SearchDateRangeDTO getDateRange() {
		return dateRange;
	}

	public void setDateRange(SearchDateRangeDTO dateRange) {
		this.dateRange = dateRange;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SearchParametersDTO [collection=").append(collection)
				.append(", subCollection=").append(subCollection)
				.append(", filterParameters=").append(filterParameters)
				.append(", returnedDataNames=").append(returnedDataNames)
				.append(", dateRange=").append(dateRange)
				.append(", searchQuery=").append(searchQuery).append("]");
		return builder.toString();
	}

}
