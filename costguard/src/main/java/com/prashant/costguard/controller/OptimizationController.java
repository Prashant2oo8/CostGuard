package com.prashant.costguard.controller;

import com.prashant.costguard.model.EC2Instance;
import com.prashant.costguard.model.OptimizationRecommendation;
import com.prashant.costguard.service.OptimizationService;
import org.springframework.web.bind.annotation.*;
import com.prashant.costguard.service.EC2Service;

import java.util.List;

@RestController
@RequestMapping("/optimize")
public class OptimizationController {
    private final EC2Service ec2Service;
    private final OptimizationService optimizationService;

    public OptimizationController(EC2Service ec2Service, OptimizationService optimizationService){
        this.ec2Service = ec2Service;
        this.optimizationService = optimizationService;
    }
    @GetMapping
    public List<OptimizationRecommendation> optimize(){
        List<EC2Instance> instances = ec2Service.getAllInstances();

        return optimizationService.analyzeInstance(instances);

    }
}
