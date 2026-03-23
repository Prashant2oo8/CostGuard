package com.prashant.costguard.model;

import java.util.List;

public class ElbReport {

    private int totalLoadBalancers;
    private List<LoadBalancerInfo> loadBalancers;
    private double totalCost;

    public ElbReport(int totalLoadBalancers,
                     List<LoadBalancerInfo> loadBalancers,
                     double totalCost) {

        this.totalLoadBalancers = totalLoadBalancers;
        this.loadBalancers = loadBalancers;
        this.totalCost = totalCost;
    }

    public int getTotalLoadBalancers() { return totalLoadBalancers; }

    public List<LoadBalancerInfo> getLoadBalancers() { return loadBalancers; }

    public double getTotalCost() {
        return totalCost;
    }
}
