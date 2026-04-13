package com.prashant.costguard.model;

public class S3Bucket {

    private String bucketName;
    private double storageGB;
    private double monthlyCost;
    private String recommendation;
    private double optimizedCost;
    private double savings;

    public S3Bucket(String bucketName, double storageGB,
                    double monthlyCost, String recommendation) {

        this.bucketName = bucketName;
        this.storageGB = storageGB;
        this.monthlyCost = monthlyCost;
        this.recommendation = recommendation;
        this.optimizedCost = monthlyCost;
        this.savings = 0;
    }

    public String getBucketName() {
        return bucketName;
    }

    public double getStorageGB() {
        return storageGB;
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