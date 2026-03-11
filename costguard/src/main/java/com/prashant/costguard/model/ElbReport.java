package com.prashant.costguard.model;

import java.util.List;

public class ElbReport {

    private int totalLoadBalancers;
    private double estimatedMonthlyCost;
    private List<LoadBalancerInfo> loadBalancers;

    public ElbReport(int totalLoadBalancers,
                     double estimatedMonthlyCost,
                     List<LoadBalancerInfo> loadBalancers) {

        this.totalLoadBalancers = totalLoadBalancers;
        this.estimatedMonthlyCost = estimatedMonthlyCost;
        this.loadBalancers = loadBalancers;
    }

    public int getTotalLoadBalancers() { return totalLoadBalancers; }

    public double getEstimatedMonthlyCost() { return estimatedMonthlyCost; }

    public List<LoadBalancerInfo> getLoadBalancers() { return loadBalancers; }
}
