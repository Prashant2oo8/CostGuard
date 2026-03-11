package com.prashant.costguard.controller;

import com.prashant.costguard.model.ElbReport;
import com.prashant.costguard.service.ElbService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/elb")
public class ElbController {

    private final ElbService elbService;

    public ElbController(ElbService elbService) {
        this.elbService = elbService;
    }

    @GetMapping("/report")
    public ElbReport getReport() {

        return elbService.generateReport();
    }
}