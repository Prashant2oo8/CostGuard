package com.prashant.costguard.model;

public class EC2Instance {
    private String instanceId;
    private String instanceType;
    private String state;
    private double monthlyCost;

    public EC2Instance(String instanceId, String instanceType, String state, double monthlyCost){
        this.instanceId = instanceId;
        this.instanceType = instanceType;
        this.state = state;
        this.monthlyCost = monthlyCost;
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
}

