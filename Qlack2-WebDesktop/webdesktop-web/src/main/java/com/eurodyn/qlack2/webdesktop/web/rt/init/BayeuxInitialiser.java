package com.eurodyn.qlack2.webdesktop.web.rt.init;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.cometd.bayeux.server.BayeuxServer;

import com.eurodyn.qlack2.webdesktop.web.rt.security.BayeuxSecurityPolicy;

public class BayeuxInitialiser {
	private static final Logger LOGGER = Logger.getLogger(BayeuxInitialiser.class.getName());

	private BayeuxSecurityPolicy securityPolicy;

	public void setSecurityPolicy(BayeuxSecurityPolicy securityPolicy) {
		this.securityPolicy = securityPolicy;
	}

	public void init(BayeuxServer server) {
		/* The acknowledge messages extension provides reliable, ordered
		 * messaging to the Bayeux protocol from server to client.*/
		server.addExtension(new org.cometd.server.ext.AcknowledgedMessagesExtension());

		/* The timestamp extension adds a timestamp to the message object
		 * for every message the client and/or server sends. It is a
		 * non-standard extension because it does not add the additional
		 * fields to the ext field, but to the message object itself.*/
		server.addExtension(new org.cometd.server.ext.TimestampExtension());

		/* The timesync extension uses the messages exchanged between a
		 * client and a server to calculate the offset between the client's
		 * clock and the server's clock.*/
		server.addExtension(new org.cometd.server.ext.TimesyncExtension());

		/* The activity extension monitors the activity of server sessions
		 * to disconnect them after a configurable period of inactivity.*/
//		server.addExtension(new ActivityExtension(ActivityExtension.Activity.CLIENT, 15 * 60 * 1000L));

		// Plug custom security implementation.
		server.setSecurityPolicy(securityPolicy);

		LOGGER.log(Level.INFO, "BayeuxServer configured.");
	}
}