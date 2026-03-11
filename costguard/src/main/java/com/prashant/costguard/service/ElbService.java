package com.prashant.costguard.service;

import com.prashant.costguard.model.*;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class ElbService {

    private final ElasticLoadBalancingV2Client elbClient;

    public ElbService() {
        this.elbClient = ElasticLoadBalancingV2Client.create();
    }

    public ElbReport generateReport() {

        DescribeLoadBalancersResponse response =
                elbClient.describeLoadBalancers();

        List<LoadBalancerInfo> loadBalancers = new ArrayList<>();

        double totalCost = 0;

        for (LoadBalancer lb : response.loadBalancers()) {

            String name = lb.loadBalancerName();
            String type = lb.typeAsString();
            String state = lb.state().codeAsString();

            double monthlyCost = 18;

            String recommendation =
                    "Check if load balancer is required";

            totalCost += monthlyCost;

            loadBalancers.add(
                    new LoadBalancerInfo(
                            name,
                            type,
                            state,
                            monthlyCost,
                            recommendation
                    )
            );
        }

        return new ElbReport(
                loadBalancers.size(),
                totalCost,
                loadBalancers
        );
    }
}