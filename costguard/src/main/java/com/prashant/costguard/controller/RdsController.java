package com.prashant.costguard.controller;

import com.prashant.costguard.model.RdsReport;
import com.prashant.costguard.service.RdsService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rds")
public class RdsController {

    private final RdsService rdsService;

    public RdsController(RdsService rdsService) {
        this.rdsService = rdsService;
    }

    @GetMapping("/report")
    public RdsReport getReport() {

        return rdsService.generateReport();
    }
}