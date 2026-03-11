package com.prashant.costguard.model;

public class WasteResource {

    private String name;
    private String reason;
    private double potentialSaving;

    public WasteResource(String name, String reason, double potentialSaving) {
        this.name = name;
        this.reason = reason;
        this.potentialSaving = potentialSaving;
    }

    public String getName() {
        return name;
    }

    public String getReason() {
        return reason;
    }

    public double getPotentialSaving() {
        return potentialSaving;
    }
}