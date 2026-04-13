package com.prashant.costguard.model;

public class EC2Instance {
    private String instanceId;
    private String instanceType;
    private String state;
    private double monthlyCost;
    private double cpuUtilization;
    private String recommendation;
    private double optimizedCost;
    private double savings;

    public EC2Instance(String instanceId, String instanceType, String state, double monthlyCost, double cpuUtilization, String recommendation){
        this.instanceId = instanceId;
        this.instanceType = instanceType;
        this.state = state;
        this.monthlyCost = monthlyCost;
        this.cpuUtilization = cpuUtilization;
        this.recommendation = recommendation;
        this.optimizedCost = monthlyCost;
        this.savings = 0;
    }


    public String getInstanceId(){
        return instanceId;
    }

    public String getInstanceType(){
        return instanceType;
    }

    public String getState() {
        return state;
    }
    public double getMonthlyCost(){
        return monthlyCost;
    }

    public double getCpuUtilization(){
        return cpuUtilization;
    }
    public String getRecommendation(){
        return recommendation;
    }

    public double getOptimizedCost() {
        return optimizedCost;
    }

    public double getSavings() {
        return savings;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public void setOptimizedCost(double optimizedCost) {
        this.optimizedCost = optimizedCost;
    }

    public void setSavings(double savings) {
        this.savings = savings;
    }
}