package com.prashant.costguard.controller;

import com.prashant.costguard.model.EbsReport;
import com.prashant.costguard.service.EbsService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ebs")
public class EbsController {

    private final EbsService ebsService;

    public EbsController(EbsService ebsService) {
        this.ebsService = ebsService;
    }

    @GetMapping("/report")
    public EbsReport getReport() {

        return ebsService.generateReport();
    }
}