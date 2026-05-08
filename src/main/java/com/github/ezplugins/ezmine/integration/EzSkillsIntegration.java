package com.github.ezplugins.ezmine.integration;

import com.github.ezplugins.ezskills.api.EzSkillsAPI;
import com.github.ezplugins.ezmine.EzMine;
import com.github.ezplugins.ezmine.util.BukkitCompatibility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public class EzSkillsIntegration {
    private final EzMine plugin;
    private String skillName;
    private boolean enabled;
    private boolean experienceEnabled;
    private double baseExperiencePerBlock;
    private Map<Material, Double> materialExperience;
    private boolean applyRankMultiplier;
    private boolean perWorldEnabled;
    private Map<String, String> worldSkillOverrides;

    public EzSkillsIntegration(EzMine plugin) {
        this.plugin = plugin;
        this.materialExperience = Collections.emptyMap();
        this.worldSkillOverrides = Collections.emptyMap();
    }

    public void reload(FileConfiguration configuration) {
        this.skillName = null;
        this.enabled = false;
        this.experienceEnabled = false;
        this.baseExperiencePerBlock = 0.0D;
        this.materialExperience = Collections.emptyMap();
        this.applyRankMultiplier = true;
        this.perWorldEnabled = false;
        this.worldSkillOverrides = Collections.emptyMap();

        ConfigurationSection section = configuration.getConfigurationSection("ezskills");
        if (section == null) {
            return;
        }

        Object enabledValue = section.get("enabled");
        Boolean enabledSetting = null;

        if (enabledValue instanceof Boolean) {
            enabledSetting = (Boolean) enabledValue;
        } else if (enabledValue instanceof String) {
            String rawValue = ((String) enabledValue).trim().toLowerCase(Locale.ROOT);
            if ("auto".equals(rawValue)) {
                enabledSetting = null;
            } else if ("true".equals(rawValue) || "false".equals(rawValue)) {
                enabledSetting = Boolean.parseBoolean(rawValue);
            } else {
                this.plugin.getLogger().warning(
                    "Unknown ezskills.enabled value '" + enabledValue
                    + "'. Expected true, false, or auto. Defaulting to auto-detect.");
                enabledSetting = null;
            }
        }

        if (Boolean.FALSE.equals(enabledSetting)) {
            this.plugin.getLogger().info("EzSkills integration is disabled in configuration.");
            return;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("EzSkills")) {
            if (Boolean.TRUE.equals(enabledSetting)) {
                this.plugin.getLogger().warning("EzSkills integration enabled but EzSkills plugin is missing.");
            }
            return;
        }

        this.skillName = section.getString("skill", "MINING");
        if (this.skillName == null || this.skillName.trim().isEmpty()) {
            this.plugin.getLogger().warning("EzSkills integration is enabled but no skill name is configured.");
            return;
        }
        this.skillName = this.skillName.toUpperCase(Locale.ROOT);
        ConfigurationSection perWorldSection = section.getConfigurationSection("per-world");
        if (perWorldSection != null) {
            this.perWorldEnabled = perWorldSection.getBoolean("enabled", false);
            ConfigurationSection overrideSection = perWorldSection.getConfigurationSection("skill-overrides");
            if (overrideSection != null) {
                Map<String, String> overrides = new HashMap<>();
                for (String worldKey : overrideSection.getKeys(false)) {
                    String skill = overrideSection.getString(worldKey);
                    if (skill != null && !skill.trim().isEmpty()) {
                        overrides.put(worldKey.toLowerCase(Locale.ROOT), skill.trim().toUpperCase(Locale.ROOT));
                    }
                }
                if (!overrides.isEmpty()) {
                    this.worldSkillOverrides = Collections.unmodifiableMap(overrides);
                }
            }
        }

        ConfigurationSection experienceSection = section.getConfigurationSection("experience");
        if (experienceSection != null) {
            this.baseExperiencePerBlock = Math.max(0.0D, experienceSection.getDouble("base-per-block", 0.0D));
            this.applyRankMultiplier = experienceSection.getBoolean("apply-rank-multiplier", true);

            ConfigurationSection overridesSection = experienceSection.getConfigurationSection("material-overrides");
            if (overridesSection != null) {
                Map<Material, Double> overrides = new EnumMap<>(Material.class);
                for (String key : overridesSection.getKeys(false)) {
                    Material material = BukkitCompatibility.matchMaterial(key);
                    if (material == null) {
                        this.plugin.getLogger().log(
                            Level.WARNING,
                            "Unknown material in ezskills.experience.material-overrides: {0}",
                            key);
                        continue;
                    }

                    double amount = Math.max(0.0D, overridesSection.getDouble(key, 0.0D));
                    if (amount > 0.0D) {
                        overrides.put(material, amount);
                    }
                }
                if (!overrides.isEmpty()) {
                    this.materialExperience = Collections.unmodifiableMap(overrides);
                }
            }
        }

        this.enabled = true;
        boolean hasPositiveOverride = this.materialExperience.values().stream().anyMatch(value -> value > 0.0D);
        this.experienceEnabled = this.baseExperiencePerBlock > 0.0D || hasPositiveOverride;
        this.plugin.getLogger().info("EzSkills integration enabled (skill: " + this.skillName + ").");
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean hasExperienceRewards() {
        return this.experienceEnabled;
    }

    public int getSkillLevel(Player player, String worldName) {
        if (!this.enabled || player == null || this.skillName == null) {
            return 0;
        }

        try {
            return EzSkillsAPI.getSkillLevel(player, this.resolveSkillName(worldName));
        } catch (IllegalStateException exception) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to query EzSkills level", exception);
            this.enabled = false;
        }

        return 0;
    }

    public void awardExperience(Player player, String worldName, Material material, double rankExperienceMultiplier) {
        if (!this.experienceEnabled || player == null || this.skillName == null) {
            return;
        }

        double base = this.materialExperience.getOrDefault(material, this.baseExperiencePerBlock);
        if (base <= 0.0D) {
            return;
        }

        double amount = this.applyRankMultiplier ? base * rankExperienceMultiplier : base;
        if (amount <= 0.0D) {
            return;
        }

        try {
            EzSkillsAPI.addExperience(player.getUniqueId(), this.resolveSkillName(worldName), amount);
        } catch (IllegalStateException exception) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to award EzSkills mining experience", exception);
        }
    }

    private String resolveSkillName(String worldName) {
        if (!this.perWorldEnabled || worldName == null || this.worldSkillOverrides.isEmpty()) {
            return this.skillName;
        }
        String override = this.worldSkillOverrides.get(worldName.toLowerCase(Locale.ROOT));
        return override != null ? override : this.skillName;
    }
}
