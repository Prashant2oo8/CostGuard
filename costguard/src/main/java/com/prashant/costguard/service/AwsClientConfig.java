package com.prashant.costguard.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;

@Configuration
public class AwsClientConfig {

    @Bean
    public Ec2Client ec2Client() {
        return Ec2Client.builder()
                .region(Region.AP_SOUTH_1)
                .build();
    }
}