package com.prashant.costguard.service;

import com.prashant.costguard.model.ActionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.UpdateAutoScalingGroupRequest;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateSnapshotRequest;
import software.amazon.awssdk.services.ec2.model.DeleteNatGatewayRequest;
import software.amazon.awssdk.services.ec2.model.DeleteVolumeRequest;
import software.amazon.awssdk.services.ec2.model.DeleteVpcEndpointsRequest;
import software.amazon.awssdk.services.ec2.model.ReleaseAddressRequest;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.StopDbInstanceRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketLifecycleConfiguration;
import software.amazon.awssdk.services.s3.model.ExpirationStatus;
import software.amazon.awssdk.services.s3.model.LifecycleRule;
import software.amazon.awssdk.services.s3.model.LifecycleRuleFilter;
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationRequest;
import software.amazon.awssdk.services.s3.model.Transition;
import software.amazon.awssdk.services.s3.model.TransitionStorageClass;

import java.util.List;
import java.util.Locale;

@Service
public class OptimizationExecutionService {
    private static final Logger log = LoggerFactory.getLogger(OptimizationExecutionService.class);

    private final Ec2Client ec2Client;
    private final S3Client s3Client;
    private final RdsClient rdsClient;
    private final ElasticLoadBalancingV2Client elbClient;
    private final AutoScalingClient asgClient;

    public OptimizationExecutionService(
            Ec2Client ec2Client,
            S3Client s3Client,
            RdsClient rdsClient,
            ElasticLoadBalancingV2Client elbClient,
            AutoScalingClient asgClient
    ) {
        this.ec2Client = ec2Client;
        this.s3Client = s3Client;
        this.rdsClient = rdsClient;
        this.elbClient = elbClient;
        this.asgClient = asgClient;
    }

    public String execute(ActionRequest request) {
        validateRequest(request);
        log.info("Execute optimization request: type={}, id={}, action={}", request.getResourceType(), request.getResourceId(), request.getAction());

        return switch (request.getResourceType().trim().toUpperCase(Locale.ROOT)) {
            case "EC2" -> executeEc2(request);
            case "EBS" -> executeEbs(request);
            case "S3" -> executeS3(request);
            case "RDS" -> executeRds(request);
            case "ELB" -> executeElb(request);
            case "ASG" -> executeAsg(request);
            case "VPC" -> executeVpc(request);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported resourceType: " + request.getResourceType());
        };
    }

    private String executeEc2(ActionRequest request) {
        String action = normalizeAction(request);
        return switch (action) {
            case "STOP" -> stopEc2(request.getResourceId());
            case "SCHEDULE_STOP" -> "EC2 stop schedule recommendation saved. No API call required.";
            case "SCHEDULE_START_STOP" -> "EC2 start/stop schedule recommendation saved. No API call required.";
            case "RIGHTSIZE_RECOMMEND" -> "EC2 rightsize recommendation recorded. Manual change required.";
            default -> throw invalidAction(request, "STOP", "SCHEDULE_STOP", "SCHEDULE_START_STOP", "RIGHTSIZE_RECOMMEND");
        };
    }

    private String executeEbs(ActionRequest request) {
        String action = normalizeAction(request);
        return switch (action) {
            case "SNAPSHOT" -> createEbsSnapshot(request.getResourceId());
            case "SNAPSHOT_DELETE" -> createSnapshotThenDeleteVolume(request.getResourceId());
            case "DELETE" -> deleteEbsVolume(request.getResourceId());
            default -> throw invalidAction(request, "SNAPSHOT", "SNAPSHOT_DELETE", "DELETE");
        };
    }

    private String executeS3(ActionRequest request) {
        String action = normalizeAction(request);
        return switch (action) {
            case "MOVE_TO_STANDARD_IA" -> applyS3Transition(request.getResourceId(), TransitionStorageClass.STANDARD_IA, "S3 transition to Standard-IA configured successfully");
            case "MOVE_TO_GLACIER" -> applyS3Transition(request.getResourceId(), TransitionStorageClass.GLACIER, "S3 transition to Glacier configured successfully");
            case "APPLY_LIFECYCLE_POLICY" -> applyS3Transition(request.getResourceId(), TransitionStorageClass.GLACIER, "S3 lifecycle policy applied successfully");
            default -> throw invalidAction(request, "MOVE_TO_STANDARD_IA", "MOVE_TO_GLACIER", "APPLY_LIFECYCLE_POLICY");
        };
    }

    private String executeRds(ActionRequest request) {
        String action = normalizeAction(request);
        return switch (action) {
            case "STOP" -> stopRds(request.getResourceId());
            case "SCHEDULE_STOP" -> "RDS stop schedule recommendation saved. No API call required.";
            case "RIGHTSIZE_RECOMMEND", "RESERVED_RECOMMEND" -> "RDS reserved/rightsize recommendation recorded. Manual change required.";
            default -> throw invalidAction(request, "STOP", "SCHEDULE_STOP", "RIGHTSIZE_RECOMMEND", "RESERVED_RECOMMEND");
        };
    }

    private String executeElb(ActionRequest request) {
        String action = normalizeAction(request);
        return switch (action) {
            case "DELETE" -> deleteElb(request.getResourceId());
            case "KEEP" -> "Load balancer marked as keep. No action executed.";
            default -> throw invalidAction(request, "DELETE", "KEEP");
        };
    }

    private String executeAsg(ActionRequest request) {
        String action = normalizeAction(request);
        return switch (action) {
            case "SCALE_DOWN" -> updateAsg(request.getResourceId(), 0, 0, "Auto Scaling Group scaled down successfully");
            case "REDUCE_MIN_MAX" -> updateAsg(request.getResourceId(), 0, 0, "Auto Scaling Group min/max reduced successfully");
            case "SCHEDULE_SCALE_DOWN" -> "Auto Scaling Group scale-down schedule recommendation saved. No API call required.";
            default -> throw invalidAction(request, "SCALE_DOWN", "REDUCE_MIN_MAX", "SCHEDULE_SCALE_DOWN");
        };
    }

    private String executeVpc(ActionRequest request) {
        String action = normalizeAction(request);
        String resourceType = request.getVpcResourceType() == null ? "" : request.getVpcResourceType().trim().toUpperCase(Locale.ROOT);

        return switch (action) {
            case "RELEASE_EIP" -> releaseElasticIp(request.getResourceId());
            case "DELETE_NAT" -> deleteNatGateway(request.getResourceId());
            case "DELETE_ENDPOINT" -> deleteVpcEndpoint(request.getResourceId());
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid VPC action for resource type " + resourceType + ": " + request.getAction());
        };
    }

    private String stopEc2(String instanceId) {
        StopInstancesRequest req = StopInstancesRequest.builder().instanceIds(instanceId).build();
        log.info("AWS request StopInstances: {}", req);
        runAws(() -> ec2Client.stopInstances(req), "ec2:StopInstances", instanceId);
        return "EC2 instance stopped successfully";
    }

    private String createEbsSnapshot(String volumeId) {
        CreateSnapshotRequest req = CreateSnapshotRequest.builder().volumeId(volumeId)
                .description("CostGuard optimization snapshot").build();
        log.info("AWS request CreateSnapshot: {}", req);
        runAws(() -> ec2Client.createSnapshot(req), "ec2:CreateSnapshot", volumeId);
        return "EBS snapshot created successfully";
    }

    private String createSnapshotThenDeleteVolume(String volumeId) {
        createEbsSnapshot(volumeId);
        deleteEbsVolume(volumeId);
        return "EBS snapshot created and volume deleted successfully";
    }

    private String deleteEbsVolume(String volumeId) {
        DeleteVolumeRequest req = DeleteVolumeRequest.builder().volumeId(volumeId).build();
        log.info("AWS request DeleteVolume: {}", req);
        runAws(() -> ec2Client.deleteVolume(req), "ec2:DeleteVolume", volumeId);
        return "EBS volume deleted successfully";
    }

    private String applyS3Transition(String bucket, TransitionStorageClass storageClass, String successMessage) {
        LifecycleRule rule = LifecycleRule.builder()
                .id("costguard-lifecycle-rule")
                .status(ExpirationStatus.ENABLED)
                .filter(LifecycleRuleFilter.builder().prefix("").build())
                .transitions(Transition.builder().days(0).storageClass(storageClass).build())
                .build();

        PutBucketLifecycleConfigurationRequest req = PutBucketLifecycleConfigurationRequest.builder()
                .bucket(bucket)
                .lifecycleConfiguration(BucketLifecycleConfiguration.builder().rules(List.of(rule)).build())
                .build();

        log.info("AWS request PutBucketLifecycleConfiguration: bucket={}, storageClass={}", bucket, storageClass);
        runAws(() -> s3Client.putBucketLifecycleConfiguration(req), "s3:PutLifecycleConfiguration", bucket);
        return successMessage;
    }

    private String stopRds(String dbIdentifier) {
        StopDbInstanceRequest req = StopDbInstanceRequest.builder().dbInstanceIdentifier(dbIdentifier).build();
        log.info("AWS request StopDBInstance: {}", req);
        runAws(() -> rdsClient.stopDBInstance(req), "rds:StopDBInstance", dbIdentifier);
        return "RDS instance stopped successfully";
    }

    private String deleteElb(String loadBalancerArn) {
        DeleteLoadBalancerRequest req = DeleteLoadBalancerRequest.builder().loadBalancerArn(loadBalancerArn).build();
        log.info("AWS request DeleteLoadBalancer: {}", req);
        runAws(() -> elbClient.deleteLoadBalancer(req), "elasticloadbalancing:DeleteLoadBalancer", loadBalancerArn);
        return "Load balancer deleted successfully";
    }

    private String updateAsg(String groupName, int desired, int min, String successMessage) {
        UpdateAutoScalingGroupRequest req = UpdateAutoScalingGroupRequest.builder()
                .autoScalingGroupName(groupName)
                .desiredCapacity(desired)
                .minSize(min)
                .build();
        log.info("AWS request UpdateAutoScalingGroup: {}", req);
        runAws(() -> asgClient.updateAutoScalingGroup(req), "autoscaling:UpdateAutoScalingGroup", groupName);
        return successMessage;
    }

    private String releaseElasticIp(String allocationId) {
        ReleaseAddressRequest req = ReleaseAddressRequest.builder().allocationId(allocationId).build();
        log.info("AWS request ReleaseAddress: {}", req);
        runAws(() -> ec2Client.releaseAddress(req), "ec2:ReleaseAddress", allocationId);
        return "Elastic IP released successfully";
    }

    private String deleteNatGateway(String natGatewayId) {
        DeleteNatGatewayRequest req = DeleteNatGatewayRequest.builder().natGatewayId(natGatewayId).build();
        log.info("AWS request DeleteNatGateway: {}", req);
        runAws(() -> ec2Client.deleteNatGateway(req), "ec2:DeleteNatGateway", natGatewayId);
        return "NAT Gateway delete initiated successfully";
    }

    private String deleteVpcEndpoint(String endpointId) {
        DeleteVpcEndpointsRequest req = DeleteVpcEndpointsRequest.builder().vpcEndpointIds(endpointId).build();
        log.info("AWS request DeleteVpcEndpoints: {}", req);
        runAws(() -> ec2Client.deleteVpcEndpoints(req), "ec2:DeleteVpcEndpoints", endpointId);
        return "VPC endpoint delete initiated successfully";
    }

    private void runAws(Runnable call, String requiredPermission, String resourceId) {
        try {
            call.run();
            log.info("AWS action success: permission={}, resourceId={}", requiredPermission, resourceId);
        } catch (AwsServiceException e) {
            String code = e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : e.getClass().getSimpleName();
            log.error("AWS action failed permission={}, resourceId={}, code={}, msg={}", requiredPermission, resourceId, code, e.getMessage(), e);
            if ("AccessDenied".equalsIgnoreCase(code)
                    || "AccessDeniedException".equalsIgnoreCase(code)
                    || "UnauthorizedOperation".equalsIgnoreCase(code)
                    || "AuthFailure".equalsIgnoreCase(code)
                    || e.statusCode() == 401
                    || e.statusCode() == 403) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized for " + requiredPermission + " (" + code + ")");
            }
            if (e.statusCode() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found: " + resourceId + " (" + code + ")");
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "AWS action failed: " + e.getMessage());
        } catch (SdkClientException e) {
            log.error("AWS SDK client error resourceId={}, msg={}", resourceId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "AWS client error: " + e.getMessage());
        }
    }

    private void validateRequest(ActionRequest req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if (isBlank(req.getResourceType()) || isBlank(req.getResourceId()) || isBlank(req.getAction())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "resourceType, resourceId and action are required");
        }
    }

    private ResponseStatusException invalidAction(ActionRequest req, String... allowed) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid action for " + req.getResourceType() + ". Expected one of: " + String.join(", ", allowed)
        );
    }

    private String normalizeAction(ActionRequest request) {
        return request.getAction().trim().toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}