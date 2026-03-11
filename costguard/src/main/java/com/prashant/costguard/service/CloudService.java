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

        List<EC2Instance> ec2Instances = ec2Service.getAllInstances();

        EbsReport ebsReport = ebsService.generateReport();
        S3Report s3Report = s3Service.generateReport();

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
            }
        }

        Summary summary = new Summary(currentCost, potentialSavings);

        Object rdsStatus = Map.of(
                "status", "Not Initialized",
                "reason", "No RDS databases found in AWS account"
        );

        Object elbStatus = Map.of(
                "status", "Not Initialized",
                "reason", "No Load Balancers found"
        );

        Object asgStatus = Map.of(
                "status", "Not Initialized",
                "reason", "No Auto Scaling Groups configured"
        );

        return new CloudReport(
                summary,
                expensiveResources,
                wasteResources,
                ec2Instances,
                ebsReport.getVolumes(),
                s3Report.getBuckets(),
                rdsStatus,
                elbStatus,
                asgStatus
        );
    }
}