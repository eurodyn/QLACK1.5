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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.eurodyn.qlack2.fuse.search.api.IndexService;
import com.eurodyn.qlack2.fuse.search.api.dto.indexing.IndexDTO;
import com.eurodyn.qlack2.fuse.search.api.dto.indexing.ReferenceDataDTO;
import com.eurodyn.qlack2.fuse.search.impl.util.SearchConstant;

public class IndexServiceImpl implements IndexService {

	private static Client client = null;

	// timeout for elasticsearch operations
	private static final int ELASTICSEARCH_TIMEOUT_MILLIS = 10000;

	private static final Logger LOGGER = Logger
			.getLogger(IndexServiceImpl.class.getName());

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
	public void index(IndexDTO indexDTO) {

		// TODO can sub collection be null??
		// then with what type where to store it?
		// else make it mandatory in IndexDTO
		// same problem in delete

		Map<String, Object> json = new HashMap<String, Object>();

		// index main information
		json.put(SearchConstant.MAIN_DATA, indexDTO.getMainData());

		// index reference data
		if (indexDTO.getReferenceDataList() != null) {
			for (ReferenceDataDTO referenceDTO : indexDTO
					.getReferenceDataList()) {
				json.put(referenceDTO.getName(), referenceDTO.getValue());

			}
		}

		client.prepareIndex(indexDTO.getCollection(),
				indexDTO.getSubCollection(), indexDTO.getObjectId())
				.setSource(json).execute()
				.actionGet(ELASTICSEARCH_TIMEOUT_MILLIS);

	}

	@Override
	public void update(IndexDTO indexDTO) {
		index(indexDTO);
	}

	@Override
	public void delete(IndexDTO indexDTO) {
		client.prepareDelete(indexDTO.getCollection(),
				indexDTO.getSubCollection(), indexDTO.getObjectId()).execute()
				.actionGet(ELASTICSEARCH_TIMEOUT_MILLIS);
	}

	public static void main(String... args) {

		IndexServiceImpl instance = new IndexServiceImpl();
		instance.init();

		IndexDTO indexDTO = new IndexDTO();
		indexDTO.setCollection("coll");
		indexDTO.setSubCollection("sub");
		indexDTO.setMainData("Oracle MongoDB mySQL");
		indexDTO.setObjectId("4");

		List<ReferenceDataDTO> refList = new ArrayList<>();
		ReferenceDataDTO dto = new ReferenceDataDTO();
		dto.setName("REFERENCE_OWNER_ID");
		dto.setValue("apostolos");
		refList.add(dto);
		ReferenceDataDTO dto2 = new ReferenceDataDTO();
		dto2.setName("REF_LAST_MOD_DATE");
		dto2.setValue(new Date());
		refList.add(dto2);
		indexDTO.setReferenceDataList(refList);

		instance.index(indexDTO);

		instance.destroy();
	}

}
