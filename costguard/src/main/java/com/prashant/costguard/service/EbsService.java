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

            double monthlyCost = calculateMonthlyCost(size);

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
                    new EbsVolume(id, size, state, monthlyCost, recommendation)
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

    private double calculateMonthlyCost(int size) {

        double pricePerGB = 0.10; // approx gp3 price

        return size * pricePerGB;
    }
}