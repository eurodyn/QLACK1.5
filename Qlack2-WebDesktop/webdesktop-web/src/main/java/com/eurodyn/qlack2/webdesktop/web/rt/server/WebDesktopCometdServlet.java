package com.eurodyn.qlack2.webdesktop.web.rt.server;

import javax.servlet.ServletException;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.server.BayeuxServerImpl;
import org.cometd.server.CometdServlet;

import com.eurodyn.qlack2.webdesktop.web.rt.init.BayeuxInitialiser;

public class WebDesktopCometdServlet extends CometdServlet implements WebDesktopCometdService {
	private static final long serialVersionUID = -6781726568429484560L;

	private BayeuxInitialiser initialiser;

	public void setInitialiser(BayeuxInitialiser initialiser) {
		this.initialiser = initialiser;
	}

	@Override
	public void init() throws ServletException {
		boolean initBayeux = getServletContext().getAttribute(BayeuxServer.ATTRIBUTE) == null;
		super.init();
		if (initBayeux) {
			BayeuxServerImpl bayeux = super.getBayeux();
			initialiser.init(bayeux);
		}
	}

	@Override
	public BayeuxServer getBayeuxServer() {
		return super.getBayeux();
	}

}
