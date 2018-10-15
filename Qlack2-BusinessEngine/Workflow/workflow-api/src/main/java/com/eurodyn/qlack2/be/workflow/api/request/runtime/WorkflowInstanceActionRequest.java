package com.eurodyn.qlack2.be.workflow.api.request.runtime;

import com.eurodyn.qlack2.fuse.idm.api.signing.QSignedRequest;

public class WorkflowInstanceActionRequest extends QSignedRequest {

	private String id;
	private Long processInstanceId;
	private String signalName;
	private Object signalEvent;

	public WorkflowInstanceActionRequest() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public Long getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	
	public String getSignalName() {
		return signalName;
	}

	public void setSignalName(String signalName) {
		this.signalName = signalName;
	}
	
	public Object getSignalEvent() {
		return signalEvent;
	}

	public void setSignalEvent(String signalEvent) {
		this.signalEvent = signalEvent;
	}

}
