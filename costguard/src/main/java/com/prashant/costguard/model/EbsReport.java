package com.prashant.costguard.model;

import java.util.List;

public class EbsReport {

    private int totalVolumes;
    private int unusedVolumes;
    private double totalMonthlyCost;
    private double potentialSavings;
    private List<EbsVolume> volumes;

    public EbsReport(int totalVolumes,
                     int unusedVolumes,
                     double totalMonthlyCost,
                     double potentialSavings,
                     List<EbsVolume> volumes) {

        this.totalVolumes = totalVolumes;
        this.unusedVolumes = unusedVolumes;
        this.totalMonthlyCost = totalMonthlyCost;
        this.potentialSavings = potentialSavings;
        this.volumes = volumes;
    }

    public int getTotalVolumes() {
        return totalVolumes;
    }

    public int getUnusedVolumes() {
        return unusedVolumes;
    }

    public double getTotalMonthlyCost() {
        return totalMonthlyCost;
    }

    public double getPotentialSavings() {
        return potentialSavings;
    }

    public List<EbsVolume> getVolumes() {
        return volumes;
    }
}