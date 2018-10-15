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
package com.eurodyn.qlack2.fuse.caching.client.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.OperationTimeoutException;

import org.apache.commons.lang3.StringUtils;

import com.eurodyn.qlack2.fuse.caching.client.api.CacheService;

public class CacheServiceImpl implements CacheService {
	public static final Logger LOGGER = Logger.getLogger(CacheServiceImpl.class.getName());
	private MemcachedClient cache;
	private int directPort;
	private String directIp;
	private String localIp;
	private Long expirationTime;
	private Boolean active;

	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}

	public void setCache(MemcachedClient cache) {
		this.cache = cache;
	}

	public void setDirectPort(int directPort) {
		this.directPort = directPort;
	}

	public String getDirectIp() {
		return directIp;
	}

	public void setDirectIp(String directIp) {
		this.directIp = directIp;
	}

	public void setExpirationTime(Long expirationTime) {
		this.expirationTime = expirationTime;
	}

	public MemcachedClient getCache() {
		return cache;
	}

	public int getDirectPort() {
		return directPort;
	}

	public String getLocalIp() {
		return localIp;
	}
	
	/**
	 * @return the active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(Boolean active) {
		this.active = active;
	}

	public void init() {
		try {
			if (active) {
				cache = new MemcachedClient(new BinaryConnectionFactory(),
						AddrUtil.getAddresses((StringUtils.isNotBlank(directIp)
							? directIp : "localhost")  + ":" + directPort));
				LOGGER.fine(MessageFormat.format(
						"Using cache server {0}:{1}.",
						directIp, String.valueOf(directPort)));
			} else {
				LOGGER.fine("Cache deactiveted.");
			}

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Cache client could not be initialised.", e);
		}
	}

	public void destroy() {
		if (cache != null) {
			cache.shutdown();
			cache = null;
		}
	}

	@Override
	public void set(String key, Object value) {
		set(key, value, System.currentTimeMillis() + expirationTime);
	}

	@Override
	public void set(String key, Object value, long expiresAtEpoch) {
		// Memcache expresses EPOCH in seconds, therefore we need
		// to convert the msec passed EPOCH.
		try {
			if (cache != null) {
				int expiresAtEpochSec = (int)(expiresAtEpoch / 1000l);
				cache.set(key, expiresAtEpochSec, value);
				LOGGER.log(Level.FINEST, "Added to memcached key {0}, with "
						+ "value {1}.", new String[]{key, value.toString()});
			}
		} catch (OperationTimeoutException | CancellationException e) {
			// In case something went wrong we simply discard the message,
			// since a cache may not necessarily be available.
			LOGGER.log(Level.FINEST,
					"Could not add the key to the cache.", e);
		}
	}

	@Override
	public void set(String namespace, String key, Object value) {
		set(namespace, key, value, expirationTime);
	}

	@Override
	public void set(String namespace, String key, Object value, long expiresAtEpoch) {
		set(namespace + "/" + key, value, expiresAtEpoch);
	}

	@Override
	public void delete(String key) {
		try {
			if (cache != null) {
				cache.delete(key);
				LOGGER.log(Level.FINEST, "Deleted from memcached key {0}.", key);
			}
		} catch (OperationTimeoutException | CancellationException e) {
			// In case something went wrong we simply discard the message,
			// since a cache may not necessarily be available.
			LOGGER.log(Level.FINEST,
					"Could not delete the key from the cache.", e);
		}
	}

	@Override
	public void delete(String namespace, String key) {
		delete(namespace + "/" + key);
	}

	@Override
	public void invalidate(String namespace) {
		delete(namespace);
	}

	@Override
	public Object get(String key) {
		Object retVal = null;
		try {
			if (cache != null) {
				retVal = cache.get(key);
			}			
		} catch (OperationTimeoutException | CancellationException e) {
			// In case something went wrong we simply discard the message,
			// since a cache may not necessarily be available.
			LOGGER.log(Level.FINEST,
					"Could not get the key to the cache.", e);
		}

		return retVal;
	}

	@Override
	public Object get(String namespace, String key) {
		return get(namespace + "/" + key);
	}

	@Override
	public Object get(String key, Object defaultValue) {
		Object retVal = get(key);

		return retVal != null ? retVal : defaultValue;
	}

	@Override
	public Object get(String namespace, String key, Object defaultValue) {
		return get(namespace + "/" + key, defaultValue);
	}



}
