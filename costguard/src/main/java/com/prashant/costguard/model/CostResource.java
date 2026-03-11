package com.prashant.costguard.model;

public class CostResource {

    private String name;
    private String type;
    private double monthlyCost;

    public CostResource(String name, String type, double monthlyCost) {
        this.name = name;
        this.type = type;
        this.monthlyCost = monthlyCost;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getMonthlyCost() {
        return monthlyCost;
    }
}