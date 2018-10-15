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

import com.eurodyn.qlack2.fuse.search.api.dto.indexing.IndexDTO;

public interface IndexService {

	/**
	 * Index a document
	 * 
	 * @param indexDTO
	 */
	void index(IndexDTO indexDTO);

	/**
	 * Update a document in the index.
	 * 
	 * @param indexDTO
	 */
	void update(IndexDTO indexDTO);

	/**
	 * Delete a document from the index.
	 * 
	 * @param indexDTO
	 */
	void delete(IndexDTO indexDTO);

}
