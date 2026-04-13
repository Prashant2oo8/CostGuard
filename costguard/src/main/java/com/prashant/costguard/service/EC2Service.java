package com.prashant.costguard.service;

import com.prashant.costguard.model.EC2Instance;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.util.List;
import java.util.ArrayList;
import java.time.Instant;

@Service
public class EC2Service {
    private final Ec2Client ec2Client;
    private final CloudWatchClient cloudWatchClient;


    public EC2Service(){
        this.ec2Client = Ec2Client.create();
        this.cloudWatchClient = CloudWatchClient.create();
    }

    public List<EC2Instance> getAllInstances(){
        DescribeInstancesResponse response = ec2Client.describeInstances();
        List<EC2Instance> instances = new ArrayList<>();
        for (var reservation : response.reservations()){
            for (Instance instance: reservation.instances()){

                double cpu = getCpuUtilization(instance.instanceId());

                double cost = calculateMonthlyCost(instance.instanceTypeAsString());

                String recommendation = getRecommendation(
                        cpu,
                        instance.state().nameAsString(),
                        cost
                );

                instances.add(new EC2Instance(
                        instance.instanceId(),
                        instance.instanceTypeAsString(),
                        instance.state().nameAsString(),
                        cost,
                        cpu,
                        recommendation
                ));
            }
        }

        return instances;
    }
    private double calculateMonthlyCost (String instanceType){
        double hourlyRate = switch (instanceType){
            case "t3.micro" -> 0.0104;
            case "t2.micro" -> 0.0116;
            case "t3.small" -> 0.0208;
            default -> 0.02;
        };
        return hourlyRate*730;
    }

    private double getCpuUtilization(String instanceId){
        Instant endTime = Instant.now();
        Instant startTime = endTime.minusSeconds(86400);
        GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder().namespace("AWS/EC2").metricName("CPUUtilization").dimensions(Dimension.builder().name("InstanceId").value(instanceId).build()).startTime(startTime).endTime(endTime).period(3600).statistics(Statistic.AVERAGE).build();

        GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);

        if (response.datapoints().isEmpty()){
            return 0;
        }
        return response.datapoints().stream().mapToDouble(Datapoint::average).average().orElse(0.0);

    }

    private String getRecommendation(double cpu, String state, double cost){

        String recommendation;

        // CASE 1: No CPU data
        if (cpu == 0) {
            if (state.equalsIgnoreCase("stopped")) {
                return "Instance is stopped - no CPU data available";
            }
            return "No CPU data available - monitor instance before taking action";
        }

        // CASE 2: Stopped instance
        if (state.equalsIgnoreCase("stopped")) {
            recommendation = "Instance is stopped - if part of scheduled usage no action needed, otherwise consider termination";
        }

        // CASE 3: Very low CPU
        else if (cpu < 5) {
            recommendation = "Very low CPU utilization - consider stopping or terminating instance";
        }

        // CASE 4: Low CPU
        else if (cpu < 20){
            recommendation = "Low CPU utilization - consider downsizing instance";
        }

        // CASE 5: High CPU
        else if (cpu > 80){
            recommendation = "High utilization - consider scaling or upgrading instance";
        }

        // CASE 6: Normal
        else{
            recommendation = "Instance utilization is optimal";
        }

        // COST PRIORITY (NEW)
        if (cost > 50) {
            recommendation += " | High cost instance - prioritize optimization";
        }

        return recommendation;
    }

}