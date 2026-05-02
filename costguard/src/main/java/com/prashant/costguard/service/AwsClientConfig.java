package com.prashant.costguard.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsClientConfig {

    private static final Region DEFAULT_REGION = Region.AP_SOUTH_1;

    @Bean
    public Ec2Client ec2Client() {
        return Ec2Client.builder().region(DEFAULT_REGION).build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder().region(DEFAULT_REGION).build();
    }

    @Bean
    public RdsClient rdsClient() {
        return RdsClient.builder().region(DEFAULT_REGION).build();
    }

    @Bean
    public ElasticLoadBalancingV2Client elasticLoadBalancingV2Client() {
        return ElasticLoadBalancingV2Client.builder().region(DEFAULT_REGION).build();
    }

    @Bean
    public AutoScalingClient autoScalingClient() {
        return AutoScalingClient.builder().region(DEFAULT_REGION).build();
    }
}