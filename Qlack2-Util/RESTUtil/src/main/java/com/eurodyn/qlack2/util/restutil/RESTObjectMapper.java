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
package com.eurodyn.qlack2.util.restutil;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

public class RESTObjectMapper extends ObjectMapper {
	
	public boolean serialiseNullValues;
	
	public RESTObjectMapper() {
		super();
	}
	
	public void setAcceptSingleValueAsArray(boolean state) {
		super.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, state);
	}
	
	public void setSerialiseNullValues(boolean b) {
		super.setSerializationInclusion(b ? Inclusion.ALWAYS : Inclusion.NON_EMPTY);
	}
}
