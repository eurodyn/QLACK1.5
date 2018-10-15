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
package com.eurodyn.qlack2.fuse.search.api;

import java.util.List;

import com.eurodyn.qlack2.fuse.search.api.dto.search.SearchParametersDTO;
import com.eurodyn.qlack2.fuse.search.api.dto.search.SearchResultsDTO;

public interface SearchService {

	/**
	 * Performs a search in the index according to the search parameters.
	 * 
	 * @param searchDTOs
	 *            DTO containing the search parameters
	 * @param maxResults
	 *            max results requested by calling component
	 * @return SearchResultsDTO containing search results.
	 */
	List<SearchResultsDTO> query(SearchParametersDTO[] searchDTOs,
			int maxResults);

	/**
	 * Performs a quick search.
	 * 
	 * @param searchQuery
	 *            the query to search with
	 * @param maxResults
	 *            max results requested by calling component
	 * @return SearchResultsDTO containing search results.
	 */
	List<SearchResultsDTO> quickQuery(String searchQuery, int maxResults);

}
