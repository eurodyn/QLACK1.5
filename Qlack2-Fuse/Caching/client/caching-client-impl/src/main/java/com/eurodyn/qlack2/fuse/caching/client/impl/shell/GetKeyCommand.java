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
package com.eurodyn.qlack2.fuse.caching.client.impl.shell;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.eurodyn.qlack2.fuse.caching.client.api.CacheService;

@Command(scope = "qlack", name = "cache-client-get-key", description = "Returns the value of a key.")
public final class GetKeyCommand extends OsgiCommandSupport {
	@Argument(index=0, name="key", description = "The name of the key to lookup.", required = true, multiValued = false)
    private String key;
	private CacheService cacheService;

	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	
	@Override
	protected Object doExecute() {
		Object o = cacheService.get(key);
		if (o == null) {
			System.out.println("Key was not found.");
		} else {
			System.out.println(o);
		}

		return null;
	}
	
}
