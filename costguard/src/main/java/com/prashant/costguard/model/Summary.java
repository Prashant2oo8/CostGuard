package com.prashant.costguard.model;

public class Summary {

    private double currentMonthlyCost;
    private double potentialSavings;
    private double optimizedMonthlyCost;
    private double savingsPercentage;

    public Summary(double currentMonthlyCost, double potentialSavings) {

        this.currentMonthlyCost = currentMonthlyCost;
        this.potentialSavings = potentialSavings;
        this.optimizedMonthlyCost = currentMonthlyCost - potentialSavings;

        if (currentMonthlyCost == 0) {
            this.savingsPercentage = 0;
        } else {
            this.savingsPercentage = (potentialSavings / currentMonthlyCost) * 100;
        }
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
}