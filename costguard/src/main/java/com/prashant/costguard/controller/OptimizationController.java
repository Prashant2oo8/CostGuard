package com.prashant.costguard.controller;

import com.prashant.costguard.model.OptimizationRecommendation;
import com.prashant.costguard.service.OptimizationService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/optimize")
public class OptimizationController {
    private final OptimizationService optimizationService;

    public OptimizationController(OptimizationService optimizationService){
        this.optimizationService = optimizationService;
    }
    @GetMapping
    public List<OptimizationRecommendation> optimize(){
        List<Double> cpuUsage = Arrays.asList(2.3, 15.5, 1.5);
        List<String> instanceIds = Arrays.asList("i-123", "i-456", "i-789");

        return optimizationService.analyzeInstance(cpuUsage, instanceIds);
    }
}
