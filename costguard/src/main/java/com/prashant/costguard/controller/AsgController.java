package com.prashant.costguard.controller;

import com.prashant.costguard.model.AsgReport;
import com.prashant.costguard.service.AsgService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/asg")
public class AsgController {

    private final AsgService asgService;

    public AsgController(AsgService asgService) {
        this.asgService = asgService;
    }

    @GetMapping("/report")
    public AsgReport getReport() {

        return asgService.generateReport();
    }
}