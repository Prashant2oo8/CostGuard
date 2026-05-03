package com.prashant.costguard.controller;

import com.prashant.costguard.model.ActionRequest;
import com.prashant.costguard.model.ApiResponse;
import com.prashant.costguard.model.CloudReport;
import com.prashant.costguard.service.CloudService;
import com.prashant.costguard.service.OptimizationExecutionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@CrossOrigin(
        origins = {
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        },
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
@RestController
@RequestMapping({"/cloud", "/api/optimize"})
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
        return new ApiResponse<>("success", "Cloud report generated successfully", report);
    }

    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeOptimization(@Valid @RequestBody ActionRequest request) {
        String message = optimizationExecutionService.execute(request);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("resourceId", request.getResourceId());
        response.put("action", request.getAction());
        response.put("message", message);
        return ResponseEntity.ok(response);
    }
}