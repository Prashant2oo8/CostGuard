package com.prashant.costguard.controller;

import com.prashant.costguard.model.CloudReport;
import com.prashant.costguard.service.CloudService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cloud")
public class CloudController {

    private final CloudService cloudService;

    public CloudController(CloudService cloudService) {
        this.cloudService = cloudService;
    }

    @GetMapping("/report")
    public CloudReport getReport() {
        return cloudService.generateReport();
    }
}