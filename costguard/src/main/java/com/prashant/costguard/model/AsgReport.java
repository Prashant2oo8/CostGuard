package com.prashant.costguard.model;

import java.util.List;

public class AsgReport {

    private int totalGroups;
    private double estimatedMonthlyCost;
    private List<AutoScalingGroupInfo> groups;

    public AsgReport(int totalGroups,
                     double estimatedMonthlyCost,
                     List<AutoScalingGroupInfo> groups) {

        this.totalGroups = totalGroups;
        this.estimatedMonthlyCost = estimatedMonthlyCost;
        this.groups = groups;
    }

    public int getTotalGroups() { return totalGroups; }

    public double getEstimatedMonthlyCost() { return estimatedMonthlyCost; }

    public List<AutoScalingGroupInfo> getGroups() { return groups; }
}