package com.prashant.costguard.controller;

import com.prashant.costguard.model.ApiResponse;
import com.prashant.costguard.model.ActionRequest;
import com.prashant.costguard.model.CloudReport;
import com.prashant.costguard.service.CloudService;
import com.prashant.costguard.service.OptimizationExecutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/cloud")
public class CloudController {

    private final CloudService cloudService;
    private final OptimizationExecutionService optimizationExecutionService;

    public CloudController(CloudService cloudService, OptimizationExecutionService optimizationExecutionService) {
        this.cloudService = cloudService;
        this.optimizationExecutionService = optimizationExecutionService;
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

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<Object>> executeOptimization(@RequestBody ActionRequest request) {
        try {
            optimizationExecutionService.execute(request);
            return ResponseEntity.ok(new ApiResponse<>(
                    "success",
                    "Action executed successfully",
                    null
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>("error", ex.getMessage(), null)
            );
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("error", "Execution failed: " + ex.getMessage(), null)
            );
        }
    }

}