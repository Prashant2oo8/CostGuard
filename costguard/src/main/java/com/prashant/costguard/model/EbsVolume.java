package com.prashant.costguard.model;

public class EbsVolume {

    private String volumeId;
    private int size;
    private String state;
    private double monthlyCost;
    private String recommendation;
    private String volumeType;

    public EbsVolume(String volumeId, int size, String state, String volumeType,
                     double monthlyCost, String recommendation) {

        this.volumeId = volumeId;
        this.size = size;
        this.state = state;
        this.volumeType = volumeType;
        this.monthlyCost = monthlyCost;
        this.recommendation = recommendation;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public int getSize() {
        return size;
    }

    public String getState() {
        return state;
    }

    public String getVolumeType() {
        return volumeType;
    }

    public double getMonthlyCost() {
        return monthlyCost;
    }

    public String getRecommendation() {
        return recommendation;
    }
}