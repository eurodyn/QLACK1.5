package com.eurodyn.qlack2.webdesktop.web.rt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerSession;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.eurodyn.qlack2.fuse.caching.client.api.CacheService;
import com.eurodyn.qlack2.webdesktop.web.rt.security.BayeuxSecurityPolicy;
import com.eurodyn.qlack2.webdesktop.web.rt.server.WebDesktopCometdService;
import com.eurodyn.qlack2.webdesktop.web.util.Constants;

/**
 * Listens for RT messages and delivers them to the appropriate recipient (or the
 * general public).
 * @author European Dynamics SA
 */
public class EventAdminListener implements EventHandler {
	private static final Logger LOGGER = Logger.getLogger(EventAdminListener.class.getName());

	private List<CacheService> cacheServiceList;

	private WebDesktopCometdService cometdService;

	public EventAdminListener() {
	}

	public void setCacheServiceList(List<CacheService> cacheServiceList) {
		this.cacheServiceList = cacheServiceList;
	}

	public void setCometdService(WebDesktopCometdService cometdService) {
		this.cometdService = cometdService;
	}

	/**
	 * A helper method to map a User ID to a CometD ID. This method first tries
	 * to obtain the mapping using the global cache service. If caching is not
	 * available, it tries to resolve the mapping using the static ConcurrentHashMap
	 * reference in BayeuxSecurityPolicy. Of course, the latter will not work
	 * if you have Web Desktop deployed on a cluster, where a cache service is
	 * required.
	 * @param userID
	 * @return The CometD ID associated with a particular user ID or null if such
	 * a mapping does not exist.
	 */
	private String mapUserIDToCometDID(String userID) {
		String retVal = null;
		if (cacheServiceList.size() > 0) {
			retVal = (String)cacheServiceList.get(0).get(Constants.CACHE_NS_USERTOCOMET, userID);
		} else {
			retVal = BayeuxSecurityPolicy.userIDToCometID.get(Constants.CACHE_NS_USERTOCOMET + userID);
		}

		return retVal;
	}

	@Override
	public void handleEvent(Event e) {
		LOGGER.log(Level.FINEST, "Got a message {0}.", e);
		String recipientID = e.getProperty("recipientID").toString();
		LOGGER.log(Level.FINEST, "Message recipientID={0}.", recipientID);

		// Check if this event is for a specific recipient or for
		// everybody or for the general public.
		if (StringUtils.isNotBlank(recipientID)) {
			// In order to forward this event to the correct recipient,
			// we should find the recipients CometD session ID.
			String cometID = mapUserIDToCometDID(recipientID);
			LOGGER.log(Level.FINEST, "CometID for recipient={0}", cometID);
			BayeuxServer server = cometdService.getBayeuxServer();
			ServerSession session = server.getSession(cometID);
			if (session != null && session.isConnected()) {
				session.deliver(null, "/service/private", unpackEvent(e), null);
				LOGGER.log(Level.FINEST, "Message delivered.");
			} else {
				LOGGER.log(Level.FINEST, "User ID {0} with CometD ID {1} is "
						+ "disconnected; message will not be delivered.",
						new String[]{recipientID, cometID});
			}
		} else {
			System.out.println("Message address to general public.");
		}
	}

	private Map<String, Object> unpackEvent(Event e) {
		Map<String, Object> retVal = new HashMap<>();
		for (String p : e.getPropertyNames()) {
			retVal.put(p, e.getProperty(p));
		}

		return retVal;
	}
}
