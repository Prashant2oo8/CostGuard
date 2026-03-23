package com.prashant.costguard.service;

import com.prashant.costguard.model.*;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.*;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class RdsService {

    private final RdsClient rdsClient;
    private final CloudWatchClient cloudWatchClient;

    public RdsService() {
        this.rdsClient = RdsClient.create();
        this.cloudWatchClient = CloudWatchClient.create();
    }

    public RdsReport generateReport() {

        DescribeDbInstancesResponse response =
                rdsClient.describeDBInstances();

        List<RdsInstance> databases = new ArrayList<>();

        double totalCost = 0;

        for (DBInstance db : response.dbInstances()) {

            String id = db.dbInstanceIdentifier();
            String engine = db.engine();
            String instanceClass = db.dbInstanceClass();

            double monthlyCost = estimateCost(instanceClass);

            //  Get real metrics
            double cpu = getMetric(id, "CPUUtilization");
            double connections = getMetric(id, "DatabaseConnections");

            String recommendation = generateRecommendation(cpu, connections, monthlyCost);

            totalCost += monthlyCost;

            databases.add(
                    new RdsInstance(
                            id,
                            engine,
                            instanceClass,
                            monthlyCost,
                            recommendation
                    )
            );
        }

        return new RdsReport(
                databases.size(),
                databases,
                totalCost
        );
    }

    /* CLOUDWATCH METRIC FETCH */
    private double getMetric(String dbId, String metricName) {

        try {

            GetMetricStatisticsRequest request =
                    GetMetricStatisticsRequest.builder()
                            .namespace("AWS/RDS")
                            .metricName(metricName)
                            .dimensions(
                                    Dimension.builder()
                                            .name("DBInstanceIdentifier")
                                            .value(dbId)
                                            .build()
                            )
                            .startTime(Instant.now().minus(2, ChronoUnit.DAYS))
                            .endTime(Instant.now())
                            .period(3600)
                            .statistics(Statistic.AVERAGE)
                            .build();

            GetMetricStatisticsResponse response =
                    cloudWatchClient.getMetricStatistics(request);

            if (!response.datapoints().isEmpty()) {
                return response.datapoints().stream()
                        .mapToDouble(Datapoint::average)
                        .average()
                        .orElse(-1);
            }

        } catch (Exception e) {
            System.out.println("CloudWatch error for RDS: " + dbId);
        }

        return -1; // fallback
    }

    /* OPTIMIZATION LOGIC */
    private String generateRecommendation(double cpu, double connections, double cost) {

        String recommendation;

        // No data case
        if (cpu == -1 || connections == -1) {
            return "No monitoring data available - verify RDS metrics before taking action";
        }

        // Idle database
        if (cpu < 5 && connections < 5) {
            recommendation = "Idle database - consider stopping or downsizing";
        }

        // Low usage
        else if (cpu < 20 && connections < 20) {
            recommendation = "Low utilization - consider smaller instance";
        }

        // High usage
        else if (cpu > 80 || connections > 100) {
            recommendation = "High load - consider scaling database";
        }

        // Healthy
        else {
            recommendation = "Database usage is optimal";
        }

        // Cost priority
        if (cost > 50) {
            recommendation += " | High cost database - prioritize optimization";
        }

        return recommendation;
    }

    /* COST ESTIMATION */
    private double estimateCost(String instanceClass) {

        if (instanceClass.contains("micro")) {
            return 15;
        } else if (instanceClass.contains("small")) {
            return 30;
        } else if (instanceClass.contains("medium")) {
            return 50;
        } else {
            return 80;
        }
    }
}