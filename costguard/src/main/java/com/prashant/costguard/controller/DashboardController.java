package com.prashant.costguard.controller;

import com.prashant.costguard.model.*;
import com.prashant.costguard.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final EC2Service ec2Service;
    private final EbsService ebsService;
    private final S3Service s3Service;
    private final RdsService rdsService;
    private final ElbService elbService;
    private final AsgService asgService;
    private final OptimizationService optimizationService;
    private final DashboardService dashboardService;

    public DashboardController(
            EC2Service ec2Service,
            EbsService ebsService,
            S3Service s3Service,
            RdsService rdsService,
            ElbService elbService,
            AsgService asgService,
            OptimizationService optimizationService,
            DashboardService dashboardService
    ) {
        this.ec2Service = ec2Service;
        this.ebsService = ebsService;
        this.s3Service = s3Service;
        this.rdsService = rdsService;
        this.elbService = elbService;
        this.asgService = asgService;
        this.optimizationService = optimizationService;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public Map<String, Object> getDashboard() {

           // FETCH ALL RESOURCES
        List<EC2Instance> ec2List = ec2Service.getAllInstances();
        List<EbsVolume> ebsList = ebsService.generateReport().getVolumes();
        List<S3Bucket> s3List = s3Service.generateReport().getBuckets();
        List<RdsInstance> rdsList = rdsService.generateReport().getDatabases();
        List<LoadBalancerInfo> elbList = elbService.generateReport().getLoadBalancers();
        List<AutoScalingGroupInfo> asgList = asgService.generateReport().getGroups();

        /* =========================
           GENERATE OPTIMIZATION
        ========================= */
        List<OptimizationRecommendation> recommendations =
                optimizationService.analyzeAll(
                        ec2List,
                        ebsList,
                        s3List,
                        rdsList,
                        elbList,
                        asgList
                );
        // GENERATE DASHBOARD

        return dashboardService.generateSummary(
                recommendations,
                ec2List,
                ebsList,
                s3List,
                rdsList,
                elbList,
                asgList
        );
    }
}