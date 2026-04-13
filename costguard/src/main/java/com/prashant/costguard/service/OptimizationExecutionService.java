package com.prashant.costguard.service;

import com.prashant.costguard.model.ActionRequest;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.UpdateAutoScalingGroupRequest;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DeleteVolumeRequest;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.StopDbInstanceRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketLifecycleConfiguration;
import software.amazon.awssdk.services.s3.model.LifecycleRule;
import software.amazon.awssdk.services.s3.model.LifecycleRuleFilter;
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationRequest;
import software.amazon.awssdk.services.s3.model.Transition;
import software.amazon.awssdk.services.s3.model.TransitionStorageClass;

import java.util.List;
import java.util.Locale;

@Service
public class OptimizationExecutionService {

    private final Ec2Client ec2Client;
    private final S3Client s3Client;
    private final RdsClient rdsClient;
    private final ElasticLoadBalancingV2Client elbClient;
    private final AutoScalingClient asgClient;

    public OptimizationExecutionService() {
        this.ec2Client = Ec2Client.create();
        this.s3Client = S3Client.create();
        this.rdsClient = RdsClient.create();
        this.elbClient = ElasticLoadBalancingV2Client.create();
        this.asgClient = AutoScalingClient.create();
    }

    public void execute(ActionRequest request) {
        validateRequest(request);

        String resourceType = request.getResourceType().trim().toUpperCase(Locale.ROOT);

        switch (resourceType) {
            case "EC2" -> handleEc2(request);
            case "EBS" -> handleEbs(request);
            case "S3" -> handleS3(request);
            case "RDS" -> handleRds(request);
            case "ELB" -> handleElb(request);
            case "ASG" -> handleAsg(request);
            default -> throw new IllegalArgumentException("Unsupported resource type: " + request.getResourceType());
        }
    }

    private void handleEc2(ActionRequest request) {
        requireAction(request, "STOP");

        try {
            ec2Client.stopInstances(StopInstancesRequest.builder()
                    .instanceIds(request.getResourceId())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to stop EC2 instance: " + request.getResourceId(), e);
        }
    }

    private void handleEbs(ActionRequest request) {
        requireAction(request, "DELETE");

        try {
            ec2Client.deleteVolume(DeleteVolumeRequest.builder()
                    .volumeId(request.getResourceId())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete EBS volume: " + request.getResourceId(), e);
        }
    }

    private void handleS3(ActionRequest request) {
        requireAction(request, "MOVE_TO_GLACIER");

        try {
            LifecycleRule transitionRule = LifecycleRule.builder()
                    .id("costguard-move-to-glacier")
                    .status("Enabled")
                    .filter(LifecycleRuleFilter.builder().prefix("").build())
                    .transitions(Transition.builder()
                            .days(0)
                            .storageClass(TransitionStorageClass.GLACIER)
                            .build())
                    .build();

            s3Client.putBucketLifecycleConfiguration(PutBucketLifecycleConfigurationRequest.builder()
                    .bucket(request.getResourceId())
                    .lifecycleConfiguration(BucketLifecycleConfiguration.builder()
                            .rules(List.of(transitionRule))
                            .build())
                    .build());

        } catch (Exception e) {
            throw new RuntimeException("Failed to move S3 bucket data to Glacier policy: " + request.getResourceId(), e);
        }
    }

    private void handleRds(ActionRequest request) {
        requireAction(request, "STOP");

        try {
            rdsClient.stopDBInstance(StopDbInstanceRequest.builder()
                    .dbInstanceIdentifier(request.getResourceId())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to stop RDS instance: " + request.getResourceId(), e);
        }
    }

    private void handleElb(ActionRequest request) {
        requireAction(request, "DELETE");

        try {
            elbClient.deleteLoadBalancer(DeleteLoadBalancerRequest.builder()
                    .loadBalancerArn(request.getResourceId())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete ELB: " + request.getResourceId(), e);
        }
    }

    private void handleAsg(ActionRequest request) {
        requireAction(request, "SCALE_DOWN");

        try {
            asgClient.updateAutoScalingGroup(UpdateAutoScalingGroupRequest.builder()
                    .autoScalingGroupName(request.getResourceId())
                    .desiredCapacity(0)
                    .minSize(0)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to scale down ASG: " + request.getResourceId(), e);
        }
    }

    private void validateRequest(ActionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (isBlank(request.getResourceType())) {
            throw new IllegalArgumentException("resourceType is required");
        }
        if (isBlank(request.getResourceId())) {
            throw new IllegalArgumentException("resourceId is required");
        }
        if (isBlank(request.getAction())) {
            throw new IllegalArgumentException("action is required");
        }
    }

    private void requireAction(ActionRequest request, String expectedAction) {
        if (!expectedAction.equalsIgnoreCase(request.getAction())) {
            throw new IllegalArgumentException(
                    "Invalid action for " + request.getResourceType() + ". Expected: " + expectedAction
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}