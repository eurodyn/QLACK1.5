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
package com.eurodyn.qlack2.fuse.caching.client.api;

public interface CacheService {
	void set(String key, Object value, long expiresAtEpoch);
	void set(String key, Object value);
	void set(String namespace, String key, Object value, long expiresAtEpoch);
	void set(String namespace, String key, Object value);
	void delete(String key);
	void delete(String namespace, String key);
	void invalidate(String namespace);
	Object get(String key);
	Object get(String namespace, String key);
	Object get(String key, Object defaultValue);
	Object get(String namespace, String key, Object defaultValue);
}
