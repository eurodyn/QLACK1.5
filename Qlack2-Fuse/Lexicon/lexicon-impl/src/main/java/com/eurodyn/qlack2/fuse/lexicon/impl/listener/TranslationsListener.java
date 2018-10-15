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
package com.eurodyn.qlack2.fuse.lexicon.impl.listener;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import com.eurodyn.qlack2.fuse.lexicon.api.GroupService;
import com.eurodyn.qlack2.fuse.lexicon.api.KeyService;
import com.eurodyn.qlack2.fuse.lexicon.api.LanguageService;
import com.eurodyn.qlack2.fuse.lexicon.api.dto.GroupDTO;
import com.eurodyn.qlack2.fuse.lexicon.api.dto.KeyDTO;
import com.eurodyn.qlack2.fuse.lexicon.api.dto.LanguageDTO;
import com.eurodyn.qlack2.fuse.lexicon.impl.model.Application;

public class TranslationsListener implements EventHandler {
	private final static Logger LOGGER = Logger.getLogger(TranslationsListener.class.getName());
	private EntityManager em;
	private GroupService groupService;
	private LanguageService languageService;
	private KeyService keyService;
	private BundleContext context;
	private TransactionManager transactionManager;
	
	private ExecutorService executorService;


	public void setEm(EntityManager em) {
		this.em = em;
	}

	public void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	public void setLanguageService(LanguageService languageService) {
		this.languageService = languageService;
	}

	public void setKeyService(KeyService keyService) {
		this.keyService = keyService;
	}

	public void setContext(BundleContext context) {
		this.context = context;
	}

	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Runs on bundle initialization and checks if any existing active
	 * bundles in the OSGi container have translations that need
	 * to be processed.
	 * @throws SystemException
	 * @throws NotSupportedException
	 * @throws javax.transaction.SystemException
	 */
	public void init() throws NotSupportedException, SystemException {
		executorService = Executors.newFixedThreadPool(1);
		
		Bundle[] existingBundles = context.getBundles();
		for (Bundle bundle : existingBundles) {
			if (bundle.getState() == Bundle.ACTIVE) {
				processBundle(bundle);
			}
		}
	}
	
	public void destroy() {
		executorService.shutdown();
	}

	/**
	 * Listens for started bundles and checks if they have translations
	 * that needs to be processed.
	 */
	@Override
	public void handleEvent(Event event) {
		Bundle bundle = (Bundle) event.getProperty("bundle");
		String bundleSymbolicName = (String) event.getProperty("bundle.symbolicName");
		processBundle(bundle);
	}

	public void processBundle(Bundle bundle) {
		Enumeration<URL> entries = bundle
				.findEntries("OSGI-INF", "qlack-lexicon.yaml", false);
		if ((entries != null) && (entries.hasMoreElements())) {
			URL url = entries.nextElement();
			executorService.execute(new TranslationsHandler(em, groupService, languageService, 
					keyService, transactionManager, bundle.getSymbolicName(), url));
		}
	}

}
