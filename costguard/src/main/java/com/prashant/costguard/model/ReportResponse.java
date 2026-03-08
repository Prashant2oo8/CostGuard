package com.prashant.costguard.model;

public class ReportResponse {
    private String instanceId;
    private String state;
    private String instanceType;
    private double cpuUsage;
    private double monthlyCost;
    private String recommendation;
    private double estimateMonthlySaving;

    public ReportResponse(String instanceId, String instanceType, String state, double cpuUsage, double monthlyCost, String recommendation, double estimateMonthlySaving){

        this.instanceId = instanceId;
        this.state = state;
        this.instanceType = instanceType;
        this.cpuUsage = cpuUsage;
        this.monthlyCost = monthlyCost;
        this.recommendation = recommendation;
        this.estimateMonthlySaving = estimateMonthlySaving;

    }

    public String getInstanceId(){
        return instanceId;
    }

    public String getInstanceType(){
        return instanceType;
    }

    public String getState(){
        return state;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public double getMonthlyCost(){
        return monthlyCost;
    }

    public String getRecommendation(){
        return recommendation;
    }

    public double getEstimateMonthlySaving(){
        return estimateMonthlySaving;
    }

}
