package com.prashant.costguard.model;

public class LoadBalancerInfo {

    private String name;
    private String type;
    private String state;
    private double monthlyCost;
    private String recommendation;

    public LoadBalancerInfo(String name,
                            String type,
                            String state,
                            double monthlyCost,
                            String recommendation) {

        this.name = name;
        this.type = type;
        this.state = state;
        this.monthlyCost = monthlyCost;
        this.recommendation = recommendation;
    }

    public String getName() { return name; }

    public String getType() { return type; }

    public String getState() { return state; }

    public double getMonthlyCost() { return monthlyCost; }

    public String getRecommendation() { return recommendation; }
}