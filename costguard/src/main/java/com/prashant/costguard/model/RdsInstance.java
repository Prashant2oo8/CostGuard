package com.prashant.costguard.model;

public class RdsInstance {

    private String dbIdentifier;
    private String engine;
    private String instanceClass;
    private double monthlyCost;
    private String recommendation;
    private double optimizedCost;
    private double savings;

    public RdsInstance(String dbIdentifier,
                       String engine,
                       String instanceClass,
                       double monthlyCost,
                       String recommendation) {

        this.dbIdentifier = dbIdentifier;
        this.engine = engine;
        this.instanceClass = instanceClass;
        this.monthlyCost = monthlyCost;
        this.recommendation = recommendation;
        this.optimizedCost = monthlyCost;
        this.savings = 0;
    }

    public String getDbIdentifier() {
        return dbIdentifier;
    }

    public String getEngine() {
        return engine;
    }

    public String getInstanceClass() {
        return instanceClass;
    }

    public double getMonthlyCost() {
        return monthlyCost;
    }

    public String getRecommendation() {
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