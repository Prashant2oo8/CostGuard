package com.prashant.costguard.service;

import com.prashant.costguard.model.EC2Instance;
import com.prashant.costguard.model.ReportResponse;
import com.prashant.costguard.model.VpcInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ReportService {

    private final OptimizationService optimizationService;
    private final VpcService vpcService;

    public ReportService(OptimizationService optimizationService, VpcService vpcService) {
        this.optimizationService = optimizationService;
        this.vpcService = vpcService;
    }

    public List<ReportResponse> generateReport(List<EC2Instance> instances) {

        optimizationService.analyzeAll(
                instances,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        double totalCurrentCost = instances.stream()
                .mapToDouble(EC2Instance::getMonthlyCost)
                .sum();

        double totalOptimizedCost = instances.stream()
                .mapToDouble(EC2Instance::getOptimizedCost)
                .sum();

        List<VpcInfo> vpcInfos = vpcService.getVpcOptimizations();
        totalCurrentCost += vpcInfos.stream().mapToDouble(VpcInfo::getCurrentCost).sum();
        totalOptimizedCost += vpcInfos.stream().mapToDouble(VpcInfo::getOptimizedCost).sum();

        double savings = Math.max(0, totalCurrentCost - totalOptimizedCost);
        double savingsPercentage = totalCurrentCost == 0
                ? 0
                : (savings / totalCurrentCost) * 100;
        int efficiencyScore = (int) Math.max(0, Math.round(100 - savingsPercentage));

        List<ReportResponse> report = new ArrayList<>();

        for (EC2Instance instance : instances) {

            String recommendation = instance.getRecommendation();
            if (instance.getSavings() > 0 && efficiencyScore < 90) {
                recommendation += " | Priority optimization candidate";
            }

            report.add(new ReportResponse(
                    instance.getInstanceId(),
                    instance.getInstanceType(),
                    getRecommendedType(instance.getInstanceType(), recommendation),
                    instance.getState(),
                    instance.getCpuUtilization(),
                    instance.getMonthlyCost(),
                    recommendation,
                    instance.getSavings()
            ));
        }

        return report;
    }

    private String getRecommendedType(String currentType, String recommendation) {
        String lowerRecommendation = recommendation.toLowerCase();

        if (lowerRecommendation.contains("stop")) {
            return "none";
        }

        if (lowerRecommendation.contains("downsize") || lowerRecommendation.contains("downgrade")) {
            if ("t3.small".equals(currentType)) {
                return "t3.micro";
            }
            if ("t3.micro".equals(currentType)) {
                return "t2.micro";
            }
        }

        return currentType;
    }
}