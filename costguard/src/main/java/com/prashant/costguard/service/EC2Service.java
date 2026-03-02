package com.prashant.costguard.service;

import com.prashant.costguard.model.EC2Instance;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.util.List;
import java.util.ArrayList;

@Service
public class EC2Service {
    private final Ec2Client ec2Client;

    public EC2Service(){
        this.ec2Client = Ec2Client.create();
    }

    public List<EC2Instance> getAllInstances(){
        DescribeInstancesResponse response = ec2Client.describeInstances();
        List<EC2Instance> instances = new ArrayList<>();
        for (var reservation : response.reservations()){
            for (Instance instance: reservation.instances()){
                instances.add(
                        new EC2Instance(
                                instance.instanceId(), instance.instanceTypeAsString(), instance.state().nameAsString(), calculateMonthlyCost(instance.instanceTypeAsString()))
                );
            }
        }

        return instances;
    }
    private double calculateMonthlyCost (String instanceType){
        double hourlyRate = switch (instanceType){
            case "t3.micro" -> 0.0104;
            case "t2.micro" -> 0.0116;
            case "t3.small" -> 0.0208;
            default -> 0.015;
        };
        return hourlyRate*730;
    }


}
