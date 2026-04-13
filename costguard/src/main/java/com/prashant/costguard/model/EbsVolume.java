package com.prashant.costguard.model;

public class EbsVolume {

    private String volumeId;
    private int size;
    private String state;
    private double monthlyCost;
    private String recommendation;
    private String volumeType;
    private double optimizedCost;
    private double savings;

    public EbsVolume(String volumeId, int size, String state, String volumeType,
                     double monthlyCost, String recommendation) {

        this.volumeId = volumeId;
        this.size = size;
        this.state = state;
        this.volumeType = volumeType;
        this.monthlyCost = monthlyCost;
        this.recommendation = recommendation;
        this.optimizedCost = monthlyCost;
        this.savings = 0;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public int getSize() {
        return size;
    }

    public String getState() {
        return state;
    }

    public String getVolumeType() {
        return volumeType;
    }

    public double getMonthlyCost() {
        return monthlyCost;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public double getOptimizedCost() {
        return optimizedCost;
    }

    public double getSavings() {
        return savings;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public void setOptimizedCost(double optimizedCost) {
        this.optimizedCost = optimizedCost;
    }

    public void setSavings(double savings) {
        this.savings = savings;
    }
}