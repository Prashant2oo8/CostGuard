package com.prashant.costguard.controller;

import com.prashant.costguard.model.ApiResponse;
import com.prashant.costguard.model.CloudReport;
import com.prashant.costguard.service.CloudService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/cloud")
public class CloudController {

    private final CloudService cloudService;

    public CloudController(CloudService cloudService) {
        this.cloudService = cloudService;
    }

    @GetMapping("/report")
    public ApiResponse<CloudReport> getReport() {

        CloudReport report = cloudService.generateReport();

        return new ApiResponse<>(
                "success",
                "Cloud report generated successfully",
                report
        );
    }

}