package com.prashant.costguard.model;

public class S3Bucket {

    private String bucketName;
    private double storageGB;
    private double monthlyCost;
    private String recommendation;

    public S3Bucket(String bucketName, double storageGB,
                    double monthlyCost, String recommendation) {

        this.bucketName = bucketName;
        this.storageGB = storageGB;
        this.monthlyCost = monthlyCost;
        this.recommendation = recommendation;
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
}