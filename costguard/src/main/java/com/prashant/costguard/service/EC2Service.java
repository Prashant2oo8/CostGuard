package com.prashant.costguard.service;

import com.prashant.costguard.model.EC2Instance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class EC2Service {
    public List<EC2Instance> getAllInstances(){
        List<EC2Instance> instances = new ArrayList<>();
        instances.add(new EC2Instance("i-1234", "t2.micro", "running", 10.5));
        instances.add(new EC2Instance("i=5678", "t2.small", "stopped", 9.5));
        return instances;
    }


}
