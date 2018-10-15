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
package com.eurodyn.qlack2.fuse.aaa.impl.listeners;

import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.eurodyn.qlack2.fuse.aaa.api.OpTemplateService;
import com.eurodyn.qlack2.fuse.aaa.api.OperationService;

public class OperationsListener implements EventHandler {
	private final static Logger LOGGER = Logger.getLogger(OperationsListener.class.getName());
	private EntityManager em;
	private BundleContext context;
	private TransactionManager transactionManager;
	private OperationService operationService;
	private OpTemplateService templateService;
	
	private ExecutorService executorService;

	public void setEm(EntityManager em) {
		this.em = em;
	}

	public void setContext(BundleContext context) {
		this.context = context;
	}

	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}

	public void setTemplateService(OpTemplateService templateService) {
		this.templateService = templateService;
	}

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

	@Override
	public void handleEvent(Event event) {
		Bundle bundle = (Bundle) event.getProperty("bundle");
		String bundleSymbolicName = (String) event.getProperty("bundle.symbolicName");
		processBundle(bundle);
	}

	public void processBundle(Bundle bundle) {
		Enumeration<URL> entries = bundle
				.findEntries("OSGI-INF", "qlack-aaa-operations.yaml", false);
		if ((entries != null) && (entries.hasMoreElements())) {
			URL url = entries.nextElement();
			executorService.execute(new OperationsHandler(em, transactionManager, operationService,
					templateService, bundle.getSymbolicName(), url));

		}
	}

}
