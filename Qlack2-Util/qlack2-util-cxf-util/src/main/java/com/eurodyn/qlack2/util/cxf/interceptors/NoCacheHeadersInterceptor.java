package com.eurodyn.qlack2.util.cxf.interceptors;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.cxf.interceptor.AbstractOutDatabindingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

/**
 * An interceptor that adds a "Cache-Control" in the HTTP Headers of every
 * response, so that the responses won't be cached by the browsers.
 * <p>
 * This interceptor is used mainly to avoid the aggressive caching made by IE
 * for GET requests.
 * </p>
 * 
 * @author European Dynamics SA
 * 
 */
public class NoCacheHeadersInterceptor extends
		AbstractOutDatabindingInterceptor {

	public NoCacheHeadersInterceptor() {
		super(Phase.MARSHAL);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void handleMessage(Message message) throws Fault {

		Map<String, List<String>> headers = (Map<String, List<String>>) message
				.get(Message.PROTOCOL_HEADERS);
		if (headers == null) {
			headers = new TreeMap<String, List<String>>(
					String.CASE_INSENSITIVE_ORDER);
			message.put(Message.PROTOCOL_HEADERS, headers);
			headers.put(
					"Cache-Control",
					Arrays.asList(new String[] { "max-age=0, no-cache, must-revalidate, proxy-revalidate" }));
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleFault(Message message) {
		handleMessage(message);
	}
}