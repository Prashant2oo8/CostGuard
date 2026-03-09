package com.prashant.costguard.controller;

import com.prashant.costguard.model.*;
import com.prashant.costguard.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardContoller {

    private final EC2Service ec2Service;
    private final DashboardService dashboardService;

    public DashboardContoller(EC2Service ec2Service, DashboardService dashboardService){
        this.ec2Service = ec2Service;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardSummary getDashboard(){

        List<EC2Instance> instances = ec2Service.getAllInstances();

        return dashboardService.generateSrummary(instances);
    }
}
