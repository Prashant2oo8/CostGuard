package com.prashant.costguard.service;

import com.prashant.costguard.model.OptimizationRecommendation;
import com.prashant.costguard.model.EC2Instance;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OptimizationService {

    public List<OptimizationRecommendation> analyzeInstance(List<EC2Instance> instances){

        List<OptimizationRecommendation> recommendations = new ArrayList<>();
        for (EC2Instance instance : instances){
            double cpu = instance.getCpuUtilization();
            String id = instance.getInstanceId();

            String recommendation;

            if (cpu < 5) recommendation = "Low CPU usage detect, Consider stopping this instance";
            else if (cpu < 20) recommendation = "Low CPU usage - Consider downgrading instance type";
            else recommendation = "CPU usage normal";

            recommendations.add(new OptimizationRecommendation(id, recommendation, cpu));

        }
        return recommendations;
    }
}
