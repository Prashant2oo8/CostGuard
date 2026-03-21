package com.prashant.costguard.service;

import com.prashant.costguard.model.EbsReport;
import com.prashant.costguard.model.EbsVolume;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.*;

@Service
public class EbsService {

    private final Ec2Client ec2Client;

    // Cache for instance states to reduce API calls
    private final Map<String, String> instanceStateCache = new HashMap<>();

    public EbsService() {
        this.ec2Client = Ec2Client.create();
    }

    public EbsReport generateReport() {

        DescribeVolumesResponse response = ec2Client.describeVolumes();

        List<EbsVolume> volumes = new ArrayList<>();

        int unusedVolumes = 0;
        double totalCost = 0;
        double potentialSavings = 0;

        for (Volume volume : response.volumes()) {

            String id = volume.volumeId();
            int size = volume.size();
            String state = volume.stateAsString();
            String volumeType = volume.volumeTypeAsString();

            double monthlyCost = calculateMonthlyCost(size, volumeType);

            String recommendation = "No recommendation available";

            if (state.equalsIgnoreCase("available")) {

                unusedVolumes++;

                boolean hasSnapshot = checkSnapshotExists(id);

                if (hasSnapshot) {
                    recommendation = "Unused volume - safe to delete (snapshot available)";
                    potentialSavings += monthlyCost;
                } else {
                    recommendation = "Unused volume - create snapshot before deletion";
                }

            } else if (state.equalsIgnoreCase("in-use")) {

                String instanceId = null;

                if (!volume.attachments().isEmpty()) {
                    for (VolumeAttachment attachment : volume.attachments()) {
                        if (attachment.instanceId() != null) {
                            instanceId = attachment.instanceId();
                            break;
                        }
                    }
                }

                if (instanceId != null) {

                    String instanceState = getCachedInstanceState(instanceId);

                    if (instanceState.equalsIgnoreCase("stopped")) {

                        recommendation = "Attached to stopped instance - if used regularly no action needed, otherwise consider snapshot and delete";

                    }else if(instanceState.equalsIgnoreCase("running")) {

                        if (size > 100) {
                            recommendation = "Large volume - consider downsizing";

                        } else if (size < 5) {
                            recommendation = "Very small volume - verify if required";

                        } else {
                            recommendation = "Volume actively used by running instance";
                        }

                    }else {
                        recommendation = "Unknown instance state - review manually";
                    }

                } else {
                    recommendation = "Volume attached but instance not found - review";
                }

            } else {
                recommendation = "Unknown volume state - manual review required";
            }

            if (monthlyCost > 50) {
                recommendation += " | High cost volume - prioritize optimization";
            }

            totalCost += monthlyCost;

            volumes.add(
                    new EbsVolume(id, size, state, volumeType, monthlyCost, recommendation)
            );
        }

        return new EbsReport(
                volumes.size(),
                unusedVolumes,
                totalCost,
                potentialSavings,
                volumes
        );
    }

    // Cached instance state lookup
    private String getCachedInstanceState(String instanceId) {

        if (instanceStateCache.containsKey(instanceId)) {
            return instanceStateCache.get(instanceId);
        }

        String state = getInstanceState(instanceId);
        instanceStateCache.put(instanceId, state);

        return state;
    }

    // Snapshot detection
    private boolean checkSnapshotExists(String volumeId) {

        try {

            DescribeSnapshotsRequest request = DescribeSnapshotsRequest.builder()
                    .filters(
                            Filter.builder()
                                    .name("volume-id")
                                    .values(volumeId)
                                    .build()
                    )
                    .build();

            DescribeSnapshotsResponse response = ec2Client.describeSnapshots(request);

            return !response.snapshots().isEmpty();

        } catch (Exception e) {
            System.err.println("Snapshot check failed for volume: " + volumeId);
            return false;
        }
    }

    // Instance state check
    private String getInstanceState(String instanceId) {

        try {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();

            DescribeInstancesResponse response = ec2Client.describeInstances(request);

            if (response.reservations().isEmpty() ||
                    response.reservations().get(0).instances().isEmpty()) {
                return "unknown";
            }

            return response.reservations().get(0)
                    .instances().get(0)
                    .state().nameAsString();

        } catch (Exception e) {
            return "unknown";
        }
    }

    // Cost calculation
    private double calculateMonthlyCost(int size, String volumeType){

        double pricePerGB;

        switch (volumeType) {

            case "gp3":
                pricePerGB = 0.08;
                break;

            case "gp2":
                pricePerGB = 0.10;
                break;

            case "io1":
            case "io2":
                pricePerGB = 0.125;
                break;

            case "st1":
                pricePerGB = 0.045;
                break;

            case "sc1":
                pricePerGB = 0.025;
                break;

            default:
                pricePerGB = 0.10;
        }

        return size * pricePerGB;
    }
}