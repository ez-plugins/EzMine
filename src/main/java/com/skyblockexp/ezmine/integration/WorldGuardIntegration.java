package com.skyblockexp.ezmine.integration;

import com.skyblockexp.ezmine.EzMine;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class WorldGuardIntegration {

    private final EzMine plugin;
    private boolean enabled;
    private boolean pluginPresent;
    private boolean requireRegion;
    private int minimumPriority;
    private List<String> allowedGlobalRegions;
    private List<String> blockedGlobalRegions;
    private final java.util.Map<String, List<String>> allowedWorldRegions = new java.util.HashMap<>();
    private final java.util.Map<String, List<String>> blockedWorldRegions = new java.util.HashMap<>();

    public WorldGuardIntegration(EzMine plugin) {
        this.plugin = plugin;
    }

    public void reload(FileConfiguration configuration) {
        this.enabled = false;
        this.pluginPresent = false;
        this.requireRegion = false;
        this.minimumPriority = 0;
        this.allowedGlobalRegions = Collections.emptyList();
        this.blockedGlobalRegions = Collections.emptyList();
        this.allowedWorldRegions.clear();
        this.blockedWorldRegions.clear();

        ConfigurationSection section = configuration.getConfigurationSection("settings.worldguard");
        boolean configuredEnabled = section == null || section.getBoolean("enabled", true);
        if (section != null) {
            this.requireRegion = section.getBoolean("require-region", false);
            this.minimumPriority = section.getInt("minimum-priority", 0);
            this.allowedGlobalRegions = toLowercaseList(section.getStringList("allowed-regions.global"));
            this.blockedGlobalRegions = toLowercaseList(section.getStringList("blocked-regions.global"));
            loadWorldRegionMap(section.getConfigurationSection("allowed-regions"), this.allowedWorldRegions);
            loadWorldRegionMap(section.getConfigurationSection("blocked-regions"), this.blockedWorldRegions);
        }

        Plugin dependency = Bukkit.getPluginManager().getPlugin("WorldGuard");
        this.pluginPresent = dependency != null && dependency.isEnabled();
        if (!configuredEnabled) {
            if (this.pluginPresent) {
                this.plugin.getLogger().info("WorldGuard integration disabled in configuration.");
            }
            return;
        }

        if (!this.pluginPresent) {
            this.plugin.getLogger().warning("WorldGuard integration enabled but WorldGuard plugin is missing.");
            return;
        }

        this.enabled = true;
        this.plugin.getLogger().info("WorldGuard integration enabled.");
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isPluginPresent() {
        return this.pluginPresent;
    }

    public List<String> getApplicableRegions(Location location) {
        if (!this.enabled || location == null || location.getWorld() == null) {
            return Collections.emptyList();
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(location.getWorld()));
        if (manager == null) {
            return Collections.emptyList();
        }

        ApplicableRegionSet regions = manager.getApplicableRegions(BlockVector3.at(
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ()
        ));
        if (regions == null || regions.size() == 0) {
            return Collections.emptyList();
        }

        List<ProtectedRegion> sorted = new ArrayList<>(regions.getRegions());
        sorted.sort(Comparator.comparingInt(ProtectedRegion::getPriority).reversed());

        List<String> ids = new ArrayList<>();
        for (ProtectedRegion region : sorted) {
            if (region.getPriority() < this.minimumPriority) {
                continue;
            }
            ids.add(region.getId().toLowerCase(Locale.ROOT));
        }
        return ids;
    }

    public boolean isMiningAllowed(Location location) {
        if (!this.enabled || location == null || location.getWorld() == null) {
            return true;
        }

        List<String> regions = this.getApplicableRegions(location);
        if (this.requireRegion && !hasNonGlobalRegion(regions)) {
            return false;
        }

        String worldKey = location.getWorld().getName().toLowerCase(Locale.ROOT);
        if (containsRegion(regions, this.blockedGlobalRegions) || containsRegion(regions, this.blockedWorldRegions.get(worldKey))) {
            return false;
        }

        List<String> allowedWorld = this.allowedWorldRegions.get(worldKey);
        boolean hasAllowed = (this.allowedGlobalRegions != null && !this.allowedGlobalRegions.isEmpty())
            || (allowedWorld != null && !allowedWorld.isEmpty());
        if (!hasAllowed) {
            return true;
        }

        return containsRegion(regions, this.allowedGlobalRegions) || containsRegion(regions, allowedWorld);
    }

    private static void loadWorldRegionMap(ConfigurationSection section, java.util.Map<String, List<String>> target) {
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            if ("global".equalsIgnoreCase(key)) {
                continue;
            }
            List<String> regions = toLowercaseList(section.getStringList(key));
            if (!regions.isEmpty()) {
                target.put(key.toLowerCase(Locale.ROOT), regions);
            }
        }
    }

    private static List<String> toLowercaseList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                result.add(value.trim().toLowerCase(Locale.ROOT));
            }
        }
        return result;
    }

    private static boolean containsRegion(List<String> regions, List<String> targets) {
        if (regions == null || regions.isEmpty() || targets == null || targets.isEmpty()) {
            return false;
        }
        for (String region : regions) {
            if (targets.contains(region)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasNonGlobalRegion(List<String> regions) {
        if (regions == null) {
            return false;
        }
        for (String region : regions) {
            if (!"__global__".equalsIgnoreCase(region)) {
                return true;
            }
        }
        return false;
    }
}
