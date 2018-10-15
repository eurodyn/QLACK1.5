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
package com.eurodyn.qlack2.webdesktop.web.rt.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.DefaultSecurityPolicy;

import com.eurodyn.qlack2.fuse.caching.client.api.CacheService;
import com.eurodyn.qlack2.fuse.idm.api.IDMService;
import com.eurodyn.qlack2.fuse.idm.api.request.ValidateTicketRequest;
import com.eurodyn.qlack2.fuse.idm.api.signing.SignedTicket;
import com.eurodyn.qlack2.webdesktop.web.util.Constants;

public class BayeuxSecurityPolicy extends DefaultSecurityPolicy  implements ServerSession.RemoveListener {
	private static final Logger LOGGER = Logger.getLogger(BayeuxSecurityPolicy.class.getName());

	private ObjectMapper mapper;

	private IDMService idmService;

	private List<CacheService> cacheServiceList;

	public void setMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public void setIdmService(IDMService idmService) {
		this.idmService = idmService;
	}

	public void setCacheServiceList(List<CacheService> cacheServiceList) {
		this.cacheServiceList = cacheServiceList;
	}

	// This is a backup strategy to keep the mapping of User IDs with CometD
	// and ticket ID. This will work fine in a non-clustered environment event
	// if a Cache service is not available, however in a clustered environment
	// a Cache service is necessary.
	public static Map<String, String> userIDToCometID = new ConcurrentHashMap<>();
	public static Map<String, String> cometIDToUserID = new ConcurrentHashMap<>();
	public static Map<String, SignedTicket> userIDToTicket = new ConcurrentHashMap<>();

	private SignedTicket validateSignedTicket(SignedTicket signedTicket) {
		SignedTicket retVal = null;
		ValidateTicketRequest req = new ValidateTicketRequest(signedTicket);
		if (idmService.validateTicket(req).isValid()) {
			retVal = signedTicket;
		} else {
			LOGGER.log(Level.FINE, "Could not validate Cookie for "
				+ "a user presented as {0} due to ticket {1} found to"
				+ "be invalid.", new Object[] { signedTicket.getUserID(),
				signedTicket.getTicketID() });
		}

		return retVal;
	}

	@SuppressWarnings("unchecked")
	private SignedTicket getAndValidateSignedTicket(ServerMessage message)
			throws IOException {
		SignedTicket retVal = null;

		try {
			Map<String, Object> ext = message.getExt();
			if (ext != null) {
				Map<String, Object> auth = (Map<String, Object>)ext.get("authentication");
				if (auth != null) {
					String ticket =(String)auth.get("ticket");
					if (StringUtils.isNotEmpty(ticket)) {
						SignedTicket signedTicket = mapper.readValue(ticket, SignedTicket.class);
						retVal = validateSignedTicket(signedTicket);
					} else {
						LOGGER.log(Level.FINE, "Ticket found to be empty.");
					}
				}
			}
		} catch (RuntimeException e) {
			LOGGER.log(Level.SEVERE, "Could not interpret security ticket value.", e);
		}

		return retVal;
	}

	/*
	 * During handshake we establish the relation between the CometD session ID
	 * and the underling user ID. This way, we can send server-side message to a
	 * specific user.
	 */
	@Override
	public boolean canHandshake(BayeuxServer server, ServerSession session, ServerMessage message) {
		boolean retVal = false;
		SignedTicket signedTicket = null;

		try {
			// Local sessions (i.e. the ones initiated from the server-side) can
			// always handshake with no security restrictions.
			if (session.isLocalSession()) {
				retVal = true;
			} else {
				// Validate the user's ticket.
				try {
					signedTicket = getAndValidateSignedTicket(message);
					if (signedTicket != null) {
						retVal = super.canHandshake(server, session, message);
						LOGGER.log(Level.FINE,
							"User {0} performed handshake using CometD "
								+ "sesion {1}.", new Object[] {
								signedTicket.getUserID(), session.getId() });
					}
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "Could not extract SignedTicket for "
							+ "CometD handshake request.", e);
				}
			}

			// Add a listener to be notified when this session ends.
			// Update application caches with User ID <-> Comet ID, and
			// User ID -> Ticket.
			if (retVal && signedTicket != null) {
				session.addListener(this);
				if (cacheServiceList.isEmpty()) {
					userIDToCometID.put(Constants.CACHE_NS_USERTOCOMET +
							signedTicket.getUserID(), session.getId());
					cometIDToUserID.put(Constants.CACHE_NS_COMETTOUSER +
							session.getId(), signedTicket.getUserID());
					userIDToTicket.put(Constants.CACHE_NS_USERTOTICKET +
							signedTicket.getUserID(), signedTicket);
				} else {
					CacheService cacheService = cacheServiceList.get(0);
					cacheService.set(
						Constants.CACHE_NS_USERTOCOMET,
						signedTicket.getUserID(), session.getId(), Long.MAX_VALUE);
					cacheService.set(
							Constants.CACHE_NS_COMETTOUSER,
							session.getId(), signedTicket.getUserID(), Long.MAX_VALUE);
					cacheService.set(
							Constants.CACHE_NS_USERTOTICKET,
							signedTicket.getUserID(),
							mapper.writeValueAsString(signedTicket), Long.MAX_VALUE);
				}
			} else {
				System.out.println("Can't handshake.");
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Could not convert SignedTicket to JSON to "
					+ "store it in cache.", e);
		}

		return retVal;
	}

	@Override
	public boolean canSubscribe(BayeuxServer server, ServerSession session, ServerChannel channel, ServerMessage message) {
		boolean retVal = false;
		try {
			if (session.isLocalSession()) {
				return true;
			}
			// Since the subscription request does not contain a SignedTicket
			// we need to find the last SignedTicket this user used to handshake
			// with CometD.
			SignedTicket signedTicket = null;
			if (cacheServiceList.isEmpty()) {
				String userID = cometIDToUserID.get(
					Constants.CACHE_NS_COMETTOUSER + session.getId());
				signedTicket = userIDToTicket.get(
						Constants.CACHE_NS_USERTOTICKET + userID);
			} else {
				CacheService cacheService = cacheServiceList.get(0);
				String userId = (String) cacheService.get(
							Constants.CACHE_NS_COMETTOUSER,	session.getId());
				String ticket = (String) cacheService.get(
							Constants.CACHE_NS_USERTOTICKET, userId);
				signedTicket = mapper.readValue(ticket, SignedTicket.class);
			}
			signedTicket = validateSignedTicket(signedTicket);

			// Users can subscribe either to their private channel, or the
			// global public channel.
			if (signedTicket != null) {
				if (channel != null && channel.getId() != null
						&& (channel.getId().equals("/service/private") || channel.getId().equals("/public"))) {
					retVal = true;
				} else {
					LOGGER.log(Level.WARNING,
						"User {0} tried to subscribe to invalid "
							+ "channel {1}.", new Object[] {
							signedTicket.getUserID(), channel.getId() });
				}
				LOGGER.log(Level.FINE,
					"Subscription request for {0} (granted={1}).",
					new Object[] { channel.getId(), retVal });
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Could not parse JSON ticket from cache.", e);
		}

		return retVal;
	}

	@Override
	public void removed(ServerSession session, boolean timeout) {

	}
}
