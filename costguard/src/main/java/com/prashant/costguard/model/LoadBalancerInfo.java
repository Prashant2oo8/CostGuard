package com.prashant.costguard.model;

public class LoadBalancerInfo {

    private String name;
    private String type;
    private String state;
    private double monthlyCost;
    private String recommendation;
    private double optimizedCost;
    private double savings;

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
        this.optimizedCost = monthlyCost;
        this.savings = 0;
    }

    public String getName() { return name; }

    public String getType() { return type; }

    public String getState() { return state; }

    public double getMonthlyCost() { return monthlyCost; }

    public String getRecommendation() { return recommendation; }

    public double getOptimizedCost() { return optimizedCost; }

    public double getSavings() { return savings; }

    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public void setOptimizedCost(double optimizedCost) { this.optimizedCost = optimizedCost; }

    public void setSavings(double savings) { this.savings = savings; }
}