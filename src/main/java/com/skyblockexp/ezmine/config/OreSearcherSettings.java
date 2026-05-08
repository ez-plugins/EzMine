package com.skyblockexp.ezmine.config;

import com.skyblockexp.ezmine.EzMine;
import com.skyblockexp.ezmine.util.BukkitCompatibility;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;

public class OreSearcherSettings {

    private static final List<String> DEFAULT_TARGETS = Arrays.asList(
        "COAL_ORE",
        "DEEPSLATE_COAL_ORE",
        "IRON_ORE",
        "DEEPSLATE_IRON_ORE",
        "COPPER_ORE",
        "DEEPSLATE_COPPER_ORE",
        "GOLD_ORE",
        "DEEPSLATE_GOLD_ORE",
        "NETHER_GOLD_ORE",
        "REDSTONE_ORE",
        "DEEPSLATE_REDSTONE_ORE",
        "LAPIS_ORE",
        "DEEPSLATE_LAPIS_ORE",
        "EMERALD_ORE",
        "DEEPSLATE_EMERALD_ORE",
        "DIAMOND_ORE",
        "DEEPSLATE_DIAMOND_ORE",
        "ANCIENT_DEBRIS"
    );

    private final boolean enabled;
    private final int range;
    private final int maxResults;
    private final Particle particle;
    private final int particleCount;
    private final double particleDistance;
    private final Set<Material> targetMaterials;

    private OreSearcherSettings(boolean enabled,
                                int range,
                                int maxResults,
                                Particle particle,
                                int particleCount,
                                double particleDistance,
                                Set<Material> targetMaterials) {
        this.enabled = enabled;
        this.range = range;
        this.maxResults = maxResults;
        this.particle = particle;
        this.particleCount = particleCount;
        this.particleDistance = particleDistance;
        this.targetMaterials = targetMaterials == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(targetMaterials));
    }

    public static OreSearcherSettings createDefault(EzMine plugin) {
        return fromConfiguration(plugin, null);
    }

    public static OreSearcherSettings fromConfiguration(EzMine plugin, FileConfiguration configuration) {
        boolean enabled = true;
        int range = 8;
        int maxResults = 6;
        Particle particle;
        try {
            particle = Particle.valueOf("VILLAGER_HAPPY");
        } catch (IllegalArgumentException e) {
            particle = Particle.HEART; // fallback for older Bukkit versions
        }
        int particleCount = 3;
        double particleDistance = 1.5D;
        List<String> targetKeys = DEFAULT_TARGETS;

        if (configuration != null) {
            ConfigurationSection section = configuration.getConfigurationSection("settings.actions.ore-searcher");
            if (section != null) {
                enabled = section.getBoolean("enabled", enabled);
                range = Math.max(1, section.getInt("range", range));
                maxResults = Math.max(1, section.getInt("max-results", maxResults));
                particleCount = Math.max(1, section.getInt("particle-count", particleCount));
                particleDistance = Math.max(0.5D, section.getDouble("particle-distance", particleDistance));
                String particleName = section.getString("particle");
                if (particleName != null && !particleName.trim().isEmpty()) {
                    try {
                        particle = Particle.valueOf(particleName.trim().toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException exception) {
                        plugin.getLogger().log(Level.WARNING, "Unknown particle for ore-searcher: {0}", particleName);
                    }
                }

                List<String> configuredTargets = section.getStringList("targets");
                if (configuredTargets != null && !configuredTargets.isEmpty()) {
                    targetKeys = configuredTargets;
                }
            }
        }

        Set<Material> targets = new HashSet<>();
        for (String key : targetKeys) {
            Material material = BukkitCompatibility.matchMaterial(key);
            if (material == null) {
                plugin.getLogger().log(Level.WARNING, "Unknown material in ore-searcher.targets: {0}", key);
                continue;
            }
            targets.add(material);
        }

        return new OreSearcherSettings(enabled, range, maxResults, particle, particleCount, particleDistance, targets);
    }

    public boolean enabled() {
        return this.enabled;
    }

    public int range() {
        return this.range;
    }

    public int maxResults() {
        return this.maxResults;
    }

    public Particle particle() {
        return this.particle;
    }

    public int particleCount() {
        return this.particleCount;
    }

    public double particleDistance() {
        return this.particleDistance;
    }

    public boolean isTarget(Material material) {
        return this.targetMaterials.contains(material);
    }
}
