package com.prashant.costguard.model;

public class EbsVolume {

    private String volumeId;
    private int size;
    private String state;
    private double estimatedMonthlyCost;
    private String recommendation;

    public EbsVolume(String volumeId, int size, String state, double estimatedMonthlyCost, String recommendation){

        this.volumeId = volumeId;
        this.size = size;
        this.state = state;
        this.estimatedMonthlyCost = estimatedMonthlyCost;
        this.recommendation = recommendation;

    }

    public String getVolumeId(){
        return volumeId;
    }

    public int getSize(){
        return size;
    }

    public String getState(){
        return state;
    }

    public double getEstimatedMonthlyCost(){
        return estimatedMonthlyCost;
    }

    public String getRecommendation(){
        return recommendation;
    }

}
