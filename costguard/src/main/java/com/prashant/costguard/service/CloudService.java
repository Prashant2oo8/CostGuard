package com.prashant.costguard.service;

import com.prashant.costguard.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

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

        List<EC2Instance> ec2Instances = ec2Service.getAllInstances();

        EbsReport ebsReport = ebsService.generateReport();
        S3Report s3Report = s3Service.generateReport();

        RdsReport rdsReport = rdsService.generateReport();
        ElbReport elbReport = elbService.generateReport();
        AsgReport asgReport = asgService.generateReport();

        double ec2Cost = ec2Instances.stream()
                .mapToDouble(EC2Instance::getMonthlyCost)
                .sum();

        double ebsCost = ebsReport.getTotalMonthlyCost();
        double s3Cost = s3Report.getEstimatedMonthlyCost();

        double currentCost = ec2Cost + ebsCost + s3Cost;

        double potentialSavings = 0;

        for (EC2Instance instance : ec2Instances) {

            expensiveResources.add(
                    new CostResource(
                            instance.getInstanceId(),
                            "EC2 Instance",
                            instance.getMonthlyCost()
                    )
            );

            if (instance.getCpuUtilization() < 10) {

                potentialSavings += instance.getMonthlyCost();

                wasteResources.add(
                        new WasteResource(
                                instance.getInstanceId(),
                                "Low CPU Utilization",
                                instance.getMonthlyCost()
                        )
                );

                insights.add(
                        "Stop EC2 instance " + instance.getInstanceId() +
                                " (Low CPU utilization)"
                );
            }
        }

        Summary summary = new Summary(currentCost, potentialSavings);

        Map<String, Double> costBreakdown = Map.of(
                "ec2", ec2Cost,
                "ebs", ebsCost,
                "s3", s3Cost,
                "rds", 0.0,
                "elb", 0.0,
                "autoscaling", 0.0
        );

        expensiveResources.sort(
                (a, b) -> Double.compare(b.getMonthlyCost(), a.getMonthlyCost())
        );

        if (expensiveResources.size() > 5) {
            expensiveResources = expensiveResources.subList(0, 5);
        }

        wasteResources.sort(
                (a, b) -> Double.compare(b.getPotentialSaving(), a.getPotentialSaving())
        );

        if (wasteResources.size() > 5) {
            wasteResources = wasteResources.subList(0, 5);
        }

        if (ebsReport.getUnusedVolumes() > 0) {

            insights.add(
                    "Delete unused EBS volumes to save storage cost"
            );
        }

        for (S3Bucket bucket : s3Report.getBuckets()) {

            insights.add(
                    "Monitor S3 bucket " + bucket.getBucketName() +
                            " for lifecycle optimization"
            );
        }

        Object rdsSection = rdsReport.getDatabases().isEmpty()
                ? Map.of(
                "status", "Not Initialized",
                "reason", "No RDS databases found in AWS account"
        )
                : rdsReport;

        Object elbSection = elbReport.getLoadBalancers().isEmpty()
                ? Map.of(
                "status", "Not Initialized",
                "reason", "No Load Balancers found"
        )
                : elbReport;

        Object asgSection = asgReport.getGroups().isEmpty()
                ? Map.of(
                "status", "Not Initialized",
                "reason", "No Auto Scaling Groups configured"
        )
                : asgReport;

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