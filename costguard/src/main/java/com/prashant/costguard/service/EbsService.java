package com.prashant.costguard.service;

import com.prashant.costguard.model.EbsReport;
import com.prashant.costguard.model.EbsVolume;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class EbsService {

    private final Ec2Client ec2Client;

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

            String recommendation;

            if (state.equalsIgnoreCase("available")) {

                recommendation = "Delete unused volume";
                unusedVolumes++;
                potentialSavings += monthlyCost;

            } else {

                recommendation = "Volume in use";
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