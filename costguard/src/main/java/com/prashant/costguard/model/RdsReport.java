package com.prashant.costguard.model;

import java.util.List;

public class RdsReport {

    private int totalDatabases;
    private List<RdsInstance> databases;
    private double totalCost;

    public RdsReport(int totalDatabases,
                     List<RdsInstance> databases,
                     double totalCost) {

        this.totalDatabases = totalDatabases;
        this.databases = databases;
        this.totalCost = totalCost;
    }

    public int getTotalDatabases() {
        return totalDatabases;
    }

    public List<RdsInstance> getDatabases() {
        return databases;
    }

    public double getTotalCost() {
        return totalCost;
    }

}