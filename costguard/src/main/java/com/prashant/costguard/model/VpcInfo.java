package com.prashant.costguard.model;

public class VpcInfo {

    private String resourceId;
    private String resourceType;
    private double currentCost;
    private double optimizedCost;
    private double savings;
    private String recommendation;
    private String action;

    public VpcInfo(String resourceId,
                   String resourceType,
                   double currentCost,
                   double optimizedCost,
                   double savings,
                   String recommendation,
                   String action) {
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.currentCost = currentCost;
        this.optimizedCost = optimizedCost;
        this.savings = savings;
        this.recommendation = recommendation;
        this.action = action;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public double getCurrentCost() {
        return currentCost;
    }

    public double getOptimizedCost() {
        return optimizedCost;
    }

    public double getSavings() {
        return savings;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public String getAction() {
        return action;
    }
}