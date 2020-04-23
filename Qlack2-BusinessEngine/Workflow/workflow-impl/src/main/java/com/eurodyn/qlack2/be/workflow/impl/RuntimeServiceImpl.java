package com.eurodyn.qlack2.be.workflow.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.eurodyn.qlack2.be.rules.api.RulesRuntimeManagementService;
import com.eurodyn.qlack2.be.rules.api.request.runtime.StatelessMultiExecuteRequest;
import com.eurodyn.qlack2.be.rules.api.request.runtime.WorkingSetRuleVersionPair;
import com.eurodyn.qlack2.be.workflow.api.RuntimeService;
import com.eurodyn.qlack2.be.workflow.api.dto.NodeInstanceDTO;
import com.eurodyn.qlack2.be.workflow.api.dto.TaskSummaryDTO;
import com.eurodyn.qlack2.be.workflow.api.dto.WorkflowInstanceDTO;
import com.eurodyn.qlack2.be.workflow.api.dto.WorkflowRuntimeErrorLogDTO;
import com.eurodyn.qlack2.be.workflow.api.exception.QInvalidActionException;
import com.eurodyn.qlack2.be.workflow.api.exception.QInvalidPreconditionsException;
import com.eurodyn.qlack2.be.workflow.api.request.runtime.DeleteWorkflowInstancesRequest;
import com.eurodyn.qlack2.be.workflow.api.request.runtime.GetWorkflowInstanceDetailsRequest;
import com.eurodyn.qlack2.be.workflow.api.request.runtime.GetWorkflowInstancesRequest;
import com.eurodyn.qlack2.be.workflow.api.request.runtime.GetWorkflowRuntimeErrorLogRequest;
import com.eurodyn.qlack2.be.workflow.api.request.runtime.WorkflowInstanceActionRequest;
import com.eurodyn.qlack2.be.workflow.api.request.runtime.WorkflowInstanceRequest;
import com.eurodyn.qlack2.be.workflow.api.request.runtime.WorkflowTaskActionRequest;
import com.eurodyn.qlack2.be.workflow.api.request.runtime.WorkflowVariableInstanceRequest;
import com.eurodyn.qlack2.be.workflow.client.api.rules.PreconditionFact;
import com.eurodyn.qlack2.be.workflow.impl.dto.AuditWorkflowInstanceDTO;
import com.eurodyn.qlack2.be.workflow.impl.model.Condition;
import com.eurodyn.qlack2.be.workflow.impl.model.ConditionType;
import com.eurodyn.qlack2.be.workflow.impl.model.Workflow;
import com.eurodyn.qlack2.be.workflow.impl.model.WorkflowVersion;
import com.eurodyn.qlack2.be.workflow.impl.util.AuditConstants;
import com.eurodyn.qlack2.be.workflow.impl.util.AuditConstants.EVENT;
import com.eurodyn.qlack2.be.workflow.impl.util.AuditConstants.GROUP;
import com.eurodyn.qlack2.be.workflow.impl.util.AuditConstants.LEVEL;
import com.eurodyn.qlack2.be.workflow.impl.util.ConverterUtil;
import com.eurodyn.qlack2.be.workflow.impl.util.RuntimeUtil;
import com.eurodyn.qlack2.be.workflow.impl.util.SecurityUtil;
import com.eurodyn.qlack2.be.workflow.impl.util.WorkflowConstants;
import com.eurodyn.qlack2.fuse.auditing.api.AuditLoggingService;
import com.eurodyn.qlack2.fuse.auditing.api.dto.AuditLogDTO;
import com.eurodyn.qlack2.fuse.auditing.api.dto.SearchDTO;
import com.eurodyn.qlack2.fuse.auditing.api.dto.SortDTO;
import com.eurodyn.qlack2.fuse.auditing.api.enums.AuditLogColumns;
import com.eurodyn.qlack2.fuse.auditing.api.enums.SearchOperator;
import com.eurodyn.qlack2.fuse.auditing.api.enums.SortOperator;
import com.eurodyn.qlack2.fuse.idm.api.IDMService;
import com.eurodyn.qlack2.fuse.idm.api.annotations.ValidateTicket;
import com.eurodyn.qlack2.fuse.idm.api.signing.SignedTicket;
import com.eurodyn.qlack2.fuse.workflow.runtime.api.WorkflowRuntimeService;
import com.eurodyn.qlack2.fuse.workflow.runtime.api.dto.ProcessInstanceDesc;
import com.eurodyn.qlack2.util.auditclient.api.AuditClientService;

public class RuntimeServiceImpl implements RuntimeService{
	
	private static final Logger LOGGER = Logger.getLogger(RuntimeServiceImpl.class.getName());
	private EntityManager em;
	private IDMService idmService;
	private SecurityUtil securityUtil;
	private WorkflowRuntimeService workflowRuntimeService;
	private ConverterUtil converterUtil;
	private RuntimeUtil runtimeUtil;
	private AuditClientService auditClientService;
	private AuditLoggingService auditLoggingService;
	
	private List<RulesRuntimeManagementService> rulesRuntimeManagementServiceList;

	public void setEm(EntityManager em) {
		this.em = em;
	}
	
	public void setIdmService(IDMService idmService) {
		this.idmService = idmService;
	}
	
	public void setSecurityUtil(SecurityUtil securityUtil) {
		this.securityUtil = securityUtil;
	}
	
	public void setWorkflowRuntimeService(WorkflowRuntimeService workflowRuntimeService) {
		this.workflowRuntimeService = workflowRuntimeService;
	}
	
	public void setRuntimeUtil(RuntimeUtil runtimeUtil) {
		this.runtimeUtil = runtimeUtil;
	}
	
	public void setConverterUtil(ConverterUtil converterUtil) {
		this.converterUtil = converterUtil;
	}
	
	public void setRulesRuntimeManagementServiceList(
			List<RulesRuntimeManagementService> rulesRuntimeManagementServiceList) {
		this.rulesRuntimeManagementServiceList = rulesRuntimeManagementServiceList;
	}
	
	public void setAuditClientService(AuditClientService auditClientService) {
		this.auditClientService = auditClientService;
	}
	
	public void setAuditLoggingService(AuditLoggingService auditLoggingService) {
		this.auditLoggingService = auditLoggingService;
	}
			
	@ValidateTicket
	@Override
	public Long startWorkflowInstance(WorkflowInstanceRequest request) {
		LOGGER.log(Level.FINE, "Starting workflow instance for version with ID {0}", request.getId());
		
		WorkflowVersion version = WorkflowVersion.find(em, request.getId());

		securityUtil.checkWorkflowOperation(request.getSignedTicket(),
				WorkflowConstants.OP_WFL_EXECUTE_RUNTIME, version.getWorkflow().getId(), version.getWorkflow().getProjectId());
		
		if (version.getConditions()!=null && 
				version.getConditions().size() > 0)
		{
			// Execute precondition rules to decide if the workflow should be executed or not
			boolean validPreconditions = executePreconditionRules(version, request.getFacts(), request.getSignedTicket());
			
			if (!validPreconditions)
				throw new QInvalidPreconditionsException("Preconditions are not met. Workflow cannot be executed.");
		}
		
		Long processInstanceId = null;
	
		if (version.getProcessId() != null && version.getContent().length() > 0)
			processInstanceId = workflowRuntimeService.startWorkflowInstance(version.getProcessId(), version.getContent(), request.getParameters());
		else
			throw new QInvalidActionException("Either the BPMN content is null or it does not contain the process id."); 
		
		AuditWorkflowInstanceDTO auditWorkflowInstanceDTO = converterUtil
				.workflowVersionToAuditWorkflowInstanceDTO(version, processInstanceId);
		auditClientService.audit(LEVEL.QBE_WORKFLOW.toString(), EVENT.START.toString(), 
				GROUP.WORKFLOW_VERSION_INSTANCE.toString(),
				null, request.getSignedTicket().getUserID(),
				auditWorkflowInstanceDTO);
		
		return processInstanceId;
	}
	
	@ValidateTicket
	@Override
	public Long createWorkflowInstance(WorkflowInstanceRequest request) {
		LOGGER.log(Level.FINE, "Creating workflow instance for version with ID {0}", request.getId());
		
		WorkflowVersion version = WorkflowVersion.find(em, request.getId());

		securityUtil.checkWorkflowOperation(request.getSignedTicket(),
				WorkflowConstants.OP_WFL_EXECUTE_RUNTIME, version.getWorkflow().getId(), version.getWorkflow().getProjectId());
		
		if (version.getConditions()!=null && 
				version.getConditions().size() > 0)
		{
			// Execute precondition rules to decide if the workflow should be executed or not
			boolean validPreconditions = executePreconditionRules(version, request.getFacts(), request.getSignedTicket());
			
			if (!validPreconditions)
				throw new QInvalidPreconditionsException("Preconditions are not met. Workflow cannot be executed.");
		}
		
		Long processInstanceId = null;
	
		if (version.getProcessId() != null && version.getContent().length() > 0)
			processInstanceId = workflowRuntimeService.createWorkflowInstance(version.getProcessId(), version.getContent(), request.getParameters());
		else
			throw new QInvalidActionException("Either the BPMN content is null or it does not contain the process id."); 
		
		AuditWorkflowInstanceDTO auditWorkflowInstanceDTO = converterUtil
				.workflowVersionToAuditWorkflowInstanceDTO(version, processInstanceId);
		auditClientService.audit(LEVEL.QBE_WORKFLOW.toString(), EVENT.CREATE.toString(), 
				GROUP.WORKFLOW_VERSION_INSTANCE.toString(),
				null, request.getSignedTicket().getUserID(),
				auditWorkflowInstanceDTO);
		
		return processInstanceId;
	}
	
	@ValidateTicket
	@Override
	public Long startWorkflowInstance(WorkflowInstanceActionRequest request) {
		LOGGER.log(Level.FINE, "Starting workflow instance ID {0}", request.getProcessInstanceId());
		
		WorkflowVersion version = WorkflowVersion.find(em, request.getId());
		
		securityUtil.checkWorkflowOperation(request.getSignedTicket(),
				WorkflowConstants.OP_WFL_EXECUTE_RUNTIME, version.getWorkflow().getId(), version.getWorkflow().getProjectId());
	
		Long processInstanceId = workflowRuntimeService.startWorkflowInstance(request.getProcessInstanceId());

		AuditWorkflowInstanceDTO auditWorkflowInstanceDTO = converterUtil
				.workflowVersionToAuditWorkflowInstanceDTO(version, processInstanceId);
		auditClientService.audit(LEVEL.QBE_WORKFLOW.toString(), EVENT.START.toString(), 
				GROUP.WORKFLOW_VERSION_INSTANCE.toString(),
				null, request.getSignedTicket().getUserID(),
				auditWorkflowInstanceDTO);
		
		return processInstanceId;
	}
	
	@ValidateTicket
	@Override
	public void stopWorkflowInstance(WorkflowInstanceActionRequest request) {
		LOGGER.log(Level.FINE, "Stopping workflow instance ID {0}", request.getProcessInstanceId());
		WorkflowVersion version = WorkflowVersion.find(em, request.getId());

		securityUtil.checkWorkflowOperation(request.getSignedTicket(),
				WorkflowConstants.OP_WFL_EXECUTE_RUNTIME, version.getWorkflow().getId(), version.getWorkflow().getProjectId());
		
		workflowRuntimeService.stopWorkflowInstance(request.getProcessInstanceId());
		
		AuditWorkflowInstanceDTO auditWorkflowInstanceDTO = converterUtil
				.workflowVersionToAuditWorkflowInstanceDTO(version, request.getProcessInstanceId());
		auditClientService.audit(LEVEL.QBE_WORKFLOW.toString(), EVENT.STOP.toString(), 
				GROUP.WORKFLOW_VERSION_INSTANCE.toString(),
				null, request.getSignedTicket().getUserID(),
				auditWorkflowInstanceDTO);
	}
	
	@ValidateTicket
	@Override
	public void deleteWorkflowInstance(WorkflowInstanceActionRequest request) {
		LOGGER.log(Level.FINE, "Deleting workflow instance ID {0}", request.getProcessInstanceId());
		Workflow workflow = Workflow.findByWorkflowVersionId(em, request.getId());

		securityUtil.checkWorkflowOperation(request.getSignedTicket(),
				WorkflowConstants.OP_WFL_EXECUTE_RUNTIME, workflow.getId(), workflow.getProjectId());
		
		workflowRuntimeService.deleteWorkflowInstance(request.getProcessInstanceId());
		
		auditClientService.audit(LEVEL.QBE_WORKFLOW.toString(), EVENT.DELETE.toString(), 
				GROUP.WORKFLOW_VERSION_INSTANCE.toString(),
				null, request.getSignedTicket().getUserID(),
				request.getProcessInstanceId());
	}
	
	@ValidateTicket
	@Override
	public void suspendWorkflowInstance(WorkflowInstanceActionRequest request) {
		LOGGER.log(Level.FINE, "Suspending workflow instance ID {0}", request.getProcessInstanceId());
		WorkflowVersion version = WorkflowVersion.find(em, request.getId());

		securityUtil.checkWorkflowOperation(request.getSignedTicket(),
				WorkflowConstants.OP_WFL_EXECUTE_RUNTIME, version.getWorkflow().getId(), version.getWorkflow().getProjectId());
		
		workflowRuntimeService.suspendWorkflowInstance(request.getProcessInstanceId());
		
		AuditWorkflowInstanceDTO auditWorkflowInstanceDTO = converterUtil
				.workflowVersionToAuditWorkflowInstanceDTO(version, request.getProcessInstanceId());
		auditClientService.audit(LEVEL.QBE_WORKFLOW.toString(), EVENT.PAUSE.toString(), 
				GROUP.WORKFLOW_VERSION_INSTANCE.toString(),
				null, request.getSignedTicket().getUserID(),
				auditWorkflowInstanceDTO);
	}
	
	@ValidateTicket
	@Override
	public void resumeWorkflowInstance(WorkflowInstanceActionRequest request) {
		LOGGER.log(Level.FINE, "Resuming workflow instance ID {0}", request.getProcessInstanceId());
		WorkflowVersion version = WorkflowVersion.find(em, request.getId());

		securityUtil.checkWorkflowOperation(request.getSignedTicket(),
				WorkflowConstants.OP_WFL_EXECUTE_RUNTIME, version.getWorkflow().getId(), version.getWorkflow().getProjectId());
		
		workflowRuntimeService.resumeWorkflowInstance(request.getProcessInstanceId());
		
		AuditWorkflowInstanceDTO auditWorkflowInstanceDTO = converterUtil
				.workflowVersionToAuditWorkflowInstanceDTO(version, request.getProcessInstanceId());
		auditClientService.audit(LEVEL.QBE_WORKFLOW.toString(), EVENT.RESUME.toString(), 
				GROUP.WORKFLOW_VERSION_INSTANCE.toString(),
				null, request.getSignedTicket().getUserID(),
				auditWorkflowInstanceDTO);
	}
	
	@ValidateTicket
	@Override
	public Object getVariableInstance(WorkflowVariableInstanceRequest request) {
		LOGGER.log(Level.FINE, "Getting variable instance for ID {0}", request.getProcessInstanceId());
		WorkflowVersion version = WorkflowVersion.find(em, request.getId());

		securityUtil.checkWorkflowOperation(request.getSignedTicket(),
				WorkflowConstants.OP_WFL_EXECUTE_RUNTIME, version.getWorkflow().getId(), version.getWorkflow().getProjectId());
		
		Object instanceVariable = workflowRuntimeService.getVariableInstance(request.getProcessInstanceId(), request.getVariableName());
		
		AuditWorkflowInstanceDTO auditWorkflowInstanceDTO = converterUtil
				.workflowVersionToAuditWorkflowInstanceDTO(version, request.getProcessInstanceId());
		auditClientService.audit(LEVEL.QBE_WORKFLOW.toString(), EVENT.VIEW.toString(), 
				GROUP.WORKFLOW_VERSION_INSTANCE.toString(),
				null, request.getSignedTicket().getUserID(),
				auditWorkflowInstanceDTO);
		
		return instanceVariable;
	}
	
	@ValidateTicket
	@Override
	public void setVariableInstance(WorkflowVariableInstanceRequest request) {
		LOGGER.log(Level.FINE, "setting variable instance for ID {0}", request.getProcessInstanceId());
		WorkflowVersion version = WorkflowVersion.find(em, request.getId());

		securityUtil.checkWorkflowOperation(request.getSignedTicket(),
				WorkflowConstants.OP_WFL_EXECUTE_RUNTIME, version.getWorkflow().getId(), version.getWorkflow().getProjectId());
		
		workflowRuntimeService.setVariableInstance(request.getProcessInstanceId(), request.getVariableName(), request.getVariableData());
		
		AuditWorkflowInstanceDTO auditWorkflowInstanceDTO = converterUtil
				.workflowVersionToAuditWorkflowInstanceDTO(version, request.getProcessInstanceId());
		auditClientService.audit(LEVEL.QBE_WORKFLOW.toString(), EVENT.VIEW.toString(), 
				GROUP.WORKFLOW_VERSION_INSTANCE.toString(),
				null, request.getSignedTicket().getUserID(),
				auditWorkflowInstanceDTO);
	}
	
	@ValidateTicket
	@Override
	public List<WorkflowInstanceDTO> getWorkflowInstances(GetWorkflowInstancesRequest req) {
		LOGGER.log(Level.FINE, "Getting workflow instances for project ID {0}", req.getProjectId());
		return getWorkflowInstancesListByProjectId(req.getProjectId());
	}
	
	@ValidateTicket
	@Override
	public WorkflowInstanceDTO getWorkflowInstanceDetails(GetWorkflowInstanceDetailsRequest req)
	{
		LOGGER.log(Level.FINE, "Getting workflow instance details for instance ID {0}", req.getInstanceId());
		ProcessInstanceDesc processInstance = workflowRuntimeService.getProcessInstanceDetails(req.getInstanceId());
		WorkflowVersion version = WorkflowVersion.findByProcessId(em,  processInstance.getProcessId());
		WorkflowInstanceDTO workflowInstanceDTO = converterUtil
				.processInstanceDescToWorkflowInstanceDTO(processInstance, version.getWorkflow(), version);
		return workflowInstanceDTO;
	}
	
	@ValidateTicket
	@Override
	public void deleteWorkflowInstancesForWorkflow(DeleteWorkflowInstancesRequest req)
	{
		LOGGER.log(Level.FINE, "Getting workflow instances for workflow ID {0}", req.getWorkflowId());
		Workflow workflow = Workflow.find(em, req.getWorkflowId()); 

		securityUtil.checkWorkflowOperation(req.getSignedTicket(),
			WorkflowConstants.OP_WFL_EXECUTE_RUNTIME, workflow.getId(), workflow.getProjectId());
			
		runtimeUtil.deleteWorkflowInstancesForWorkflow(workflow, req.getSignedTicket().getUserID());
	}
	
	@ValidateTicket
	@Override
	public void signalWorkflowInstance(WorkflowInstanceActionRequest req)
	{
		LOGGER.log(Level.FINE, "Signaling workflow instance for instance ID {0}", req.getProcessInstanceId());
		Workflow workflow = Workflow.findByWorkflowVersionId(em, req.getId());

		securityUtil.checkWorkflowOperation(req.getSignedTicket(),
				WorkflowConstants.OP_WFL_EXECUTE_RUNTIME, workflow.getId(), workflow.getProjectId());
			
		workflowRuntimeService.signalWorkflowInstance(req.getProcessInstanceId(), req.getSignalName(), req.getSignalEvent());
	}
	
	@ValidateTicket
	@Override
	public TaskSummaryDTO getTaskDetails(WorkflowTaskActionRequest request)
	{
		LOGGER.log(Level.FINE, "Getting task details for task ID {0}", request.getTaskId());
		Workflow workflow = Workflow.findByWorkflowVersionId(em, request.getId());

		securityUtil.checkWorkflowOperation(request.getSignedTicket(),
				WorkflowConstants.OP_WFL_VIEW_RUNTIME, workflow.getId(), workflow.getProjectId());
		
		return converterUtil.
				taskSummaryToTaskSummaryDTO(workflowRuntimeService.
						getTaskDetails(request.getProcessInstanceId(), request.getTaskId()));
	}
	
	@ValidateTicket
	@Override
	public List<TaskSummaryDTO> getTasksAssignedAsPotentialOwner(WorkflowTaskActionRequest request)
	{
		LOGGER.log(Level.FINE, "Getting Tasks Assigned As Potential Owner for User ID {0}", request.getUserId());
		if (request.getId() != null)
		{		
			Workflow workflow = Workflow.findByWorkflowVersionId(em, request.getId());

			securityUtil.checkViewRuntimeWorkflowOperation(request.getSignedTicket(), workflow.getId());
		
			return converterUtil.
					taskSummaryToTaskSummaryDTOList(workflowRuntimeService.
							getTasksAssignedAsPotentialOwner(request.getProcessInstanceId(), request.getUserId(), request.getStatusList()));
		}
		else if (request.getProjectId() != null)
		{
			securityUtil.checkViewRuntimeWorkflowOperation(request.getSignedTicket(), request.getProjectId());
		
			return converterUtil.
					taskSummaryToTaskSummaryDTOList(workflowRuntimeService.
							getAllTasksAssignedAsPotentialOwner(request.getUserId(), request.getStatusList()));
		}
		return null;
	}
	
	@ValidateTicket
	@Override
	public List<TaskSummaryDTO> getTasksByStatusByProcessInstanceId(WorkflowTaskActionRequest request)
	{
		LOGGER.log(Level.FINE, "Getting Tasks for Process Instance ID {0}", request.getProcessInstanceId());
		if (request.getId() != null)
		{		
			Workflow workflow = Workflow.findByWorkflowVersionId(em, request.getId());

			securityUtil.checkViewRuntimeWorkflowOperation(request.getSignedTicket(), workflow.getId());
		
			return converterUtil.
					taskSummaryToTaskSummaryDTOList(workflowRuntimeService.
							getTasksByStatusByProcessInstanceId(request.getProcessInstanceId(), request.getStatusList()));
		}
		return null;
	}
	
	@ValidateTicket
	@Override
	public void acceptTask(WorkflowTaskActionRequest request)
	{
		LOGGER.log(Level.FINE, "Accepting task ID {0}", request.getTaskId());
		Workflow workflow = Workflow.findByWorkflowVersionId(em, request.getId());

		securityUtil.checkWorkflowOperation(request.getSignedTicket(),
				WorkflowConstants.OP_WFL_EXECUTE_RUNTIME, workflow.getId(), workflow.getProjectId());
		
		workflowRuntimeService.acceptTask(request.getProcessInstanceId(), request.getTaskId(), request.getUserId());
	}
	
	@ValidateTicket
	@Override
	public void startTask(WorkflowTaskActionRequest request)
	{
		LOGGER.log(Level.FINE, "Starting task ID {0}", request.getTaskId());
		Workflow workflow = Workflow.findByWorkflowVersionId(em, request.getId());

		securityUtil.checkWorkflowOperation(request.getSignedTicket(),
				WorkflowConstants.OP_WFL_EXECUTE_RUNTIME, workflow.getId(), workflow.getProjectId());
		
		workflowRuntimeService.startTask(request.getProcessInstanceId(), request.getTaskId(), request.getUserId());
	}
	
	@ValidateTicket
	@Override
	public void completeTask(WorkflowTaskActionRequest request)
	{
		LOGGER.log(Level.FINE, "Completing task ID {0}", request.getTaskId());
		Workflow workflow = Workflow.findByWorkflowVersionId(em, request.getId());

		securityUtil.checkWorkflowOperation(request.getSignedTicket(),
				WorkflowConstants.OP_WFL_EXECUTE_RUNTIME, workflow.getId(), workflow.getProjectId());
		
		workflowRuntimeService.completeTask(request.getProcessInstanceId(), request.getTaskId(), request.getUserId(), request.getData());
	}
	
	@ValidateTicket
	@Override
	public List<Long> getTasksByProcessInstanceId(WorkflowInstanceActionRequest request)
	{
		LOGGER.log(Level.FINE, "Getting tasks for process instance task ID {0}", request.getProcessInstanceId());
		Workflow workflow = Workflow.findByWorkflowVersionId(em, request.getId());

		securityUtil.checkWorkflowOperation(request.getSignedTicket(),
				WorkflowConstants.OP_WFL_VIEW_RUNTIME, workflow.getId(), workflow.getProjectId());
		
		return workflowRuntimeService.getTasksByProcessInstanceId(request.getProcessInstanceId());
	}
	
	@ValidateTicket
	@Override
	public List<WorkflowRuntimeErrorLogDTO> getWorkflowErrorAuditLogs(GetWorkflowRuntimeErrorLogRequest req)
	{
		LOGGER.log(Level.FINE, "Getting workflow error logs for project ID {0}", req.getProjectId());
		
		List<WorkflowRuntimeErrorLogDTO> workflowLogDTOs = new ArrayList<>();
		
		List<Workflow> myWorkflows = Workflow.findByProjectId(em, req.getProjectId()); 
		
		for (Workflow workflow : myWorkflows)
		{
			securityUtil.checkWorkflowOperation(req.getSignedTicket(),
					WorkflowConstants.OP_WFL_VIEW_RUNTIME, workflow.getId(), workflow.getProjectId());
			
			for (WorkflowVersion version : workflow.getWorkflowVersions())
			{
				LOGGER.log(Level.FINE, "WorkflowName: " + workflow.getName() + ", VersionName: " + version.getName() + ", ProcessId: " + version.getProcessId());
				if (version.getProcessId() != null)
				{
					List<SearchDTO> searchList = new ArrayList();
					List<SortDTO> sortList = new ArrayList();
					
					SortDTO sort1 = new SortDTO();
					sort1.setColumn(AuditLogColumns.createdOn);
					sort1.setOperator(SortOperator.DESC);
					sortList.add(sort1);
					
					SearchDTO search1 = new SearchDTO();
					search1.setColumn(AuditLogColumns.groupName);
					search1.setOperator(SearchOperator.EQUAL);
					List <String> values1 = new ArrayList();
					values1.add(AuditConstants.RUNTIME_GROUP.RUNTIME_WORKFLOW.toString());
					search1.setValue(values1);
					searchList.add(search1);
					
					SearchDTO search2 = new SearchDTO();
					search2.setColumn(AuditLogColumns.shortDescription);
					search2.setOperator(SearchOperator.EQUAL);
					List <String> values2 = new ArrayList();
					values2.add(version.getProcessId());
					search2.setValue(values2);
					searchList.add(search2);
					
					List<AuditLogDTO> auditLogs = auditLoggingService.listAuditLogs(searchList, null, null, sortList, null);
					for (AuditLogDTO auditLog: auditLogs)
						workflowLogDTOs.add(converterUtil
								.auditLogDTOToWorkflowRuntimeErrorLogDTO(auditLog, workflow, version));								
				}
			}
		}	
		return workflowLogDTOs;
	}
	
	@ValidateTicket
	@Override
	public List<NodeInstanceDTO> getProcessInstanceFullHistory(GetWorkflowInstanceDetailsRequest req)
	{
		LOGGER.log(Level.FINE, "Getting workflow nodes for instance ID {0}", req.getInstanceId());
		return converterUtil.
				nodeInstanceToNodeInstanceDTOList(workflowRuntimeService.getProcessInstanceFullHistory(req.getInstanceId()));
	}
	
	@ValidateTicket
	@Override
	public List<NodeInstanceDTO> getProcessInstanceActiveNodes(GetWorkflowInstanceDetailsRequest req)
	{
		LOGGER.log(Level.FINE, "Getting active workflow nodes for instance ID {0}", req.getInstanceId());
		return converterUtil.
				nodeInstanceToNodeInstanceDTOList(workflowRuntimeService.getProcessInstanceActiveNodes(req.getInstanceId()));
	}
	
	@ValidateTicket
	@Override
	public String getTaskVariableValueByTaskId(WorkflowTaskActionRequest req)
	{
		LOGGER.log(Level.FINE, "Getting task variable for task ID {0}", req.getTaskId());
		return workflowRuntimeService.getTaskVariableValueByTaskId(req.getTaskId(), req.getVariableName());
	}
	
	private boolean executePreconditionRules(WorkflowVersion version, List<byte[]> facts, SignedTicket signedTicket) {
		boolean retVal = false;

		if (rulesRuntimeManagementServiceList.size() == 0) {
			throw new QInvalidActionException(
					"RulesRuntimeManagementService not available");
		}

		List<Condition> conditions = Condition.getConditionsWithoutParent(em, version.getId(), ConditionType.PRECONDITION);
		List<Condition> preconditions = new ArrayList<>();
		sortConditions(conditions, preconditions);

		List<WorkingSetRuleVersionPair> pairs = new ArrayList<>();
		if (preconditions != null) {
			for (Condition condition : preconditions) {
				WorkingSetRuleVersionPair pair = new WorkingSetRuleVersionPair();
				pair.setWorkingSetVersionId(condition.getWorkingSetId());
				pair.setRuleVersionId(condition.getRuleId());
				pairs.add(pair);
			}

			StatelessMultiExecuteRequest statelessMultiExecuteRequest = new StatelessMultiExecuteRequest();
			statelessMultiExecuteRequest.setPairs(pairs);
			statelessMultiExecuteRequest.setFacts(facts);
			statelessMultiExecuteRequest.setSignedTicket(signedTicket);

			List<byte[]> results = rulesRuntimeManagementServiceList.get(0)
					.statelessMultiExecute(statelessMultiExecuteRequest).getFacts();

			if (results != null && !results.isEmpty()) {
				byte[] preconditionFactBytes = results.get(0);
				Object object = deserializeObject(preconditionFactBytes);

				PreconditionFact preconditionFact = (PreconditionFact) object;

				retVal = preconditionFact.isValid();
			} else {
				throw new QInvalidActionException(
						"Output facts cannot be null or empty when executing preconditions");
			}

		}
		return retVal;
	}
	
	private Object deserializeObject(byte[] bytes) {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
				ObjectInputStream ois = new ObjectInputStream(bais)) {
			
			return ois.readObject();

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error while deserializing output fact", e);
			throw new QInvalidActionException(e);
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Error while deserializing output fact", e);
			throw new QInvalidActionException(e);
		}
	}
	
	private void sortConditions(List<Condition> conditions,
			List<Condition> sortedConditions) {
		if (conditions != null) {
			for (Condition condition : conditions) {
				sortedConditions.add(condition);

				if (!condition.getChildren().isEmpty()) {
					sortConditions(condition.getChildren(), sortedConditions);
				}
			}
		}
	}

	private List<WorkflowInstanceDTO> getWorkflowInstancesListByProjectId(String projectId) {
		List<WorkflowInstanceDTO> workflowInstanceDtos = new ArrayList<>();

		Query query = em
				.createNativeQuery("SELECT PROC.ID as ID, PROC.DURATION AS DURATION, PROC.START_DATE AS STARTDATE, "
						+ "PROC.END_DATE as ENDDATE,  PROC.PROCESSID AS PROCESSID, PROC.PROCESSINSTANCEID AS PROCESSINSTANCEID,  PROC.PROCESSNAME as PROCESSNAME, W.ID AS WID, WV.ID AS WVID, "
						+ "W.NAME AS WNAME, WV.NAME AS WVNAME, PROC.STATUS as STATUS, "
						+ "CASE PROC.STATUS WHEN 0 THEN 'Pending'" + "WHEN 1 THEN 'Active' "
						+ "WHEN 2 THEN 'Completed' " + "WHEN 3 THEN 'Aborted'" + "WHEN 4 THEN 'Suspended' "
						+ "ELSE 'Invalid state' " + "END STATUSTEXT "
						+ "FROM jbpm_ProcessInstanceLog PROC JOIN wfl_workflow_version WV ON PROC.PROCESSID=WV.PROCESSID JOIN wfl_workflow W ON W.ID = WV.WORKFLOW "
						+ "where W.PROJECT_ID = :projectId AND PROC.STATUS != 2");
		query.setParameter("projectId", projectId);

		List results = query.getResultList();
		for (Object result : results) {
			WorkflowInstanceDTO dto = new WorkflowInstanceDTO();
			Object[] items = (Object[]) result;
			dto.setId(((BigDecimal) items[0]).longValue());
			if (items[1] != null) {
				dto.setDuration(((BigDecimal) items[1]).longValue());
			}
			if (items[2] != null) {
				dto.setStartDate(((Timestamp) items[2]).getTime());
			}
			if (items[3] != null) {
				dto.setEndDate(((Timestamp) items[3]).getTime());
			}
			if (items[4] != null) {
				dto.setProcessId(items[4].toString());
			}
			dto.setProcessInstanceId(((BigDecimal) items[5]).longValue());
			if (items[6] != null) {
				dto.setProcessName(items[6].toString());
			}
			dto.setWorkflowId(items[7].toString());
			dto.setVersionId(items[8].toString());
			dto.setWorkflowName(items[9].toString());
			dto.setVersionName(items[10].toString());
			if (items[11] != null) {
				dto.setStatus(((BigDecimal) items[11]).intValue());
			}
			if (items[12] != null) {
				dto.setStatusDesc(items[12].toString());
			}

			workflowInstanceDtos.add(dto);

		}

		return workflowInstanceDtos;
	}


}
