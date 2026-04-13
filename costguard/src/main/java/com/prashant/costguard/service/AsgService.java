package com.prashant.costguard.service;

import com.prashant.costguard.model.*;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.*;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AsgService {

    private final AutoScalingClient asgClient;
    private final CloudWatchClient cloudWatchClient;

    public AsgService() {
        this.asgClient = AutoScalingClient.create();
        this.cloudWatchClient = CloudWatchClient.create();
    }

    public AsgReport generateReport() {

        DescribeAutoScalingGroupsResponse response =
                asgClient.describeAutoScalingGroups();

        List<AutoScalingGroupInfo> groups = new ArrayList<>();

        double totalCost = 0;

        // Edge case: no ASG present
        if (response.autoScalingGroups().isEmpty()) {
            return new AsgReport(
                    0,
                    new ArrayList<>(),
                    0
            );
        }

        for (AutoScalingGroup group : response.autoScalingGroups()) {

            String name = group.autoScalingGroupName();
            int min = group.minSize();
            int max = group.maxSize();
            int desired = group.desiredCapacity();

            double monthlyCost = desired * 8;

            double cpu = getCpuUtilization(name);

            String recommendation = generateRecommendation(cpu, min, max, desired);

            // Cost priority
            if (monthlyCost > 50 && cpu < 20 && desired > (min + 1)) {
                recommendation += " | High cost with low utilization - optimize scaling";
            }

            totalCost += monthlyCost;

            groups.add(
                    new AutoScalingGroupInfo(
                            name,
                            min,
                            max,
                            desired,
                            monthlyCost,
                            recommendation
                    )
            );
        }

        return new AsgReport(
                groups.size(),
                groups,
                totalCost
        );
    }

    /* =========================
       CLOUDWATCH CPU METRIC
    ========================= */
    private double getCpuUtilization(String groupName) {

        try {
            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace("AWS/EC2")
                    .metricName("CPUUtilization")
                    .dimensions(
                            Dimension.builder()
                                    .name("AutoScalingGroupName")
                                    .value(groupName)
                                    .build()
                    )
                    .startTime(Instant.now().minusSeconds(86400))
                    .endTime(Instant.now())
                    .period(3600)
                    .statistics(Statistic.AVERAGE)
                    .build();

            GetMetricStatisticsResponse response =
                    cloudWatchClient.getMetricStatistics(request);

            if (!response.datapoints().isEmpty()) {
                return response.datapoints().stream()
                        .mapToDouble(Datapoint::average)
                        .max()
                        .orElse(-1);
            }

        } catch (Exception e) {
            System.err.println("ASG metric error: " + groupName);
        }

        return -1;
    }

    /* =========================
       OPTIMIZATION LOGIC
    ========================= */
    private String generateRecommendation(double cpu, int min, int max, int desired) {

        // FIRST check idle
        if (desired == 0) {
            return "No running instances - auto scaling group is idle";
        }

        // THEN check data
        if (cpu == -1) {
            return "No CPU data available - verify monitoring";
        }

        // No scaling flexibility
        if (min == max) {
            return "Auto scaling disabled - min and max capacity are equal";
        }

        // Over-provisioned
        if (cpu < 10 && desired > min + 1) {
            return "Low CPU usage with excess capacity - reduce instance count";
        }

        // Under-provisioned
        if (cpu > 80 && desired >= max) {
            return "High CPU and max capacity reached - increase max size";
        }

        // Detect inefficient configuration FIRST
        if ((max - min) <= 1) {
            return "Limited scaling range - consider increasing max capacity";
        }

        // THEN check inefficiency
        if (desired > (min + 2)) {
            return "Capacity higher than baseline - review scaling policy";
        }

        return "Auto scaling configuration is optimal";
    }
}