package com.prashant.costguard.service;

import com.prashant.costguard.model.S3Bucket;
import com.prashant.costguard.model.S3Report;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final CloudWatchClient cloudWatchClient;

    public S3Service() {
        this.s3Client = S3Client.builder().build();
        this.cloudWatchClient = CloudWatchClient.builder().build();
    }

    public S3Report generateReport() {

        try {

            ListBucketsResponse response = s3Client.listBuckets();

            List<S3Bucket> buckets = new ArrayList<>();

            double totalStorage = 0;
            double totalCost = 0;

            for (Bucket bucket : response.buckets()) {

                String bucketName = bucket.name();

                double storageGB = getBucketSizeFromCloudWatch(bucketName);

                // Fallback if CloudWatch returns 0
                if (storageGB == 0) {
                    storageGB = 1;
                }

                double monthlyCost = calculateCost(storageGB);

                String recommendation = generateRecommendation(storageGB);

                if (monthlyCost > 20) {
                    recommendation += " | High cost bucket - prioritize optimization";
                }

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

    /* =========================
       CLOUDWATCH STORAGE LOGIC
    ========================= */
    private double getBucketSizeFromCloudWatch(String bucketName) {

        try {

            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace("AWS/S3")
                    .metricName("BucketSizeBytes")
                    .dimensions(
                            Dimension.builder()
                                    .name("BucketName")
                                    .value(bucketName)
                                    .build(),
                            Dimension.builder()
                                    .name("StorageType")
                                    .value("StandardStorage")
                                    .build()
                    )
                    .startTime(Instant.now().minus(2, ChronoUnit.DAYS))
                    .endTime(Instant.now())
                    .period(86400)
                    .statistics(Statistic.AVERAGE)
                    .build();

            GetMetricStatisticsResponse response =
                    cloudWatchClient.getMetricStatistics(request);

            if (!response.datapoints().isEmpty()) {

                double bytes = response.datapoints().stream()
                        .mapToDouble(Datapoint::average)
                        .max()
                        .orElse(0);

                // Convert bytes → GB
                return bytes / (1024 * 1024 * 1024);
            }

        } catch (Exception e) {
            System.err.println("CloudWatch error for bucket: " + bucketName);
        }

        return 0;
    }


    /* =========================
       COST CALCULATION
    ========================= */
    private double calculateCost(double storageGB) {

        double pricePerGB = 0.023;

        return storageGB * pricePerGB;
    }

    /* =========================
       OPTIMIZATION LOGIC
    ========================= */
    private String generateRecommendation(double storageGB) {

        if (storageGB == 0) {
            return "No storage data available - verify bucket metrics or usage";
        }

        if (storageGB > 50) {
            return "High storage usage - apply lifecycle policies or move data to Glacier";
        }
        else if (storageGB > 5) {
            return "Moderate storage - consider lifecycle optimization";
        }
        else if (storageGB > 1) {
            return "Storage usage normal - monitor periodically";
        }
        else {
            return "Very low storage - minimal cost";
        }
    }
}