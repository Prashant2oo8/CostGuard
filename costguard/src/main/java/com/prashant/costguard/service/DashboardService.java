package com.prashant.costguard.service;

import com.prashant.costguard.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    public Map<String, Object> generateSummary(
            List<OptimizationRecommendation> recommendations,
            List<EC2Instance> ec2List,
            List<EbsVolume> ebsList,
            List<S3Bucket> s3List,
            List<RdsInstance> rdsList,
            List<LoadBalancerInfo> elbList,
            List<AutoScalingGroupInfo> asgList
    ) {

        // RESOURCE COUNTS

        int totalEC2 = ec2List.size();
        int totalEBS = ebsList.size();
        int totalS3 = s3List.size();
        int totalRDS = rdsList.size();
        int totalELB = elbList.size();
        int totalASG = asgList.size();

        int totalResources = calculateTotalResources(
                totalEC2, totalEBS, totalS3, totalRDS, totalELB, totalASG
        );

        //   EC2 STATE ANALYSIS

        int[] ec2State = calculateEC2State(ec2List);
        int runningEC2 = ec2State[0];
        int stoppedEC2 = ec2State[1];

        // COST DISTRIBUTION

        double ec2Cost = ec2List.stream().mapToDouble(EC2Instance::getMonthlyCost).sum();
        double ebsCost = ebsList.stream().mapToDouble(EbsVolume::getMonthlyCost).sum();
        double s3Cost = s3List.stream().mapToDouble(S3Bucket::getMonthlyCost).sum();
        double rdsCost = rdsList.stream().mapToDouble(RdsInstance::getMonthlyCost).sum();
        double elbCost = elbList.stream().mapToDouble(LoadBalancerInfo::getMonthlyCost).sum();
        double asgCost = asgList.stream().mapToDouble(AutoScalingGroupInfo::getMonthlyCost).sum();

        // COST + SAVINGS
        Map<String, Double> costData = calculateCost(recommendations);

        double totalCost = costData.get("total");
        double totalSavings = costData.get("savings");

        //  WASTE + TOP RESOURCE

        Map<String, Object> wasteData = calculateWaste(recommendations);

        int wasteResources = (int) wasteData.get("waste");
        String topWasteResourceId = (String) wasteData.get("topId");
        double topWasteSaving = (double) wasteData.get("maxSaving");

        // TOP COST SERVICE

        Map<String, Double> costMap = new HashMap<>();
        costMap.put("EC2", ec2Cost);
        costMap.put("EBS", ebsCost);
        costMap.put("S3", s3Cost);
        costMap.put("RDS", rdsCost);
        costMap.put("ELB", elbCost);
        costMap.put("ASG", asgCost);

        String topService = "EC2";
        double maxCost = 0;

        for (Map.Entry<String, Double> entry : costMap.entrySet()) {
            if (entry.getValue() > maxCost) {
                maxCost = entry.getValue();
                topService = entry.getKey();
            }
        }

        // FINAL METRICS
        totalCost = round(totalCost);
        totalSavings = round(totalSavings);

        double optimizedCost = round(totalCost - totalSavings);

        int efficiencyScore = totalCost == 0
                ? 100
                : (int) Math.min(100, (totalSavings / totalCost) * 100);

        double savingsPercentage = totalCost == 0
                ? 0
                : round((totalSavings / totalCost) * 100);

        //SYSTEM HEALTH
        String systemHealth;

        if (efficiencyScore > 80) {
            systemHealth = "EXCELLENT";
        } else if (efficiencyScore > 50) {
            systemHealth = "MODERATE";
        } else {
            systemHealth = "POOR";
        }

        Map<String, Object> response = new HashMap<>();

        // OVERVIEW

        Map<String, Object> overview = new HashMap<>();
        overview.put("totalResources", totalResources);
        overview.put("runningEC2", runningEC2);
        overview.put("stoppedEC2", stoppedEC2);

        // COST

        Map<String, Object> cost = new HashMap<>();
        cost.put("totalCost", totalCost);
        cost.put("totalSavings", totalSavings);
        cost.put("optimizedCost", optimizedCost);
        cost.put("efficiencyScore", efficiencyScore);
        cost.put("savingsPercentage", savingsPercentage);

        cost.put("avgCostPerResource",
                totalResources == 0 ? 0 : round(totalCost / totalResources));

        // RESOURCES

        Map<String, Object> resources = new HashMap<>();
        resources.put("EC2", totalEC2);
        resources.put("EBS", totalEBS);
        resources.put("S3", totalS3);
        resources.put("RDS", totalRDS);
        resources.put("ELB", totalELB);
        resources.put("ASG", totalASG);

        //  DISTRIBUTION

        Map<String, Object> distribution = new HashMap<>();
        distribution.put("EC2", round(ec2Cost));
        distribution.put("EBS", round(ebsCost));
        distribution.put("S3", round(s3Cost));
        distribution.put("RDS", round(rdsCost));
        distribution.put("ELB", round(elbCost));
        distribution.put("ASG", round(asgCost));

        // INSIGHTS

        Map<String, Object> insights = new HashMap<>();
        insights.put("wasteResources", wasteResources);
        insights.put("topService", topService);
        insights.put("topWasteResourceId", topWasteResourceId);
        insights.put("topWasteSaving", round(topWasteSaving));
        insights.put("systemHealth", systemHealth);

        insights.put("resourceEfficiency",
                wasteResources == 0 ? "OPTIMAL" :
                        wasteResources <= 2 ? "MODERATE" : "POOR");

        // FINAL RESPONSE

        response.put("overview", overview);
        response.put("cost", cost);
        response.put("resources", resources);
        response.put("distribution", distribution);
        response.put("insights", insights);

        return response;
    }

    // HELPER METHODS
    private int calculateTotalResources(
            int ec2, int ebs, int s3, int rds, int elb, int asg
    ) {
        return ec2 + ebs + s3 + rds + elb + asg;
    }

    private int[] calculateEC2State(List<EC2Instance> ec2List) {
        int running = 0, stopped = 0;

        for (EC2Instance instance : ec2List) {
            if ("running".equalsIgnoreCase(instance.getState())) {
                running++;
            } else {
                stopped++;
            }
        }
        return new int[]{running, stopped};
    }

    private Map<String, Double> calculateCost(List<OptimizationRecommendation> recs) {

        double total = 0, savings = 0;

        for (OptimizationRecommendation rec : recs) {
            total += rec.getTotalMonthlyCost();
            savings += rec.getEstimatedMonthlySaving();
        }

        Map<String, Double> result = new HashMap<>();
        result.put("total", total);
        result.put("savings", savings);

        return result;
    }

    private Map<String, Object> calculateWaste(List<OptimizationRecommendation> recs) {

        int waste = 0;
        double maxSaving = 0;
        String topId = "N/A";

        for (OptimizationRecommendation rec : recs) {

            if (rec.getEstimatedMonthlySaving() > 0) {
                waste++;
            }

            if (rec.getEstimatedMonthlySaving() > maxSaving) {
                maxSaving = rec.getEstimatedMonthlySaving();
                topId = rec.getInstanceId();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("waste", waste);
        result.put("topId", topId);
        result.put("maxSaving", maxSaving);

        return result;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}