package com.prashant.costguard.service;

import com.prashant.costguard.model.VpcInfo;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.DescribeAddressesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeNatGatewaysResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointsResponse;
import software.amazon.awssdk.services.ec2.model.NatGateway;
import software.amazon.awssdk.services.ec2.model.VpcEndpoint;

import java.util.ArrayList;
import java.util.List;

@Service
public class VpcService {

    private static final double EIP_MONTHLY_COST = 3.6;
    private static final double NAT_MONTHLY_COST = 32.0;
    private static final double ENDPOINT_MONTHLY_COST = 7.2;

    private final Ec2Client ec2Client;

    public VpcService(Ec2Client ec2Client) {
        this.ec2Client = ec2Client;
    }

    public List<VpcInfo> getVpcOptimizations() {
        List<VpcInfo> results = new ArrayList<>();

        optimizeElasticIps(results);
        optimizeNatGateways(results);
        optimizeVpcEndpoints(results);
        addCrossAzRecommendation(results);

        return results;
    }

    private void optimizeElasticIps(List<VpcInfo> results) {
        DescribeAddressesResponse addressesResponse = ec2Client.describeAddresses();

        for (Address address : addressesResponse.addresses()) {
            if (address.associationId() == null || address.associationId().isBlank()) {
                double currentCost = EIP_MONTHLY_COST;
                double optimizedCost = 0;

                results.add(new VpcInfo(
                        address.allocationId() == null ? address.publicIp() : address.allocationId(),
                        "EIP",
                        currentCost,
                        optimizedCost,
                        currentCost - optimizedCost,
                        "Unused Elastic IP detected",
                        "RELEASE_EIP"
                ));
            }
        }
    }

    private void optimizeNatGateways(List<VpcInfo> results) {
        DescribeNatGatewaysResponse natResponse = ec2Client.describeNatGateways();

        for (NatGateway natGateway : natResponse.natGateways()) {
            String natId = natGateway.natGatewayId();
            double currentCost = NAT_MONTHLY_COST;

            results.add(new VpcInfo(
                    natId,
                    "NAT_GATEWAY",
                    currentCost,
                    0,
                    currentCost,
                    "Idle NAT Gateway detected",
                    "DELETE_NAT"
            ));

            results.add(new VpcInfo(
                    natId,
                    "NAT_GATEWAY",
                    currentCost,
                    currentCost * 0.3,
                    currentCost - (currentCost * 0.3),
                    "Consider replacing NAT usage with VPC Gateway/Interface Endpoints",
                    "RECOMMEND_ONLY"
            ));

            results.add(new VpcInfo(
                    natId,
                    "NAT_GATEWAY",
                    currentCost,
                    currentCost * 0.5,
                    currentCost - (currentCost * 0.5),
                    "Add Gateway Endpoint to reduce NAT data transfer cost",
                    "RECOMMEND_ONLY"
            ));
        }
    }

    private void optimizeVpcEndpoints(List<VpcInfo> results) {
        DescribeVpcEndpointsResponse endpointResponse = ec2Client.describeVpcEndpoints();

        for (VpcEndpoint endpoint : endpointResponse.vpcEndpoints()) {
            double currentCost = ENDPOINT_MONTHLY_COST;

            results.add(new VpcInfo(
                    endpoint.vpcEndpointId(),
                    "VPC_ENDPOINT",
                    currentCost,
                    0,
                    currentCost,
                    "Unused VPC Endpoint detected",
                    "DELETE_ENDPOINT"
            ));
        }
    }

    private void addCrossAzRecommendation(List<VpcInfo> results) {
        results.add(new VpcInfo(
                "cross-az-assessment",
                "VPC_RECOMMENDATION",
                10,
                7,
                3,
                "Cross-AZ traffic detected — align resources in same AZ",
                "RECOMMEND_ONLY"
        ));
    }
}