package com.eurodyn.qlack2.fuse.aaa.impl.listeners;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import com.eurodyn.qlack2.fuse.aaa.api.OpTemplateService;
import com.eurodyn.qlack2.fuse.aaa.api.OperationService;
import com.eurodyn.qlack2.fuse.aaa.api.dto.OpTemplateDTO;
import com.eurodyn.qlack2.fuse.aaa.api.dto.OperationDTO;
import com.eurodyn.qlack2.fuse.aaa.impl.model.Application;

public class OperationsHandler implements Runnable {
	private final static Logger LOGGER = Logger
			.getLogger(OperationsListener.class.getName());
	private EntityManager em;
	private TransactionManager transactionManager;
	private OperationService operationService;
	private OpTemplateService templateService;
	private String symbolicName;
	private URL yamlUrl;

	public OperationsHandler(EntityManager em,
			TransactionManager transactionManager,
			OperationService operationService, OpTemplateService templateService,
			String symbolicName, URL yamlUrl) {
		super();
		this.em = em;
		this.transactionManager = transactionManager;
		this.operationService = operationService;
		this.templateService = templateService;
		this.symbolicName = symbolicName;
		this.yamlUrl = yamlUrl;
	}

	@Override
	public void run() {
		handle(symbolicName, yamlUrl);
	}

	public void handle(String symbolicName, URL yamlUrl) {
		LOGGER.log(Level.INFO, "Handling bundle " + symbolicName
				+ " in AAA Operations Handler");

		try {
			transactionManager.begin();

			String checksum = DigestUtils.md5Hex(yamlUrl.openStream());
			Yaml yaml = new Yaml(new CustomClassLoaderConstructor(getClass()
					.getClassLoader()));

			// Check if this file has been executed again in the past
			Application application = Application.findBySymbolicName(
					symbolicName, em);
			if (application == null) {
				// If the application was not found in the DB initialize the
				// entity
				// to use it lated on when registering the YAML file
				// execution.
				LOGGER.log(Level.FINE, "Processing AAA operations of bundle "
						+ symbolicName);
				application = new Application();
				application.setSymbolicName(symbolicName);
			} else if (application.getChecksum().equals(checksum)) {
				// If the application has been processed before and the YAML
				// file
				// has not been modified in the meantime no need to proceed
				// with the processing.
				LOGGER.log(
						Level.FINEST,
						"Ignoring operations found in bundle "
								+ symbolicName
								+ "; the operations are unchanged since the last time they were processed");
				transactionManager.commit();
				return;
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> contents = (Map<String, Object>) yaml
					.load(yamlUrl.openStream());
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> operations = (List<Map<String, Object>>) contents
					.get("operations");
			if (operations != null) {
				handleOperations(operations);
			}
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> templates = (List<Map<String, Object>>) contents
					.get("templates");
			if (templates != null) {
				for (Map<String, Object> template : templates) {
					String templateName = (String) template.get("name");
					String templateDescription = (String) template
							.get("description");
					OpTemplateDTO templateDTO = templateService.getTemplateByName(templateName);
					if (templateDTO == null) {
						templateDTO = new OpTemplateDTO();
						templateDTO.setName(templateName);
						templateDTO.setDescription(templateDescription);
						templateService.createTemplate(templateDTO);
					}
				}
			}

			// Register the processing of the file in the DB
			application.setChecksum(checksum);
			application.setExecutedOn(DateTime.now().getMillis());
			em.merge(application);

			transactionManager.commit();
		} catch (IOException | NotSupportedException | SystemException
				| IllegalStateException | SecurityException
				| HeuristicMixedException | HeuristicRollbackException
				| RollbackException ex) {
			LOGGER.log(Level.SEVERE, "Error handling AAA YAML file", ex);
			try {
				transactionManager.rollback();
			} catch (IllegalStateException | SecurityException
					| SystemException e) {
				LOGGER.log(Level.SEVERE, "Error rolling back AAA transaction",
						e);
			}
		}
	}

	private void handleOperations(List<Map<String, Object>> operations) {
		for (Map<String, Object> operation : operations) {
			String operationName = (String) operation.get("name");
			String operationDescription = (String) operation
					.get("description");
			String dynamicCode = (String) operation.get("dynamicCode");
			OperationDTO operationDTO = operationService
					.getOperationByName(operationName);
			if (operationDTO == null) {
				operationDTO = new OperationDTO();
				operationDTO.setName(operationName);
				operationDTO.setDescription(operationDescription);
				operationDTO.setDynamic(dynamicCode != null);
				operationDTO.setDynamicCode(dynamicCode);
				operationService.createOperation(operationDTO);
			} else if ((operation.get("forceUpdate") != null)
					&& ((Boolean) operation.get("forceUpdate"))) {
				operationDTO.setDescription(operationDescription);
				operationDTO.setDynamic(dynamicCode != null);
				operationDTO.setDynamicCode(dynamicCode);
				operationService.updateOperation(operationDTO);
			}
		}
	}

}
