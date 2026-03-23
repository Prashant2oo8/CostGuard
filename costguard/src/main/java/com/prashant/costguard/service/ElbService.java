package com.prashant.costguard.service;

import com.prashant.costguard.model.*;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.*;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ElbService {

    private final ElasticLoadBalancingV2Client elbClient;
    private final CloudWatchClient cloudWatchClient;

    public ElbService() {
        this.elbClient = ElasticLoadBalancingV2Client.create();
        this.cloudWatchClient = CloudWatchClient.create();
    }

    public ElbReport generateReport() {

        DescribeLoadBalancersResponse response =
                elbClient.describeLoadBalancers();

        List<LoadBalancerInfo> loadBalancers = new ArrayList<>();

        double totalCost = 0;

        for (LoadBalancer lb : response.loadBalancers()) {

            String name = lb.loadBalancerName();
            String type = lb.typeAsString();
            String state = lb.state().codeAsString();

            double monthlyCost = type.equalsIgnoreCase("application") ? 18 : 25;

            double requests = getRequestCount(lb.loadBalancerArn());

            String recommendation = generateRecommendation(requests, monthlyCost);

            totalCost += monthlyCost;

            loadBalancers.add(
                    new LoadBalancerInfo(
                            name,
                            type,
                            state,
                            monthlyCost,
                            recommendation
                    )
            );
        }

        return new ElbReport(
                loadBalancers.size(),
                loadBalancers,
                totalCost
        );
    }

    /* =========================
       CLOUDWATCH REQUEST COUNT
    ========================= */
    private double getRequestCount(String loadBalancerArn) {

        try {

            String[] parts = loadBalancerArn.split("loadbalancer/");
            String dimensionValue = parts.length > 1 ? parts[1] : loadBalancerArn;

            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace("AWS/ApplicationELB")
                    .metricName("RequestCount")
                    .dimensions(
                            Dimension.builder()
                                    .name("LoadBalancer")
                                    .value(dimensionValue)
                                    .build()
                    )
                    .startTime(Instant.now().minusSeconds(86400))
                    .endTime(Instant.now())
                    .period(86400)
                    .statistics(Statistic.SUM)
                    .build();

            GetMetricStatisticsResponse response =
                    cloudWatchClient.getMetricStatistics(request);

            if (!response.datapoints().isEmpty()) {
                return response.datapoints().stream()
                        .mapToDouble(Datapoint::sum)
                        .max()
                        .orElse(0);
            }

        } catch (Exception e) {
            System.err.println("ELB metric error: " + loadBalancerArn);
        }

        return -1;
    }

    /* =========================
       OPTIMIZATION LOGIC
    ========================= */
    private String generateRecommendation(double requests, double cost) {

        String recommendation;

        // No data
        if (requests == -1) {
            return "No traffic data available - load balancer may be idle or monitoring not enabled";
        }

        // Unused (highest waste)
        if (requests == 0) {
            recommendation = "No traffic - unused load balancer, consider deleting";
        }

        // Very low traffic
        else if (requests < 1000) {
            recommendation = "Very low traffic - consider removing or consolidating";
        }

        // Low traffic
        else if (requests < 10000) {
            recommendation = "Low traffic - review necessity of load balancer";
        }

        // High traffic
        else if (requests > 100000) {
            recommendation = "High traffic - ensure scaling and health checks configured";
        }

        // Normal
        else {
            recommendation = "Load balancer usage is normal";
        }

    /* =========================
       COST PRIORITY LAYER (NEW)
    ========================= */

        if (cost > 15) {

            if (requests == 0) {
                recommendation += " | High cost with no usage - immediate removal recommended";
            }

            else if (requests < 1000) {
                recommendation += " | High cost with low traffic - optimize or remove";
            }

            else if (requests < 10000) {
                recommendation += " | Cost is high relative to traffic - review configuration";
            }

            else {
                recommendation += " | Cost justified by traffic";
            }
        }

        return recommendation;
    }
}