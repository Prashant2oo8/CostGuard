package com.prashant.costguard.controller;

import com.prashant.costguard.model.*;
import com.prashant.costguard.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/optimize")
public class OptimizationController {

    private final EC2Service ec2Service;
    private final EbsService ebsService;
    private final S3Service s3Service;
    private final RdsService rdsService;
    private final ElbService elbService;
    private final AsgService asgService;
    private final OptimizationService optimizationService;

    public OptimizationController(
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

    @GetMapping
    public List<OptimizationRecommendation> optimize() {

        try {

            /* =========================
               FETCH ALL RESOURCES
            ========================= */
            List<EC2Instance> ec2List = ec2Service.getAllInstances();
            List<EbsVolume> ebsList = ebsService.generateReport().getVolumes();
            List<S3Bucket> s3List = s3Service.generateReport().getBuckets();
            List<RdsInstance> rdsList = rdsService.generateReport().getDatabases();
            List<LoadBalancerInfo> elbList = elbService.generateReport().getLoadBalancers();
            List<AutoScalingGroupInfo> asgList = asgService.generateReport().getGroups();

            /* =========================
               UNIFIED OPTIMIZATION
               (FIXED ORDER ✅)
            ========================= */
            return optimizationService.analyzeAll(
                    ec2List,
                    ebsList,
                    s3List,
                    rdsList,
                    elbList,
                    asgList
            );

        } catch (Exception e) {
            e.printStackTrace();

              // SAFE FALLBACK RESPONSE
            return List.of(
                    new OptimizationRecommendation(
                            "SYSTEM",
                            "Failed to fetch optimization data",
                            0,
                            0,
                            0
                    )
            );
        }
    }
}