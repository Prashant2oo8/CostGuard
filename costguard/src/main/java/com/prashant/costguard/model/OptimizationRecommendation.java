package com.prashant.costguard.model;

public class OptimizationRecommendation {
    private String instanceId;
    private String recommendation;
    private double cpuUsage;

    public OptimizationRecommendation(String instanceId, String recommendation, double cpuUsage){
        this.instanceId = instanceId;
        this.cpuUsage = cpuUsage;
        this.recommendation = recommendation;
    }

    public String getInstanceId() {
        return instanceId;
    }
    public String getRecommendation(){
        return recommendation;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

}
