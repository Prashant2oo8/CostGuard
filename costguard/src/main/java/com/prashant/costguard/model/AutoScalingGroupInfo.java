package com.prashant.costguard.model;

public class AutoScalingGroupInfo {

    private String name;
    private int minSize;
    private int maxSize;
    private int desiredCapacity;
    private double monthlyCost;
    private String recommendation;

    public AutoScalingGroupInfo(String name,
                                int minSize,
                                int maxSize,
                                int desiredCapacity,
                                double monthlyCost,
                                String recommendation) {

        this.name = name;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.desiredCapacity = desiredCapacity;
        this.monthlyCost = monthlyCost;
        this.recommendation = recommendation;
    }

    public String getName() { return name; }

    public int getMinSize() { return minSize; }

    public int getMaxSize() { return maxSize; }

    public int getDesiredCapacity() { return desiredCapacity; }

    public double getMonthlyCost() { return monthlyCost; }

    public String getRecommendation() { return recommendation; }
}