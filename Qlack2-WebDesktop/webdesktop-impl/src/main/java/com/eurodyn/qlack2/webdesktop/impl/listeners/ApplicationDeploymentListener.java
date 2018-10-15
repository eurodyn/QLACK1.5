package com.eurodyn.qlack2.webdesktop.impl.listeners;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.osgi.framework.Bundle;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import com.eurodyn.qlack2.fuse.aaa.api.ResourceService;
import com.eurodyn.qlack2.fuse.aaa.api.dto.ResourceDTO;
import com.eurodyn.qlack2.webdesktop.api.dto.ApplicationInfo;
import com.eurodyn.qlack2.webdesktop.impl.model.Application;

public class ApplicationDeploymentListener implements EventHandler {
	private Logger LOGGER = Logger.getLogger(ApplicationDeploymentListener.class.getName());
	private EntityManager em;
	private ResourceService resourceService;

	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

	public void setResourceService(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	@Override
	public void handleEvent(Event event) {
		Bundle bundle = (Bundle) event.getProperty("bundle");
		String symbolicName = (String) event.getProperty("bundle.symbolicName");
		LOGGER.log(Level.FINEST, "Web Desktop application listener at deployment of: {0}.",
				symbolicName);
		
		Enumeration<URL> entries = bundle
				.findEntries("OSGI-INF", "wd-app.yaml", false);
		if ((entries != null) && (entries.hasMoreElements())) {
			LOGGER.log(Level.FINE, "Web Desktop application identified");
			URL url = entries.nextElement();
			try {
				Yaml yaml = new Yaml(new CustomClassLoaderConstructor(getClass().getClassLoader()));
				ApplicationInfo appInfo = yaml.loadAs(url.openStream(), ApplicationInfo.class);
				
				// Check if the application is already in the WebDesktopDB and 
				// if yes check that it is not currently registered through another 
				// service. If the application is already in the DB and not 
				// currently registered through another service then update the 
				// implementing service ID and all the application data that 
				// should not be persistent between application deployments.
				Application application = em.find(Application.class, appInfo.getIdentification().getUniqueId());
				if (application == null) {
					LOGGER.log(Level.FINE, "Application with UUID {0} deployed for the fist time.",
							appInfo.getIdentification().getUniqueId());
					application = new Application();
					application.setAppUuid(appInfo.getIdentification().getUniqueId());
					application.setRestrictAccess(appInfo.getInstantiation().getRestrictAccess());
					application.setActive(appInfo.isActive());
					application.setAddedOn(Calendar.getInstance().getTime());
					
					// Create a AAA resource corresponding to the application to allow setting
					// user access.
					ResourceDTO appResource = new ResourceDTO();
					appResource.setName(appInfo.getInstantiation().getPath());
					appResource.setDescription("Web desktop application");
					appResource.setObjectID(appInfo.getIdentification().getUniqueId());
					resourceService.createResource(appResource);
				} else if ((application.getBundleSymbolicName() != null) 
						&& (!application.getBundleSymbolicName().equals(symbolicName))) {
					LOGGER.log(Level.WARNING, "Attempted to register bundle {0}" +
							" but application with UUID {1} " +
							"is already registered by bundle {2}. " +
							"This service will not be taken into account", 
							new String[]{symbolicName,
									appInfo.getIdentification().getUniqueId(), 
									application.getBundleSymbolicName()});
				}
				application.setTitleKey(appInfo.getIdentification().getTitleKey());
				application.setDescriptionKey(appInfo.getIdentification().getDescriptionKey());
				application.setVersion(appInfo.getIdentification().getVersion());
				application.setPath(appInfo.getInstantiation().getPath());
				application.setIndex(appInfo.getInstantiation().getIndex());
				application.setConfigPath(appInfo.getInstantiation().getConfigPath());
				application.setTranslationsGroup(appInfo.getInstantiation().getTranslationsGroup());
				application.setMultipleInstances(appInfo.getInstantiation().getMultipleInstances());
				application.setIcon(appInfo.getMenu().getIcon());
				application.setIconSmall(appInfo.getMenu().getIconSmall());
				application.setDisplayOn(appInfo.getMenu().getType().getDisplayOn());
				application.setGroupKey(appInfo.getMenu().getType().getGroupKey());
				application.setWidth(appInfo.getWindow().getWidth());
				application.setMinWidth(appInfo.getWindow().getMinWidth());
				application.setHeight(appInfo.getWindow().getHeight());
				application.setMinHeight(appInfo.getWindow().getMinHeight());
				application.setResizable(appInfo.getWindow().isResizable());
				application.setMaximizable(appInfo.getWindow().isMaximizable());
				application.setMinimizable(appInfo.getWindow().isMinimizable());
				application.setDraggable(appInfo.getWindow().isDraggable());
				application.setClosable(appInfo.getWindow().isClosable());
				application.setLastDeployedOn(Calendar.getInstance().getTime());
				application.setBundleSymbolicName(symbolicName);
				em.persist(application);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, MessageFormat.format("Could not read application yaml" +
						"for bundle {0}.", symbolicName), e);
			}
		} 
	}

}
