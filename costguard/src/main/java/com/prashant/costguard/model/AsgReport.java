package com.prashant.costguard.model;

import java.util.List;

public class AsgReport {

    private int totalGroups;
    private List<AutoScalingGroupInfo> groups;
    private double totalCost;

    public AsgReport(int totalGroups,
                     List<AutoScalingGroupInfo> groups,
                     double totalCost) {

        this.totalGroups = totalGroups;
        this.groups = groups;
        this.totalCost = totalCost;
    }

    public int getTotalGroups() { return totalGroups; }

    public List<AutoScalingGroupInfo> getGroups() { return groups; }

    public double getTotalCost() {
        return totalCost;
    }
}