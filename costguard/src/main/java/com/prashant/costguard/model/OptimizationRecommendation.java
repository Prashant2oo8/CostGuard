package com.prashant.costguard.model;

public class OptimizationRecommendation {
    private String instanceId;
    private String recommendation;
    private double cpuUsage;
    private double estimatedMonthlySaving;

    public OptimizationRecommendation(String instanceId, String recommendation, double cpuUsage, double estimatedMonthlySaving){
        this.instanceId = instanceId;
        this.cpuUsage = cpuUsage;
        this.recommendation = recommendation;
        this.estimatedMonthlySaving = estimatedMonthlySaving;
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
    public double getEstimatedMonthlySaving(){
        return estimatedMonthlySaving;
    }

}
