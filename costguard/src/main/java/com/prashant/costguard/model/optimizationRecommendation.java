package com.prashant.costguard.model;

public class optimizationRecommendation {
    private String instanceId;
    private String recommendation;
    private double cpuUsage;

    public optimizationRecommendation(String instanceId, String recommendation, double cpuUsage){
        this.instanceId = instanceId;
        this.cpuUsage = cpuUsage;
        this.recommendation = recommendation;
    }

    public String getInstanceId() {
        return instanceId;
    }
    public String getRecommendation
}
