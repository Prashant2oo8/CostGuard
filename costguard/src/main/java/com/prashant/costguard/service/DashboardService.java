package com.prashant.costguard.service;

import com.prashant.costguard.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    public DashboardSummary generateSrummary(List<EC2Instance> instances){

        int total = instances.size();
        int running = 0;
        int stopped = 0;
        double totalCost = 0;
        double potentialSavings = 0;

        for(EC2Instance instance: instances){

            if(instance.getState().equalsIgnoreCase("running")){
                running++;
            }else {
                stopped++;
            }
            totalCost += instance.getMonthlyCost();

            if(instance.getCpuUtilization() < 5){
                potentialSavings += instance.getMonthlyCost();
            }
        }
        return new DashboardSummary(total, running, stopped, totalCost, potentialSavings);
    }
}
