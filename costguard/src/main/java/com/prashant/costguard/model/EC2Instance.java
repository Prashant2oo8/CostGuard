package com.prashant.costguard.model;

public class EC2Instance {
    private String instanceId;
    private String instanceType;
    private String state;
    private double monthlyCost;
    private double cpuUtilization;
    private String recommendation;

    public EC2Instance(String instanceId, String instanceType, String state, double monthlyCost, double cpuUtilization, String recommendation){
        this.instanceId = instanceId;
        this.instanceType = instanceType;
        this.state = state;
        this.monthlyCost = monthlyCost;
        this.cpuUtilization = cpuUtilization;
        this.recommendation = recommendation;
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
}

