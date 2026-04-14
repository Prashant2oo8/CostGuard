package com.prashant.costguard.service;

import com.prashant.costguard.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OptimizationService {

    private final VpcService vpcService;

    public OptimizationService(VpcService vpcService) {
        this.vpcService = vpcService;
    }

    public List<OptimizationRecommendation> analyzeAll(
            List<EC2Instance> ec2List,
            List<EbsVolume> ebsList,
            List<S3Bucket> s3List,
            List<RdsInstance> rdsList,
            List<LoadBalancerInfo> elbList,
            List<AutoScalingGroupInfo> asgList
    ) {
        List<OptimizationRecommendation> results = new ArrayList<>();

        analyzeEc2(ec2List, results);
        analyzeEbs(ebsList, results);
        analyzeS3(s3List, results);
        analyzeRds(rdsList, results);
        analyzeElb(elbList, results);
        analyzeAsg(asgList, results);
        analyzeVpc(results);

        return results;
    }

    private void analyzeVpc(List<OptimizationRecommendation> results) {
        List<VpcInfo> vpcResults = vpcService.getVpcOptimizations();

        for (VpcInfo vpcResult : vpcResults) {
            String recommendation = "VPC: " + vpcResult.getRecommendation()
                    + " | action=" + vpcResult.getAction();

            results.add(new OptimizationRecommendation(
                    vpcResult.getResourceId(),
                    recommendation,
                    vpcResult.getOptimizedCost(),
                    vpcResult.getCurrentCost(),
                    vpcResult.getSavings()
            ));
        }
    }

    private void analyzeEc2(List<EC2Instance> ec2List, List<OptimizationRecommendation> results) {
        for (EC2Instance instance : ec2List) {
            double cost = instance.getMonthlyCost();
            double cpu = instance.getCpuUtilization();
            String state = instance.getState();

            String recommendation;
            double optimizedCost;

            if (state.equalsIgnoreCase("stopped")) {
                recommendation = "EC2: Instance already stopped";
                optimizedCost = cost;
            } else if (cpu < 5) {
                recommendation = "EC2: Idle workload detected (low CPU/network) - stop instance";
                optimizedCost = 0;
            } else if (cpu < 30) {
                recommendation = "EC2: Underutilized - downsize instance";
                optimizedCost = cost * 0.5;
            } else if (cpu <= 80) {
                recommendation = "EC2: Steady workload - consider Savings Plan";
                optimizedCost = cost * 0.75;
            } else {
                recommendation = "EC2: Overutilized - scale up, no savings from downsizing";
                optimizedCost = cost;
            }

            double savings = calculateSavings(cost, optimizedCost);
            updateOptimization(instance, recommendation, optimizedCost, savings);

            results.add(new OptimizationRecommendation(
                    instance.getInstanceId(),
                    recommendation,
                    cpu,
                    cost,
                    savings
            ));
        }

        if (ec2List.isEmpty()) {
            results.add(new OptimizationRecommendation("EC2", "No EC2 instances found", 0, 0, 0));
        }
    }

    private void analyzeEbs(List<EbsVolume> ebsList, List<OptimizationRecommendation> results) {
        for (EbsVolume volume : ebsList) {
            double cost = volume.getMonthlyCost();
            String state = volume.getState();
            String volumeType = volume.getVolumeType() == null ? "" : volume.getVolumeType().toLowerCase();

            String recommendation;
            double optimizedCost;

            if (state.equalsIgnoreCase("available")) {
                recommendation = "EBS: Unattached volume - snapshot and delete";
                optimizedCost = 0;
            } else if (volume.getRecommendation().toLowerCase().contains("low iops")
                    || volume.getRecommendation().toLowerCase().contains("idle")) {
                recommendation = "EBS: Very low IOPS - delete or downgrade volume";
                optimizedCost = cost * 0.5;
            } else if (volumeType.equals("gp2") || volumeType.equals("io1")) {
                recommendation = "EBS: Convert to gp3 for better price-performance";
                optimizedCost = cost * 0.75;
            } else {
                recommendation = "EBS: Actively used - no optimization needed";
                optimizedCost = cost;
            }

            double savings = calculateSavings(cost, optimizedCost);
            updateOptimization(volume, recommendation, optimizedCost, savings);

            results.add(new OptimizationRecommendation(
                    volume.getVolumeId(),
                    recommendation,
                    cost,
                    cost,
                    savings
            ));
        }

        if (ebsList.isEmpty()) {
            results.add(new OptimizationRecommendation("EBS", "No EBS volumes found", 0, 0, 0));
        }
    }

    private void analyzeS3(List<S3Bucket> s3List, List<OptimizationRecommendation> results) {
        for (S3Bucket bucket : s3List) {
            double cost = bucket.getMonthlyCost();
            double storageGb = bucket.getStorageGB();

            String recommendation;
            double optimizedCost;

            if (storageGb < 1) {
                recommendation = "S3: Very small bucket - no meaningful savings";
                optimizedCost = cost;
            } else if (storageGb < 20) {
                recommendation = "S3: Frequently accessed data - keep STANDARD";
                optimizedCost = cost;
            } else if (storageGb < 100) {
                recommendation = "S3: Infrequent access (30-90 days) - move to STANDARD_IA";
                optimizedCost = cost * 0.6;
            } else if (storageGb < 500) {
                recommendation = "S3: Cold data (>90 days) - move to GLACIER";
                optimizedCost = cost * 0.2;
            } else {
                recommendation = "S3: Archive data (>180 days) - move to DEEP_ARCHIVE";
                optimizedCost = cost * 0.1;
            }

            double savings = calculateSavings(cost, optimizedCost);
            updateOptimization(bucket, recommendation, optimizedCost, savings);

            results.add(new OptimizationRecommendation(
                    bucket.getBucketName(),
                    recommendation,
                    storageGb,
                    cost,
                    savings
            ));
        }

        if (s3List.isEmpty()) {
            results.add(new OptimizationRecommendation("S3", "No S3 buckets found", 0, 0, 0));
        }
    }

    private void analyzeRds(List<RdsInstance> rdsList, List<OptimizationRecommendation> results) {
        for (RdsInstance db : rdsList) {
            double cost = db.getMonthlyCost();
            String existingRec = db.getRecommendation().toLowerCase();

            String recommendation;
            double optimizedCost;

            if (existingRec.contains("0 connection") || existingRec.contains("no connection")) {
                recommendation = "RDS: Low CPU and zero connections - stop database";
                optimizedCost = 0;
            } else if (existingRec.contains("low") || existingRec.contains("underutilized")) {
                recommendation = "RDS: Underutilized - downsize database instance";
                optimizedCost = cost * 0.6;
            } else if (existingRec.contains("steady") || existingRec.contains("optimal")) {
                recommendation = "RDS: Steady workload - use Reserved Instance";
                optimizedCost = cost * 0.7;
            } else if (existingRec.contains("high") || existingRec.contains("over")) {
                recommendation = "RDS: Overutilized - scale up, no savings";
                optimizedCost = cost;
            } else {
                recommendation = "RDS: Monitor usage for optimization opportunity";
                optimizedCost = cost;
            }

            double savings = calculateSavings(cost, optimizedCost);
            updateOptimization(db, recommendation, optimizedCost, savings);

            results.add(new OptimizationRecommendation(
                    db.getDbIdentifier(),
                    recommendation,
                    cost,
                    cost,
                    savings
            ));
        }

        if (rdsList.isEmpty()) {
            results.add(new OptimizationRecommendation("RDS", "No RDS instances found", 0, 0, 0));
        }
    }

    private void analyzeElb(List<LoadBalancerInfo> elbList, List<OptimizationRecommendation> results) {
        for (LoadBalancerInfo elb : elbList) {
            double cost = elb.getMonthlyCost();
            String existingRec = elb.getRecommendation().toLowerCase();

            String recommendation;
            double optimizedCost;

            if (existingRec.contains("0 request") || existingRec.contains("no traffic") || existingRec.contains("unused")) {
                recommendation = "ELB: No requests - delete load balancer";
                optimizedCost = 0;
            } else if (existingRec.contains("low")) {
                recommendation = "ELB: Very low traffic - consolidate load balancers";
                optimizedCost = cost * 0.5;
            } else {
                recommendation = "ELB: Active traffic - keep current configuration";
                optimizedCost = cost;
            }

            double savings = calculateSavings(cost, optimizedCost);
            updateOptimization(elb, recommendation, optimizedCost, savings);

            results.add(new OptimizationRecommendation(
                    elb.getName(),
                    recommendation,
                    cost,
                    cost,
                    savings
            ));
        }

        if (elbList.isEmpty()) {
            results.add(new OptimizationRecommendation("ELB", "No load balancers found", 0, 0, 0));
        }
    }

    private void analyzeAsg(List<AutoScalingGroupInfo> asgList, List<OptimizationRecommendation> results) {
        for (AutoScalingGroupInfo asg : asgList) {
            double cost = asg.getMonthlyCost();
            String existingRec = asg.getRecommendation().toLowerCase();

            String recommendation;
            double optimizedCost;

            if (existingRec.contains("avg cpu <10") || existingRec.contains("very low")) {
                recommendation = "ASG: Very low avg CPU - scale to minimum/zero";
                optimizedCost = 0;
            } else if (existingRec.contains("low") || existingRec.contains("underutilized")) {
                recommendation = "ASG: Low avg CPU - reduce desired capacity";
                optimizedCost = cost * 0.7;
            } else if (existingRec.contains("high") || existingRec.contains("over")) {
                recommendation = "ASG: High avg CPU - scale up, no savings";
                optimizedCost = cost;
            } else {
                recommendation = "ASG: Capacity balanced - monitor scaling policy";
                optimizedCost = cost;
            }

            double savings = calculateSavings(cost, optimizedCost);
            updateOptimization(asg, recommendation, optimizedCost, savings);

            results.add(new OptimizationRecommendation(
                    asg.getName(),
                    recommendation,
                    cost,
                    cost,
                    savings
            ));
        }

        if (asgList.isEmpty()) {
            results.add(new OptimizationRecommendation("ASG", "No auto scaling groups found", 0, 0, 0));
        }
    }

    private double calculateSavings(double currentCost, double optimizedCost) {
        double nonNegativeOptimized = Math.max(0, optimizedCost);
        return Math.max(0, currentCost - nonNegativeOptimized);
    }

    private void updateOptimization(EC2Instance instance, String recommendation, double optimizedCost, double savings) {
        instance.setRecommendation(recommendation);
        instance.setOptimizedCost(Math.max(0, optimizedCost));
        instance.setSavings(savings);
    }

    private void updateOptimization(EbsVolume volume, String recommendation, double optimizedCost, double savings) {
        volume.setRecommendation(recommendation);
        volume.setOptimizedCost(Math.max(0, optimizedCost));
        volume.setSavings(savings);
    }

    private void updateOptimization(S3Bucket bucket, String recommendation, double optimizedCost, double savings) {
        bucket.setRecommendation(recommendation);
        bucket.setOptimizedCost(Math.max(0, optimizedCost));
        bucket.setSavings(savings);
    }

    private void updateOptimization(RdsInstance db, String recommendation, double optimizedCost, double savings) {
        db.setRecommendation(recommendation);
        db.setOptimizedCost(Math.max(0, optimizedCost));
        db.setSavings(savings);
    }

    private void updateOptimization(LoadBalancerInfo elb, String recommendation, double optimizedCost, double savings) {
        elb.setRecommendation(recommendation);
        elb.setOptimizedCost(Math.max(0, optimizedCost));
        elb.setSavings(savings);
    }

    private void updateOptimization(AutoScalingGroupInfo asg, String recommendation, double optimizedCost, double savings) {
        asg.setRecommendation(recommendation);
        asg.setOptimizedCost(Math.max(0, optimizedCost));
        asg.setSavings(savings);
    }
}