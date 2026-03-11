package com.prashant.costguard.service;

import com.prashant.costguard.model.*;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class RdsService {

    private final RdsClient rdsClient;

    public RdsService() {
        this.rdsClient = RdsClient.create();
    }

    public RdsReport generateReport() {

        DescribeDbInstancesResponse response =
                rdsClient.describeDBInstances();

        List<RdsInstance> databases = new ArrayList<>();

        double totalCost = 0;

        for (DBInstance db : response.dbInstances()) {

            String id = db.dbInstanceIdentifier();
            String engine = db.engine();
            String instanceClass = db.dbInstanceClass();

            double monthlyCost = estimateCost(instanceClass);

            String recommendation;

            if (instanceClass.contains("micro")) {
                recommendation = "Low cost instance";
            } else {
                recommendation = "Consider smaller instance if underutilized";
            }

            totalCost += monthlyCost;

            databases.add(
                    new RdsInstance(
                            id,
                            engine,
                            instanceClass,
                            monthlyCost,
                            recommendation
                    )
            );
        }

        return new RdsReport(
                databases.size(),
                totalCost,
                databases
        );
    }

    private double estimateCost(String instanceClass) {

        if (instanceClass.contains("micro")) {
            return 15;
        } else if (instanceClass.contains("small")) {
            return 30;
        } else {
            return 60;
        }
    }
}