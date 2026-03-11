package com.prashant.costguard.model;

import java.util.List;

public class S3Report {

    private int totalBuckets;
    private double totalStorageGB;
    private double estimatedMonthlyCost;
    private List<S3Bucket> buckets;

    public S3Report(int totalBuckets,
                    double totalStorageGB,
                    double estimatedMonthlyCost,
                    List<S3Bucket> buckets) {

        this.totalBuckets = totalBuckets;
        this.totalStorageGB = totalStorageGB;
        this.estimatedMonthlyCost = estimatedMonthlyCost;
        this.buckets = buckets;
    }

    public int getTotalBuckets() {
        return totalBuckets;
    }

    public double getTotalStorageGB() {
        return totalStorageGB;
    }

    public double getEstimatedMonthlyCost() {
        return estimatedMonthlyCost;
    }

    public List<S3Bucket> getBuckets() {
        return buckets;
    }
}