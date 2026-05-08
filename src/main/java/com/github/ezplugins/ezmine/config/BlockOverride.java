package com.github.ezplugins.ezmine.config;

public class BlockOverride {

    private final Double dropMultiplier;
    private final Boolean autoSmelt;
    private final Boolean fortuneEnabled;
    private final Double experienceMultiplier;

    public BlockOverride(Double dropMultiplier,
                         Boolean autoSmelt,
                         Boolean fortuneEnabled,
                         Double experienceMultiplier) {
        this.dropMultiplier = dropMultiplier;
        this.autoSmelt = autoSmelt;
        this.fortuneEnabled = fortuneEnabled;
        this.experienceMultiplier = experienceMultiplier;
    }

    public Double dropMultiplier() {
        return this.dropMultiplier;
    }

    public Boolean autoSmelt() {
        return this.autoSmelt;
    }

    public Boolean fortuneEnabled() {
        return this.fortuneEnabled;
    }

    public Double experienceMultiplier() {
        return this.experienceMultiplier;
    }
}
