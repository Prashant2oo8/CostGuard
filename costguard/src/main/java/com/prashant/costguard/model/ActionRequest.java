package com.prashant.costguard.model;

public class ActionRequest {

    private String resourceType;
    private String resourceId;
    private String action;
    private String vpcResourceType;

    public ActionRequest() {
    }

    public ActionRequest(String resourceType, String resourceId, String action) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.action = action;
    }

    public ActionRequest(String resourceType, String resourceId, String action, String vpcResourceType) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.action = action;
        this.vpcResourceType = vpcResourceType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getVpcResourceType() {
        return vpcResourceType;
    }

    public void setVpcResourceType(String vpcResourceType) {
        this.vpcResourceType = vpcResourceType;
    }
}