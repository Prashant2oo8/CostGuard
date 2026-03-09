package com.prashant.costguard.model;

public class DashboardSummary {

    private int totalInstances;
    private int runningInstances;
    private int stoppedInstances;
    private double totalMonthlyCost;
    private double potentialMonthlySaving;

    public DashboardSummary(int totalInstances, int runningInstances, int stoppedInstances, double totalMonthlyCost, double potentialMonthlySaving){

        this.totalInstances = totalInstances;
        this.runningInstances = runningInstances;
        this.stoppedInstances = stoppedInstances;
        this.totalMonthlyCost = totalMonthlyCost;
        this.potentialMonthlySaving = potentialMonthlySaving;

    }

    public int getTotalInstances() {
        return totalInstances;
    }

    public int getRunningInstances() {
        return runningInstances;
    }

    public int getStoppedInstances() {
        return stoppedInstances;
    }

    public double getTotalMonthlyCost() {
        return totalMonthlyCost;
    }

    public double getPotentialMonthlySaving() {
        return potentialMonthlySaving;
    }
}
