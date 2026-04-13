package com.prashant.costguard.service;

import com.prashant.costguard.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CloudService {

    private final EC2Service ec2Service;
    private final EbsService ebsService;
    private final S3Service s3Service;
    private final RdsService rdsService;
    private final ElbService elbService;
    private final AsgService asgService;
    private final OptimizationService optimizationService;

    public CloudService(
            EC2Service ec2Service,
            EbsService ebsService,
            S3Service s3Service,
            RdsService rdsService,
            ElbService elbService,
            AsgService asgService,
            OptimizationService optimizationService
    ) {
        this.ec2Service = ec2Service;
        this.ebsService = ebsService;
        this.s3Service = s3Service;
        this.rdsService = rdsService;
        this.elbService = elbService;
        this.asgService = asgService;
        this.optimizationService = optimizationService;
    }

    public CloudReport generateReport() {

        List<CostResource> expensiveResources = new ArrayList<>();
        List<WasteResource> wasteResources = new ArrayList<>();
        List<String> insights = new ArrayList<>();

        /* =========================
           FETCH ALL DATA (NO DUPLICATION)
        ========================= */
        List<EC2Instance> ec2Instances = ec2Service.getAllInstances();
        EbsReport ebsReport = ebsService.generateReport();
        S3Report s3Report = s3Service.generateReport();
        RdsReport rdsReport = rdsService.generateReport();
        ElbReport elbReport = elbService.generateReport();
        AsgReport asgReport = asgService.generateReport();

        optimizationService.analyzeAll(
                ec2Instances,
                ebsReport.getVolumes(),
                s3Report.getBuckets(),
                rdsReport.getDatabases(),
                elbReport.getLoadBalancers(),
                asgReport.getGroups()
        );

        /* =========================
           COST CALCULATION (USE SERVICE DATA)
        ========================= */
        double ec2Cost = ec2Instances.stream()
                .mapToDouble(EC2Instance::getMonthlyCost)
                .sum();

        double ebsCost = ebsReport.getTotalMonthlyCost();
        double s3Cost = s3Report.getEstimatedMonthlyCost();
        double rdsCost = rdsReport.getTotalCost();
        double elbCost = elbReport.getTotalCost();
        double asgCost = asgReport.getTotalCost();

        double currentCost = ec2Cost + ebsCost + s3Cost + rdsCost + elbCost + asgCost;
        double optimizedCost = ec2Instances.stream().mapToDouble(EC2Instance::getOptimizedCost).sum()
                + ebsReport.getVolumes().stream().mapToDouble(EbsVolume::getOptimizedCost).sum()
                + s3Report.getBuckets().stream().mapToDouble(S3Bucket::getOptimizedCost).sum()
                + rdsReport.getDatabases().stream().mapToDouble(RdsInstance::getOptimizedCost).sum()
                + elbReport.getLoadBalancers().stream().mapToDouble(LoadBalancerInfo::getOptimizedCost).sum()
                + asgReport.getGroups().stream().mapToDouble(AutoScalingGroupInfo::getOptimizedCost).sum();

        double potentialSavings = Math.max(0, currentCost - optimizedCost);

        /* =========================
           ADD ALL RESOURCES (NO NESTED LOOP)
        ========================= */

        // EC2
        ec2Instances.forEach(i ->
                expensiveResources.add(new CostResource(
                        i.getInstanceId(),
                        "EC2",
                        i.getMonthlyCost()
                ))
        );

        // EBS
        ebsReport.getVolumes().forEach(v ->
                expensiveResources.add(new CostResource(
                        v.getVolumeId(),
                        "EBS",
                        v.getMonthlyCost()
                ))
        );

        // S3
        s3Report.getBuckets().forEach(b ->
                expensiveResources.add(new CostResource(
                        b.getBucketName(),
                        "S3",
                        b.getMonthlyCost()
                ))
        );

        // RDS
        rdsReport.getDatabases().forEach(db ->
                expensiveResources.add(new CostResource(
                        db.getDbIdentifier(),
                        "RDS",
                        db.getMonthlyCost()
                ))
        );

        // ELB
        elbReport.getLoadBalancers().forEach(lb ->
                expensiveResources.add(new CostResource(
                        lb.getName(),
                        "ELB",
                        lb.getMonthlyCost()
                ))
        );

        // ASG
        asgReport.getGroups().forEach(group ->
                expensiveResources.add(new CostResource(
                        group.getName(),
                        "ASG",
                        group.getMonthlyCost()
                ))
        );

        ec2Instances.stream()
                .filter(i -> i.getSavings() > 0)
                .forEach(i -> wasteResources.add(new WasteResource(i.getInstanceId(), i.getRecommendation(), i.getSavings())));
        ebsReport.getVolumes().stream()
                .filter(v -> v.getSavings() > 0)
                .forEach(v -> wasteResources.add(new WasteResource(v.getVolumeId(), v.getRecommendation(), v.getSavings())));
        s3Report.getBuckets().stream()
                .filter(b -> b.getSavings() > 0)
                .forEach(b -> wasteResources.add(new WasteResource(b.getBucketName(), b.getRecommendation(), b.getSavings())));
        rdsReport.getDatabases().stream()
                .filter(db -> db.getSavings() > 0)
                .forEach(db -> wasteResources.add(new WasteResource(db.getDbIdentifier(), db.getRecommendation(), db.getSavings())));
        elbReport.getLoadBalancers().stream()
                .filter(lb -> lb.getSavings() > 0)
                .forEach(lb -> wasteResources.add(new WasteResource(lb.getName(), lb.getRecommendation(), lb.getSavings())));
        asgReport.getGroups().stream()
                .filter(group -> group.getSavings() > 0)
                .forEach(group -> wasteResources.add(new WasteResource(group.getName(), group.getRecommendation(), group.getSavings())));

        List<WasteResource> topWastefulResources = wasteResources.stream()
                .sorted((a, b) -> Double.compare(b.getPotentialSaving(), a.getPotentialSaving()))
                .limit(5)
                .toList();

        /* =========================
           SUMMARY
        ========================= */
        Summary summary = new Summary(currentCost, potentialSavings);

        /* =========================
           COST BREAKDOWN
        ========================= */
        Map<String, Double> costBreakdown = Map.of(
                "ec2", ec2Cost,
                "ebs", ebsCost,
                "s3", s3Cost,
                "rds", rdsCost,
                "elb", elbCost,
                "autoscaling", asgCost
        );

        /* =========================
           TOP EXPENSIVE (CLEAN)
        ========================= */
        List<CostResource> topExpensiveResources = expensiveResources.stream()
                .sorted((a, b) -> Double.compare(b.getMonthlyCost(), a.getMonthlyCost()))
                .limit(5)
                .toList();

        /* =========================
           INSIGHTS (SMART)
        ========================= */
        if (ebsReport.getUnusedVolumes() > 0) {
            insights.add("Unused EBS volumes detected - delete to save cost");
        }

        if (!s3Report.getBuckets().isEmpty()) {
            insights.add("Apply lifecycle policies to optimize S3 storage cost");
        }

        if (ec2Cost > rdsCost && ec2Cost > s3Cost) {
            insights.add("EC2 contributes highest cost - consider rightsizing instances");
        }

        /* =========================
           OPTIONAL SECTIONS
        ========================= */
        Map<String, Object> rdsSection = rdsReport.getDatabases().isEmpty()
                ? Map.of(
                "status", "Not Initialized",
                "reason", "No RDS databases found"
        )
                : Map.of(
                "status", "Active",
                "data", rdsReport
        );

        Map<String, Object> elbSection = elbReport.getLoadBalancers().isEmpty()
                ? Map.of(
                "status", "Not Initialized",
                "reason", "No Load Balancers found"
        )
                : Map.of(
                "status", "Active",
                "data", elbReport
        );

        Map<String, Object> asgSection = asgReport.getGroups().isEmpty()
                ? Map.of(
                "status", "Not Initialized",
                "reason", "No Auto Scaling Groups configured"
        )
                : Map.of(
                "status", "Active",
                "data", asgReport
        );

        /* =========================
           FINAL RESPONSE
        ========================= */
        return new CloudReport(
                summary,
                costBreakdown,
                expensiveResources,
                topWastefulResources,
                ec2Instances,
                ebsReport.getVolumes(),
                s3Report.getBuckets(),
                rdsSection,
                elbSection,
                asgSection,
                insights
        );
    }
}