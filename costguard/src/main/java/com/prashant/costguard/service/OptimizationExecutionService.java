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
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.StopDbInstanceRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

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
        log.info("Execute optimization request: type={}, id={}, action={}",
                request.getResourceType(), request.getResourceId(), request.getAction());

        return switch (request.getResourceType().trim().toUpperCase(Locale.ROOT)) {
            case "EC2" -> stopEc2(request);
            case "EBS" -> snapshotEbs(request);
            case "S3" -> transitionS3(request);
            case "RDS" -> stopRds(request);
            case "ELB" -> deleteElb(request);
            case "ASG" -> scaleDownAsg(request);
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported resourceType: " + request.getResourceType()
            );
        };
    }

    private String stopEc2(ActionRequest r) {
        requireAction(r, "STOP");
        StopInstancesRequest req = StopInstancesRequest.builder().instanceIds(r.getResourceId()).build();
        log.info("AWS request StopInstances: {}", req);
        runAws(() -> ec2Client.stopInstances(req), "ec2:StopInstances", r.getResourceId());
        return "EC2 instance stopped successfully";
    }

    private String snapshotEbs(ActionRequest r) {
        requireOneOfActions(r, "SNAPSHOT", "DELETE");
        CreateSnapshotRequest req = CreateSnapshotRequest.builder()
                .volumeId(r.getResourceId())
                .description("CostGuard optimization snapshot")
                .build();
        log.info("AWS request CreateSnapshot: {}", req);
        runAws(() -> ec2Client.createSnapshot(req), "ec2:CreateSnapshot", r.getResourceId());
        return "EBS snapshot created successfully";
    }

    private String transitionS3(ActionRequest r) {
        requireAction(r, "MOVE_TO_GLACIER");
        LifecycleRule rule = LifecycleRule.builder()
                .id("costguard-move-to-glacier")
                .status(ExpirationStatus.ENABLED)
                .filter(LifecycleRuleFilter.builder().prefix("").build())
                .transitions(Transition.builder().days(0).storageClass(TransitionStorageClass.GLACIER).build())
                .build();
        PutBucketLifecycleConfigurationRequest req = PutBucketLifecycleConfigurationRequest.builder()
                .bucket(r.getResourceId())
                .lifecycleConfiguration(BucketLifecycleConfiguration.builder().rules(List.of(rule)).build())
                .build();
        log.info("AWS request PutBucketLifecycleConfiguration: bucket={}", r.getResourceId());
        runAws(() -> s3Client.putBucketLifecycleConfiguration(req), "s3:PutLifecycleConfiguration", r.getResourceId());
        return "S3 lifecycle transition configured successfully";
    }

    private String stopRds(ActionRequest r) {
        requireAction(r, "STOP");
        StopDbInstanceRequest req = StopDbInstanceRequest.builder()
                .dbInstanceIdentifier(r.getResourceId())
                .build();
        log.info("AWS request StopDBInstance: {}", req);
        runAws(() -> rdsClient.stopDBInstance(req), "rds:StopDBInstance", r.getResourceId());
        return "RDS instance stopped successfully";
    }

    private String deleteElb(ActionRequest r) {
        requireAction(r, "DELETE");
        DeleteLoadBalancerRequest req = DeleteLoadBalancerRequest.builder()
                .loadBalancerArn(r.getResourceId())
                .build();
        log.info("AWS request DeleteLoadBalancer: {}", req);
        runAws(() -> elbClient.deleteLoadBalancer(req), "elasticloadbalancing:DeleteLoadBalancer", r.getResourceId());
        return "Load balancer deleted successfully";
    }

    private String scaleDownAsg(ActionRequest r) {
        requireAction(r, "SCALE_DOWN");
        UpdateAutoScalingGroupRequest req = UpdateAutoScalingGroupRequest.builder()
                .autoScalingGroupName(r.getResourceId())
                .desiredCapacity(0)
                .minSize(0)
                .build();
        log.info("AWS request UpdateAutoScalingGroup: {}", req);
        runAws(() -> asgClient.updateAutoScalingGroup(req), "autoscaling:UpdateAutoScalingGroup", r.getResourceId());
        return "Auto Scaling Group scaled down successfully";
    }

    private void runAws(Runnable call, String requiredPermission, String resourceId) {
        try {
            call.run();
            log.info("AWS action success: permission={}, resourceId={}", requiredPermission, resourceId);
        } catch (AwsServiceException e) {
            String code = e.awsErrorDetails() != null
                    ? e.awsErrorDetails().errorCode()
                    : e.getClass().getSimpleName();
            log.error("AWS action failed permission={}, resourceId={}, code={}, msg={}",
                    requiredPermission, resourceId, code, e.getMessage(), e);

            if ("AccessDenied".equalsIgnoreCase(code)
                    || "AccessDeniedException".equalsIgnoreCase(code)
                    || "UnauthorizedOperation".equalsIgnoreCase(code)
                    || "AuthFailure".equalsIgnoreCase(code)
                    || e.statusCode() == 401
                    || e.statusCode() == 403) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Unauthorized for " + requiredPermission + " (" + code + ")"
                );
            }

            if (e.statusCode() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resource not found: " + resourceId + " (" + code + ")"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "AWS action failed: " + e.getMessage()
            );
        } catch (SdkClientException e) {
            log.error("AWS SDK client error resourceId={}, msg={}", resourceId, e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "AWS client error: " + e.getMessage()
            );
        }
    }

    private void validateRequest(ActionRequest req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
    }

    private void requireAction(ActionRequest req, String expected) {
        if (!expected.equalsIgnoreCase(req.getAction())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid action for " + req.getResourceType() + ". Expected: " + expected
            );
        }
    }

    private void requireOneOfActions(ActionRequest req, String a, String b) {
        if (!a.equalsIgnoreCase(req.getAction()) && !b.equalsIgnoreCase(req.getAction())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid action for " + req.getResourceType() + ". Expected: " + a + " or " + b
            );
        }
    }
}