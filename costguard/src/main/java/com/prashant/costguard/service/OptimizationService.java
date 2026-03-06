package com.prashant.costguard.service;

import com.prashant.costguard.model.OptimizationRecommendation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OptimizationService {
    public List<OptimizationRecommendation> analyzeInstance(List<Double> cpuUsages, List<String> instanceIds){
        List<OptimizationRecommendation> recommendations = new ArrayList<>();
        for (int i = 0; i < cpuUsages.size(); i++){
            double cpu = cpuUsages.get(i);
            String id = instanceIds.get(i);

            if (cpu < 5){
                recommendations.add(new OptimizationRecommendation(id, "Low CPU usage detect, Consider stopping this instance", cpu ));
            }
            else {
                recommendations.add(new OptimizationRecommendation(id,"CPU usage normal", cpu));
            }
        }
        return recommendations;
    }
}
