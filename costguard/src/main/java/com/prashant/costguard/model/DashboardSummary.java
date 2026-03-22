package com.prashant.costguard.model;

public class DashboardSummary {

    private int totalResources;
    private int runningEC2;
    private int stoppedEC2;

    private double totalCost;
    private double totalSavings;
    private double optimizedCost;
    private int efficiencyScore;

    private int totalEC2;
    private int totalEBS;
    private int totalS3;
    private int totalRDS;
    private int totalELB;
    private int totalASG;

    private double ec2Cost;
    private double ebsCost;
    private double s3Cost;
    private double rdsCost;
    private double elbCost;
    private double asgCost;

    private int wasteResources;
    private String topService;

    private String topWasteResourceId;
    private double topWasteSaving;

    private double savingsPercentage;
    private String systemHealth;

    public DashboardSummary(
            int totalResources,
            int runningEC2,
            int stoppedEC2,
            double totalCost,
            double totalSavings,
            double optimizedCost,
            int efficiencyScore,

            int totalEC2,
            int totalEBS,
            int totalS3,
            int totalRDS,
            int totalELB,
            int totalASG,

            double ec2Cost,
            double ebsCost,
            double s3Cost,
            double rdsCost,
            double elbCost,
            double asgCost,

            int wasteResources,
            String topService,
            String topWasteResourceId,
            double topWasteSaving,

            double savingsPercentage,
            String systemHealth
    ) {
        this.totalResources = totalResources;
        this.runningEC2 = runningEC2;
        this.stoppedEC2 = stoppedEC2;
        this.totalCost = totalCost;
        this.totalSavings = totalSavings;
        this.optimizedCost = optimizedCost;
        this.efficiencyScore = efficiencyScore;

        this.totalEC2 = totalEC2;
        this.totalEBS = totalEBS;
        this.totalS3 = totalS3;
        this.totalRDS = totalRDS;
        this.totalELB = totalELB;
        this.totalASG = totalASG;

        this.ec2Cost = ec2Cost;
        this.ebsCost = ebsCost;
        this.s3Cost = s3Cost;
        this.rdsCost = rdsCost;
        this.elbCost = elbCost;
        this.asgCost = asgCost;

        this.wasteResources = wasteResources;
        this.topService = topService;
        this.topWasteResourceId = topWasteResourceId;
        this.topWasteSaving = topWasteSaving;

        this.savingsPercentage = savingsPercentage;
        this.systemHealth = systemHealth;
    }


    public int getTotalResources() { return totalResources; }
    public int getRunningEC2() { return runningEC2; }
    public int getStoppedEC2() { return stoppedEC2; }

    public double getTotalCost() { return totalCost; }
    public double getTotalSavings() { return totalSavings; }
    public double getOptimizedCost() { return optimizedCost; }
    public int getEfficiencyScore() { return efficiencyScore; }

    public int getTotalEC2() { return totalEC2; }
    public int getTotalEBS() { return totalEBS; }
    public int getTotalS3() { return totalS3; }
    public int getTotalRDS() { return totalRDS; }
    public int getTotalELB() { return totalELB; }
    public int getTotalASG() { return totalASG; }

    public double getEc2Cost() { return ec2Cost; }
    public double getEbsCost() { return ebsCost; }
    public double getS3Cost() { return s3Cost; }
    public double getRdsCost() { return rdsCost; }
    public double getElbCost() { return elbCost; }
    public double getAsgCost() { return asgCost; }

    public int getWasteResources() { return wasteResources; }
    public String getTopService() { return topService; }

    public String getTopWasteResourceId() { return topWasteResourceId; }
    public double getTopWasteSaving() { return topWasteSaving; }

    public double getSavingsPercentage() { return savingsPercentage; }
    public String getSystemHealth() { return systemHealth; }
}