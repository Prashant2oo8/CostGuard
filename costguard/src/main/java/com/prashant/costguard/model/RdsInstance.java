package com.prashant.costguard.model;

public class RdsInstance {

    private String dbIdentifier;
    private String engine;
    private String instanceClass;
    private double monthlyCost;
    private String recommendation;

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
}