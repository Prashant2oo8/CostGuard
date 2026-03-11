package com.prashant.costguard.service;

import com.prashant.costguard.model.*;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class AsgService {

    private final AutoScalingClient asgClient;

    public AsgService() {
        this.asgClient = AutoScalingClient.create();
    }

    public AsgReport generateReport() {

        DescribeAutoScalingGroupsResponse response =
                asgClient.describeAutoScalingGroups();

        List<AutoScalingGroupInfo> groups = new ArrayList<>();

        double totalCost = 0;

        for (AutoScalingGroup group : response.autoScalingGroups()) {

            String name = group.autoScalingGroupName();
            int min = group.minSize();
            int max = group.maxSize();
            int desired = group.desiredCapacity();

            double monthlyCost = desired * 8;

            String recommendation;

            if (desired > 2) {
                recommendation = "Check scaling policy to avoid unnecessary cost";
            } else {
                recommendation = "Scaling configuration looks normal";
            }

            totalCost += monthlyCost;

            groups.add(
                    new AutoScalingGroupInfo(
                            name,
                            min,
                            max,
                            desired,
                            monthlyCost,
                            recommendation
                    )
            );
        }

        return new AsgReport(
                groups.size(),
                totalCost,
                groups
        );
    }
}