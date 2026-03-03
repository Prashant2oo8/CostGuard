package com.prashant.costguard.controller;

import com.prashant.costguard.model.EC2Instance;
import com.prashant.costguard.service.EC2Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import  org.springframework.http.RequestEntity;

@RestController
@RequestMapping("/ec2")

public class EC2Controller {
    //dependency
    private final EC2Service ec2Service;

    public EC2Controller(EC2Service ec2Service){
        this.ec2Service = ec2Service;

    }
    @GetMapping("/instances")
    public ResponseEntity<List<EC2Instance>> getInstances(){
        List<EC2Instance> instances = ec2Service.getAllInstances();
        if (instances.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(instances);
    }

}
