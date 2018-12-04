package com.eurodyn.qlack2.be.workflow.api.dto;

import java.util.Date;

public class NodeInstanceDTO {

    private Long id;
    private String nodeId;
    private String name;
    private String deploymentId;
    private Long processInstanceId;
    private String nodeType;
    private String connection;
    private int type;

    private Date dataTimeStamp;

    private Long workItemId;

    public NodeInstanceDTO() {
      //empty no arg DTO Constructor
    }

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
   }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getDeploymentId() {
        return deploymentId;
    }
    
    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }
    
    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
   }

    public Date getDataTimeStamp() {
        return dataTimeStamp;
    }
    
    public void setDataTimeStamp(Date dataTimeStamp) {
        this.dataTimeStamp = dataTimeStamp;
    }

    public String getNodeType() {
        return nodeType;
    }
    
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeUniqueId) {
        this.nodeId = nodeUniqueId;
    }

    public boolean isCompleted() {
        return (this.type==1);
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String incomingConnection) {
        this.connection = incomingConnection;
    }

    public int getType() {
        return type;
    }
    
    public void setType(int type){
    	this.type = type;
    }

    public Long getWorkItemId() {
        return workItemId;
    }

    public void setWorkItemId(Long workItemId) {
        this.workItemId = workItemId;
   }

}