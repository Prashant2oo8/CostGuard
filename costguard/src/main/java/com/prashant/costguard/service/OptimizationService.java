package com.prashant.costguard.service;

import com.prashant.costguard.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OptimizationService {

    public List<OptimizationRecommendation> analyzeAll(
            List<EC2Instance> ec2List,
            List<EbsVolume> ebsList,
            List<S3Bucket> s3List,
            List<RdsInstance> rdsList,
            List<LoadBalancerInfo> elbList,
            List<AutoScalingGroupInfo> asgList
    ) {

        List<OptimizationRecommendation> results = new ArrayList<>();

        /* =========================
           EC2 OPTIMIZATION (NO DUPLICATION)
        ========================= */
        for (EC2Instance instance : ec2List) {

            String id = instance.getInstanceId();
            double cost = instance.getMonthlyCost();
            double cpu = instance.getCpuUtilization();
            String state = instance.getState();

            String rec = "EC2: " + instance.getRecommendation();
            double saving = 0;

            // Savings based on recommendation
            if (rec.toLowerCase().contains("stop")) {
                saving = cost;
            }
            else if (rec.toLowerCase().contains("downsize")) {
                saving = cost * 0.4;
            }

            // Cost priority
            if (cost > 50 && cpu < 20 && !state.equalsIgnoreCase("stopped")) {
                rec += " | High cost - prioritize optimization";
            }

            results.add(new OptimizationRecommendation(id, rec, cpu, cost, saving));
        }

        if (ec2List.isEmpty()) {
            results.add(new OptimizationRecommendation("EC2", "No EC2 instances found", 0, 0, 0));
        }

        /* =========================
           EBS OPTIMIZATION (NO DUPLICATION)
        ========================= */
        for (EbsVolume volume : ebsList) {

            String id = volume.getVolumeId();
            double cost = volume.getMonthlyCost();
            String state = volume.getState();

            String rec = "EBS: " + volume.getRecommendation();
            double saving = 0;

            if (state.equalsIgnoreCase("available")) {
                saving = cost;
            }

            if (cost > 10 && state.equalsIgnoreCase("available")) {
                rec += " | High cost unused volume - delete to save cost";
            }

            results.add(new OptimizationRecommendation(id, rec, cost, cost, saving));
        }

        if (ebsList.isEmpty()) {
            results.add(new OptimizationRecommendation("EBS", "No EBS volumes found", 0, 0, 0));
        }

        /* =========================
           S3 OPTIMIZATION (NO DUPLICATION)
        ========================= */
        for (S3Bucket bucket : s3List) {

            String id = bucket.getBucketName();
            double cost = bucket.getMonthlyCost();
            double size = bucket.getStorageGB();

            String rec = "S3: " + bucket.getRecommendation();
            double saving = 0;

            if (rec.toLowerCase().contains("lifecycle") ||
                    rec.toLowerCase().contains("glacier")) {
                saving = cost * 0.3;
            }

            if (cost > 20) {
                rec += " | High cost storage - optimize lifecycle";
            }

            results.add(new OptimizationRecommendation(id, rec, size, cost, saving));
        }

        if (s3List.isEmpty()) {
            results.add(new OptimizationRecommendation("S3", "No S3 buckets found", 0, 0, 0));
        }

        /* =========================
           RDS OPTIMIZATION (NO DUPLICATION)
        ========================= */
        for (RdsInstance db : rdsList) {

            String id = db.getDbIdentifier();
            double cost = db.getMonthlyCost();
            String rec = "RDS: " + db.getRecommendation();
            double saving = 0;

            if (rec.toLowerCase().contains("stop")) {
                saving = cost;
            }
            else if (rec.toLowerCase().contains("reduce") ||
                    rec.toLowerCase().contains("downsize")) {
                saving = cost * 0.4;
            }

            // Cost priority (without CPU)
            if (cost > 50 && rec.toLowerCase().contains("underutilized")) {
                rec += " | High cost DB - prioritize optimization";
            }

            results.add(new OptimizationRecommendation(id, rec, cost, cost, saving));
        }
        if (rdsList.isEmpty()) {
            results.add(new OptimizationRecommendation("RDS", "No RDS instances found", 0, 0, 0));
        }

        /* =========================
           ELB OPTIMIZATION (NO DUPLICATION)
        ========================= */
        for (LoadBalancerInfo elb : elbList) {

            String id = elb.getName();
            double cost = elb.getMonthlyCost();

            String rec = "ELB: " + elb.getRecommendation();
            double saving = 0;

            if (rec.toLowerCase().contains("delete")) {
                saving = cost;
            }
            else if (rec.toLowerCase().contains("remove")) {
                saving = cost * 0.5;
            }

            if (cost > 15 && rec.toLowerCase().contains("low")) {
                rec += " | High cost with low traffic - optimize";
            }

            results.add(new OptimizationRecommendation(id, rec, cost, cost, saving));
        }
        if (elbList.isEmpty()) {
            results.add(new OptimizationRecommendation("ELB", "No load balancers found", 0, 0, 0));
        }

        /* =========================
           ASG OPTIMIZATION (NO DUPLICATION)
        ========================= */
        for (AutoScalingGroupInfo asg : asgList) {

            String id = asg.getName();
            double cost = asg.getMonthlyCost();

            String rec = "ASG: " + asg.getRecommendation();
            double saving = 0;

            if (rec.toLowerCase().contains("reduce")) {
                saving = cost * 0.3;
            }

            if (cost > 50 && rec.toLowerCase().contains("over")) {
                rec += " | High cost scaling group - optimize";
            }

            results.add(new OptimizationRecommendation(id, rec, cost, cost, saving));
        }

        if (asgList.isEmpty()) {
            results.add(new OptimizationRecommendation("ASG", "No auto scaling groups found", 0, 0, 0));
        }

        return results;
    }
}
