package com.prashant.costguard.controller;

import com.prashant.costguard.model.S3Report;
import com.prashant.costguard.service.S3Service;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/s3")
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @GetMapping("/report")
    public S3Report getReport() {

        return s3Service.generateReport();
    }
}