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

            if(cpu < 5){
                recommendation ="Stop Instance";
                saving = monthlyCost;
            } else if (cpu < 20) {
                recommendation = "Downgrade instance";
                saving = monthlyCost * 0.5;
            }else{
                recommendation = "Healthy";
                saving = 0;
            }

            report.add(new ReportResponse(instance.getInstanceId(), instance.getInstanceType(), instance.getState(), cpu, monthlyCost, recommendation, saving));

        }

        return report;
    }
}
