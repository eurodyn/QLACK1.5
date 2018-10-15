package com.eurodyn.qlack2.webdesktop.impl.listeners;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.eurodyn.qlack2.webdesktop.impl.model.Application;

public class ApplicationUndeploymentListener implements EventHandler {
	private static final Logger LOGGER = Logger.getLogger(ApplicationUndeploymentListener.class.getName());
	private EntityManager em;

	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

	@Override
	public void handleEvent(Event event) {
		String symbolicName = (String) event.getProperty("bundle.symbolicName");
		LOGGER.log(Level.FINEST, "Web Desktop application listener at undeployment of: {0}.",
				symbolicName);
		
		Application application = Application.getApplicationForSymbolicName(symbolicName, em);
		if (application != null) {
			application.setBundleSymbolicName(null);
			em.persist(application);
		}
		LOGGER.log(Level.FINE, "Unregistered application from bundle {0}.",
				symbolicName);
	}

}
