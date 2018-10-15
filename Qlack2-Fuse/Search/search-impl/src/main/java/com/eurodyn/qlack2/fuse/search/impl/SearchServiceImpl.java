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
package com.eurodyn.qlack2.fuse.search.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;

import com.eurodyn.qlack2.fuse.search.api.SearchService;
import com.eurodyn.qlack2.fuse.search.api.dto.indexing.ReferenceDataDTO;
import com.eurodyn.qlack2.fuse.search.api.dto.search.SearchDateRangeDTO;
import com.eurodyn.qlack2.fuse.search.api.dto.search.SearchParametersDTO;
import com.eurodyn.qlack2.fuse.search.api.dto.search.SearchResultsDTO;
import com.eurodyn.qlack2.fuse.search.impl.util.SearchConstant;

public class SearchServiceImpl implements SearchService {

	private static Client client = null;

	// timeout for elasticsearch operations
	private static final int ELASTICSEARCH_TIMEOUT_MILLIS = 10000;

	private static final Logger LOGGER = Logger
			.getLogger(SearchServiceImpl.class.getName());

	public void init() {
		// TODO cluster name from property same as in elasticsearch bundle
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", "KARAF").build();
		// TODO address from property same as in elasticsearch bundle
		client = new TransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(
						"localhost", 9300));
	}

	public void destroy() {
		client.close();
	}

	@Override
	public List<SearchResultsDTO> query(SearchParametersDTO[] searchDTOs,
			int maxResults) {
		List<SearchResultsDTO> results = new ArrayList<>();

		for (SearchParametersDTO searchDTO : searchDTOs) {

			SearchRequestBuilder builder = null;

			// search in given index else an all indexes
			if (searchDTO.getCollection() != null) {
				builder = client.prepareSearch(searchDTO.getCollection());
			} else {
				builder = client.prepareSearch();
			}

			// search for given type
			if (searchDTO.getSubCollection() != null) {
				builder = builder.setTypes(searchDTO.getSubCollection());
			}

			StringBuilder query = new StringBuilder();
			// main search term
			query.append(searchDTO.getSearchQuery());
			// is + needed in search query (see next line)??
			// query.append('+').append(searchDTO.getSearchQuery());

			// add more search terms
			List<ReferenceDataDTO> filterReferenceDatas = searchDTO
					.getFilterParameters();
			if (filterReferenceDatas != null) {
				for (ReferenceDataDTO referenceDataDTO : filterReferenceDatas) {
					query.append(" AND ").append(referenceDataDTO.getName())
							.append(':').append(referenceDataDTO.getValue());
				}
			}

			builder = builder.setQuery(QueryBuilders.queryString(query
					.toString()));

			// filter with date
			SearchDateRangeDTO searchDateRangeDTO = searchDTO.getDateRange();
			if (searchDateRangeDTO != null) {
				builder = builder.setFilter(FilterBuilders
						.rangeFilter(searchDateRangeDTO.getName())
						.from(searchDateRangeDTO.getDateAfter())
						.to(searchDateRangeDTO.getDateBefore()));
			}

			// set last options and execute search
			SearchResponse response = builder
					.addHighlightedField(SearchConstant.MAIN_DATA)
					.setSize(maxResults)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH).execute()
					.actionGet(ELASTICSEARCH_TIMEOUT_MILLIS);

			// parse response
			if (response.getHits().getTotalHits() > 0) {
				SearchHit[] hits = response.getHits().getHits();

				for (SearchHit searchHit : hits) {
					SearchResultsDTO searchResultsDTO = new SearchResultsDTO();

					// get highlighted fields
					Map<String, HighlightField> highlightFields = searchHit
							.getHighlightFields();

					HighlightField highlightField = highlightFields
							.get(SearchConstant.MAIN_DATA);

					if (highlightField != null) {
						Text[] fragments = highlightField.getFragments();
						if (fragments.length > 0) {
							searchResultsDTO.setMainData(highlightField
									.getFragments()[0].toString());
						}
					}

					// get source fields
					Map<String, Object> sourceMap = searchHit.getSource();

					/*
					 * Map<String, SearchHitField> sourceMap2 = searchHit
					 * .getFields(); // TODO see if it best to use this
					 */

					// if the main field was not found in the highlighted
					// fields
					if (searchResultsDTO.getMainData() == null) {
						searchResultsDTO.setMainData((String) sourceMap
								.get(SearchConstant.MAIN_DATA));
					}

					// return the fields that were requested
					if (searchDTO.getReturnedDataNames() != null) {
						searchResultsDTO
								.setReferenceData(new HashMap<String, String>());
						for (String name : searchDTO.getReturnedDataNames()) {
							searchResultsDTO.getReferenceData().put(name,
									(String) sourceMap.get(name));
						}
					}

					// return general data
					searchResultsDTO.setObjectId(searchHit.getId());
					searchResultsDTO.setSubCollection(searchHit.getType());

					results.add(searchResultsDTO);
				}
			}

		}

		return results;
	}

	@Override
	public List<SearchResultsDTO> quickQuery(String searchQuery, int maxResults) {
		SearchParametersDTO searchDTO = new SearchParametersDTO();
		searchDTO.setSearchQuery(searchQuery);

		return query(new SearchParametersDTO[] { searchDTO }, maxResults);
	}

	public static void main(String... args) throws ParseException, IOException {

		SearchServiceImpl instance = new SearchServiceImpl();
		instance.init();

		SearchParametersDTO[] searchDTOArray = new SearchParametersDTO[1];
		SearchParametersDTO dto = new SearchParametersDTO();

		// dto.setCollection("coll");
		// dto.setSubCollection("sub");
		dto.setSearchQuery("mongo*");

		List<ReferenceDataDTO> filterParameters = new ArrayList<>();
		ReferenceDataDTO filter = new ReferenceDataDTO();
		filter.setName("REFERENCE_OWNER_ID");
		filter.setValue("apost*");
		filterParameters.add(filter);
		dto.setFilterParameters(filterParameters);

		SearchDateRangeDTO dateRange = new SearchDateRangeDTO();
		dateRange.setName("REF_LAST_MOD_DATE");
		dateRange.setDateAfter((new SimpleDateFormat("yyyyMMdd"))
				.parse("20130101"));
		dateRange.setDateBefore((new SimpleDateFormat("yyyyMMdd"))
				.parse("20150101"));
		dto.setDateRange(dateRange);

		List<String> returnedDataNames = new ArrayList<>();
		returnedDataNames.add("REFERENCE_OWNER_ID");
		dto.setReturnedDataNames(returnedDataNames);

		searchDTOArray[0] = dto;

		List<SearchResultsDTO> resp = instance.query(searchDTOArray, 100);
		for (SearchResultsDTO searchResultsDTO : resp) {
			System.out.println(searchResultsDTO);
		}

		instance.destroy();
	}

}
