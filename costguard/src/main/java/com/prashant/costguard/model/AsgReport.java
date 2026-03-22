package com.prashant.costguard.model;

import java.util.List;

public class AsgReport {

    private int totalGroups;
    private double estimatedMonthlyCost;
    private List<AutoScalingGroupInfo> groups;
    private double totalCost;

    public AsgReport(int totalGroups,
                     double estimatedMonthlyCost,
                     List<AutoScalingGroupInfo> groups,
                     double totalCost) {

        this.totalGroups = totalGroups;
        this.estimatedMonthlyCost = estimatedMonthlyCost;
        this.groups = groups;
        this.totalCost = totalCost;
    }

    public int getTotalGroups() { return totalGroups; }

    public double getEstimatedMonthlyCost() { return estimatedMonthlyCost; }

    public List<AutoScalingGroupInfo> getGroups() { return groups; }

    public double getTotalCost() {
        return totalCost;
    }
}