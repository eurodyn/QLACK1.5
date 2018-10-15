package com.eurodyn.qlack2.fuse.workflow.runtime.api;

import java.util.List;
import java.util.Map;

import com.eurodyn.qlack2.fuse.workflow.runtime.api.dto.ProcessInstanceDesc;
import com.eurodyn.qlack2.fuse.workflow.runtime.api.dto.NodeInstanceDesc;
import com.eurodyn.qlack2.fuse.workflow.runtime.api.dto.TaskSummary;

public interface WorkflowRuntimeService {
	
	Long startWorkflowInstance(String processId, String content, Map<String, Object> parameters);
	
	//List<ProcessInstanceLog> getProcessInstancesByProcessId(String processId);
	
	List<ProcessInstanceDesc> getProcessInstancesByProcessId(String processId);
	
	List<ProcessInstanceDesc> getProcessInstancesByProcessIdList(List<String> processIds);
	
	void stopWorkflowInstance(Long processInstanceId);
	
	void suspendWorkflowInstance(Long processInstanceId);
	
	void resumeWorkflowInstance(Long processInstanceId);
	
	void deleteWorkflowInstance(Long processInstanceId);
	
	void signalWorkflowInstance(Long processInstanceId, String signalName, Object event);
	
	TaskSummary getTaskDetails(Long processInstanceId, Long taskId);
	
	List<TaskSummary> getTasksAssignedAsPotentialOwner(Long processInstanceId, String userId, List<String> statusList);
	
	void acceptTask(Long processInstanceId, Long taskId, String userId);
	
	void startTask(Long processInstanceId, Long taskId, String userId);
	
	void completeTask(Long processInstanceId, Long taskId, String userId, Map<String, Object> data);
	
	List<Long> getTasksByProcessInstanceId(Long processInstanceId);
	
	Object getVariableInstance(Long processInstanceId, String variableName);

	List<TaskSummary> getAllTasksAssignedAsPotentialOwner(String userId, List<String> statusList);

	ProcessInstanceDesc getProcessInstanceDetails(Long processInstanceId);

	void setVariableInstance(Long processInstanceId, String variableName, Object data);

	List<NodeInstanceDesc> getProcessInstanceFullHistory(Long processInstanceId);

	Long createWorkflowInstance(String processId, String content, Map<String, Object> parameters);

	Long startWorkflowInstance(Long processInstanceId);

	List<NodeInstanceDesc> getProcessInstanceActiveNodes(Long processInstanceId);

	String getTaskVariableValueByTaskId(Long taskId, String variableName);

	List<TaskSummary> getTasksByStatusByProcessInstanceId(Long processInstanceId, List<String> statusList);
}
