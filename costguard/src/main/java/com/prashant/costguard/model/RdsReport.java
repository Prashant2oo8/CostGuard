package com.prashant.costguard.model;

import java.util.List;

public class RdsReport {

    private int totalDatabases;
    private double estimatedMonthlyCost;
    private List<RdsInstance> databases;

    public RdsReport(int totalDatabases,
                     double estimatedMonthlyCost,
                     List<RdsInstance> databases) {

        this.totalDatabases = totalDatabases;
        this.estimatedMonthlyCost = estimatedMonthlyCost;
        this.databases = databases;
    }

    public int getTotalDatabases() {
        return totalDatabases;
    }

    public double getEstimatedMonthlyCost() {
        return estimatedMonthlyCost;
    }

    public List<RdsInstance> getDatabases() {
        return databases;
    }
}