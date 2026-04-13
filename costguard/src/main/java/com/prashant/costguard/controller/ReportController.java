package com.prashant.costguard.controller;

import com.prashant.costguard.model.*;
import com.prashant.costguard.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report")

public class ReportController {
    private final EC2Service ec2Service;
    private final ReportService reportService;

    public ReportController(EC2Service ec2Service, ReportService reportService){
        this.ec2Service = ec2Service;
        this.reportService = reportService;
    }
    @CrossOrigin(origins = "*")
    @GetMapping("/api/cloud/report")
    public List<ReportResponse> getReport(){

        List<EC2Instance> instances = ec2Service.getAllInstances();

        return reportService.generateReport(instances);
    }

}