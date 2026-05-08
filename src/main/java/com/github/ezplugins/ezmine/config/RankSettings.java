package com.github.ezplugins.ezmine.config;

import org.bukkit.Material;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class RankSettings {

    private final String name;
    private final String permission;
    private final int minimumSkillLevel;
    private final double dropMultiplier;
    private final boolean autoSmelt;
    private final boolean fortuneEnabled;
    private final double experienceMultiplier;
    private final Map<Material, BlockOverride> blockOverrides;
    private final String luckPermsGroup;
    private final String luckPermsPermission;

    public RankSettings(String name,
                        String permission,
                        int minimumSkillLevel,
                        double dropMultiplier,
                        boolean autoSmelt,
                        boolean fortuneEnabled,
                        double experienceMultiplier,
                        Map<Material, BlockOverride> blockOverrides,
                        String luckPermsGroup,
                        String luckPermsPermission) {
        this.name = name;
        this.permission = permission;
        this.minimumSkillLevel = Math.max(0, minimumSkillLevel);
        this.dropMultiplier = dropMultiplier;
        this.autoSmelt = autoSmelt;
        this.fortuneEnabled = fortuneEnabled;
        this.experienceMultiplier = experienceMultiplier;
        this.luckPermsGroup = normalizeRequirement(luckPermsGroup);
        this.luckPermsPermission = normalizeRequirement(luckPermsPermission);
        if (blockOverrides == null || blockOverrides.isEmpty()) {
            this.blockOverrides = Collections.emptyMap();
        } else {
            Map<Material, BlockOverride> overridesCopy = new EnumMap<>(Material.class);
            overridesCopy.putAll(blockOverrides);
            this.blockOverrides = Collections.unmodifiableMap(overridesCopy);
        }
    }

    public String name() {
        return this.name;
    }

    public String permission() {
        return this.permission;
    }

    public int minimumSkillLevel() {
        return this.minimumSkillLevel;
    }

    public double dropMultiplier() {
        return this.dropMultiplier;
    }

    public boolean autoSmelt() {
        return this.autoSmelt;
    }

    public boolean fortuneEnabled() {
        return this.fortuneEnabled;
    }

    public double experienceMultiplier() {
        return this.experienceMultiplier;
    }

    public Map<Material, BlockOverride> blockOverrides() {
        return this.blockOverrides;
    }

    public MineConfiguration.AppliedSettings appliedSettings(Material material) {
        BlockOverride override = this.blockOverrides.get(material);
        if (override == null) {
            return new MineConfiguration.AppliedSettings(
                this.dropMultiplier, this.autoSmelt,
                this.fortuneEnabled, this.experienceMultiplier);
        }
        double resolvedDropMultiplier = override.dropMultiplier() != null
            ? override.dropMultiplier() : this.dropMultiplier;
        boolean resolvedAutoSmelt = override.autoSmelt() != null
            ? override.autoSmelt() : this.autoSmelt;
        boolean resolvedFortune = override.fortuneEnabled() != null
            ? override.fortuneEnabled() : this.fortuneEnabled;
        double resolvedExperience = override.experienceMultiplier() != null
            ? override.experienceMultiplier() : this.experienceMultiplier;
        return new MineConfiguration.AppliedSettings(
            resolvedDropMultiplier, resolvedAutoSmelt, resolvedFortune, resolvedExperience);
    }

    public static RankSettings createDefault() {
        return new RankSettings("default", "", 0, 1.0D, false, true, 1.0D, Collections.emptyMap(), "", "");
    }

    public boolean meetsSkillRequirement(int playerLevel, boolean enforceSkillRequirement) {
        if (!enforceSkillRequirement) {
            return true;
        }

        return playerLevel >= this.minimumSkillLevel;
    }

    public boolean meetsLuckPermsRequirement(String primaryGroup,
                                             String groupPermissionPrefix,
                                             boolean usePrimaryGroup,
                                             org.bukkit.entity.Player player) {
        if (this.luckPermsGroup == null && this.luckPermsPermission == null) {
            return true;
        }

        if (this.luckPermsPermission != null && player != null && player.hasPermission(this.luckPermsPermission)) {
            return true;
        }

        if (this.luckPermsGroup != null) {
            if (usePrimaryGroup) {
                return primaryGroup != null && primaryGroup.equalsIgnoreCase(this.luckPermsGroup);
            }
            if (player != null) {
                String prefix = groupPermissionPrefix == null ? "" : groupPermissionPrefix;
                return player.hasPermission(prefix + this.luckPermsGroup);
            }
        }

        return false;
    }

    private static String normalizeRequirement(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
