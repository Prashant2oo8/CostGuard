package com.prashant.costguard.service;

import com.prashant.costguard.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class ReportService {

    public List<ReportResponse> generateReport(List<EC2Instance> instances){

        List<ReportResponse> report = new ArrayList<>();

        for(EC2Instance instance : instances){

            double cpu = instance.getCpuUtilization();
            double monthlyCost = instance.getMonthlyCost();

            String recommendation;
            double saving;
            String recommendedType = instance.getInstanceType();

            if(cpu < 5){
                recommendation ="Stop Instance";
                saving = monthlyCost;
                recommendedType = "none";
            } else if (cpu < 20) {
                recommendation = "Downgrade instance";
                if(instance.getInstanceType().equals("t3.small")){
                    recommendedType = "t3.micro";
                } else if (instance.getInstanceType().equals("t3.micro")) {
                    recommendedType = "t2.micro";
                }
                saving = monthlyCost * 0.5;
            }else{
                recommendation = "Healthy";
                saving = 0;
            }

            report.add(new ReportResponse(instance.getInstanceId(), instance.getInstanceType(),recommendedType, instance.getState(), cpu, monthlyCost, recommendation, saving));

        }

        return report;
    }
}
