package com.prashant.costguard.service;

import com.prashant.costguard.model.S3Bucket;
import com.prashant.costguard.model.S3Report;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import software.amazon.awssdk.regions.Region;

import java.util.ArrayList;
import java.util.List;

@Service
public class S3Service {

    private final S3Client s3Client;

    public S3Service() {

        this.s3Client = S3Client.builder().build();
    }

    public S3Report generateReport() {

        try {

            ListBucketsResponse response = s3Client.listBuckets();

            List<S3Bucket> buckets = new ArrayList<>();

            double totalStorage = 0;
            double totalCost = 0;

            for (Bucket bucket : response.buckets()) {

                String bucketName = bucket.name();

                double storageGB = 1;

                double monthlyCost = calculateCost(storageGB);

                String recommendation = "Monitor storage usage";

                totalStorage += storageGB;
                totalCost += monthlyCost;

                buckets.add(
                        new S3Bucket(bucketName, storageGB, monthlyCost, recommendation)
                );
            }

            return new S3Report(
                    buckets.size(),
                    totalStorage,
                    totalCost,
                    buckets
            );

        } catch (Exception e) {

            e.printStackTrace();

            return new S3Report(
                    0,
                    0,
                    0,
                    new ArrayList<>()
            );
        }
    }
    private double calculateCost(double storageGB) {

        double pricePerGB = 0.023;

        return storageGB * pricePerGB;
    }
}