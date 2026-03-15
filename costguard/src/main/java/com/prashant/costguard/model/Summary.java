package com.prashant.costguard.model;

public class Summary {

    private double currentMonthlyCost;
    private double potentialSavings;
    private double optimizedMonthlyCost;
    private double savingsPercentage;
    private int efficiencyScore;

    public Summary(double currentMonthlyCost, double potentialSavings) {

        this.currentMonthlyCost = round(currentMonthlyCost);
        this.potentialSavings = round(potentialSavings);

        this.optimizedMonthlyCost = round(currentMonthlyCost - potentialSavings);

        if (currentMonthlyCost == 0) {
            this.savingsPercentage = 0;
            this.efficiencyScore = 100;
        } else {

            double savings = (potentialSavings / currentMonthlyCost) * 100;

            this.savingsPercentage = round(savings);

            this.efficiencyScore = (int) Math.round(100 - savingsPercentage);
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public double getCurrentMonthlyCost() {
        return currentMonthlyCost;
    }

    public double getPotentialSavings() {
        return potentialSavings;
    }

    public double getOptimizedMonthlyCost() {
        return optimizedMonthlyCost;
    }

    public double getSavingsPercentage() {
        return savingsPercentage;
    }

    public int getEfficiencyScore() {
        return efficiencyScore;
    }
}