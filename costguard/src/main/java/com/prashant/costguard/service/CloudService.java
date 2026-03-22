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

    public CloudService(
            EC2Service ec2Service,
            EbsService ebsService,
            S3Service s3Service,
            RdsService rdsService,
            ElbService elbService,
            AsgService asgService
    ) {
        this.ec2Service = ec2Service;
        this.ebsService = ebsService;
        this.s3Service = s3Service;
        this.rdsService = rdsService;
        this.elbService = elbService;
        this.asgService = asgService;
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

        double potentialSavings = 0; // (can later integrate OptimizationService)

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
                wasteResources,
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