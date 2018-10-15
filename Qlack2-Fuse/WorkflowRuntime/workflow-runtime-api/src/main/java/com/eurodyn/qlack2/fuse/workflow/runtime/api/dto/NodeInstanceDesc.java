package com.eurodyn.qlack2.fuse.workflow.runtime.api.dto;

import java.io.Serializable;
import java.util.Date;

public class NodeInstanceDesc implements Serializable{

    private static final long serialVersionUID = 7310019271033570922L;

    private long id;
    private String nodeId;
    private String name;
    private String deploymentId;
    private long processInstanceId;
    private String nodeType;
    private String connection;
    private int type;

    private Date dataTimeStamp;

    private Long workItemId;

    public NodeInstanceDesc() {
        // default constructor
    }


    public NodeInstanceDesc(String id, String nodeId, String name, String nodeType,
                            String deploymentId, long processInstanceId, Date date, 
                            String connection, int type, Long workItemId) {
        this.id = Long.parseLong(id);
        this.name = name;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.deploymentId = deploymentId;
        this.processInstanceId = processInstanceId;
        this.dataTimeStamp = date;
        this.connection = connection;
        this.type = type;
        this.workItemId = workItemId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public Date getDataTimeStamp() {
        return dataTimeStamp;
    }

    public String getNodeType() {
        return nodeType;
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

    @Override
    public String toString() {
        return "NodeInstanceDesc{" + "id=" + id + ", nodeId=" + nodeId + ", nodeUniqueId=" + nodeId + ", name=" + name + ", deploymentId=" + deploymentId + ", processInstanceId="
                + processInstanceId + ", type=" + nodeType + ", completed=" + isCompleted() + ", dataTimeStamp=" + dataTimeStamp + '}';
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

    public Long getWorkItemId() {
        return workItemId;
    }

    public void setWorkItemId(Long workItemId) {
        this.workItemId = workItemId;
   }

}