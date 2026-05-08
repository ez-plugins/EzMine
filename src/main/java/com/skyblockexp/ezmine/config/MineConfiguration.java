package com.skyblockexp.ezmine.config;

import com.skyblockexp.ezmine.EzMine;
import com.skyblockexp.ezmine.util.BukkitCompatibility;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class MineConfiguration {

    private final EzMine plugin;
    private final Map<String, ProfileSettings> profiles;
    private String defaultProfileName;
    private Set<Material> trackedMaterials;
    private Map<Material, Material> autoSmeltResults;
    private final CustomToolConfiguration customToolConfiguration;
    private ConfigurationReloadResult customToolReloadResult;
    private String luckPermsGroupPermissionPrefix;
    private boolean useLuckPermsGroup;
    private OreSearcherSettings oreSearcherSettings;
    private WorldSettings worldSettings;
    private CustomTextureSettings customTextureSettings;

    public MineConfiguration(EzMine plugin) {
        this.plugin = plugin;
        this.profiles = new LinkedHashMap<>();
        this.defaultProfileName = "default";
        this.trackedMaterials = new HashSet<>();
        this.autoSmeltResults = Collections.emptyMap();
        this.customToolConfiguration = new CustomToolConfiguration(plugin);
        this.oreSearcherSettings = OreSearcherSettings.createDefault(plugin);
        this.worldSettings = WorldSettings.createDefault();
        this.luckPermsGroupPermissionPrefix = "group.";
        this.useLuckPermsGroup = false;
        this.customTextureSettings = new CustomTextureSettings(false, "", Collections.emptyMap());
    }

    public void reload(FileConfiguration settingsConfiguration,
                       FileConfiguration ranksConfiguration,
                       FileConfiguration toolsConfiguration) {
        this.trackedMaterials = this.parseTrackedMaterials(settingsConfiguration);
        this.autoSmeltResults = this.parseAutoSmeltResults(settingsConfiguration);
        this.oreSearcherSettings = OreSearcherSettings.fromConfiguration(this.plugin, settingsConfiguration);
        this.worldSettings = WorldSettings.fromConfiguration(settingsConfiguration);
        this.parseLuckPermsSettings(settingsConfiguration);
        Map<String, ProfileSettings> parsedProfiles = this.parseProfiles(settingsConfiguration, ranksConfiguration);
        this.profiles.clear();
        this.profiles.putAll(parsedProfiles);
        // Load custom textures from both settings.yml and tools.yml (tools.yml takes precedence for custom tools)
        this.customTextureSettings = CustomTextureSettings.fromMultipleConfigurations(
            settingsConfiguration, toolsConfiguration);
        this.defaultProfileName = this.resolveDefaultProfileName(this.worldSettings, parsedProfiles);

        ConfigurationReloadResult toolResult = this.customToolConfiguration.reload(toolsConfiguration);
        this.customToolReloadResult = toolResult;
        if (toolResult != null) {
            if (toolResult.hasErrors()) {
                this.plugin.getLogger().warning(
                    "Errors while loading custom tools: " + toolResult.getErrorMessages());
            }
            if (toolResult.hasWarnings()) {
                this.plugin.getLogger().info("Warnings while loading custom tools: " + toolResult.getWarningMessages());
            }
        }

        int totalRanks = this.profiles.values().stream()
            .mapToInt(profile -> profile.rankSettings.size())
            .sum();
        this.plugin.getLogger().info(
            "Loaded " + this.profiles.size() + " profile(s) with " + totalRanks
                + " rank definition(s) for EzMine");
    }

    public CustomTextureSettings getCustomTextureSettings() {
        return this.customTextureSettings;
    }

    public boolean isTracked(Material material) {
        return this.trackedMaterials.contains(material);
    }

    public boolean isWorldEnabled(String worldName) {
        return this.worldSettings.isWorldEnabled(worldName);
    }

    public String resolveProfileName(String worldName, Collection<String> regionNames) {
        String profileName = this.worldSettings.resolveProfile(worldName, regionNames);
        if (profileName == null) {
            return this.defaultProfileName;
        }
        String normalized = profileName.toLowerCase(Locale.ROOT);
        if (this.profiles.containsKey(normalized)) {
            return normalized;
        }
        return this.defaultProfileName;
    }

    public String getDefaultProfileName() {
        return this.defaultProfileName;
    }

    public AppliedSettings resolveSettings(
            Player player, Material material, int skillLevel,
            boolean enforceSkillRequirement, String profileName, String luckPermsGroup) {
        RankSettings rank = this.resolveRank(player, skillLevel, enforceSkillRequirement, profileName, luckPermsGroup);
        if (rank == null) {
            return AppliedSettings.DEFAULT;
        }
        return rank.appliedSettings(material);
    }

    public String resolveRankName(
            Player player, int skillLevel,
            boolean enforceSkillRequirement, String profileName, String luckPermsGroup) {
        RankSettings rank = this.resolveRank(player, skillLevel, enforceSkillRequirement, profileName, luckPermsGroup);
        return rank != null ? rank.name() : "default";
    }

    public List<String> getRankNames(String profileName) {
        ProfileSettings profile = this.getProfile(profileName);
        if (profile == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(profile.rankOrder);
    }

    public Material resolveAutoSmeltResult(Material dropType, Material sourceMaterial) {
        Material result = this.autoSmeltResults.get(dropType);
        if (result != null) {
            return result;
        }
        return this.autoSmeltResults.get(sourceMaterial);
    }

    public CustomToolConfiguration getCustomToolConfiguration() {
        return this.customToolConfiguration;
    }

    public ConfigurationReloadResult getCustomToolReloadResult() {
        return this.customToolReloadResult;
    }

    public OreSearcherSettings getOreSearcherSettings() {
        return this.oreSearcherSettings;
    }
    
    private RankSettings resolveRank(
            Player player, int skillLevel,
            boolean enforceSkillRequirement, String profileName, String luckPermsGroup) {
        ProfileSettings profile = this.getProfile(profileName);
        if (profile == null || profile.rankOrder.isEmpty()) {
            return null;
        }

        for (String name : profile.rankOrder) {
            RankSettings rank = profile.rankSettings.get(name);
            if (rank == null) {
                continue;
            }
            if ((rank.permission() == null || rank.permission().trim().isEmpty()
                        || player.hasPermission(rank.permission()))
                    && rank.meetsSkillRequirement(skillLevel, enforceSkillRequirement)
                    && rank.meetsLuckPermsRequirement(
                        luckPermsGroup,
                        this.luckPermsGroupPermissionPrefix,
                        this.useLuckPermsGroup, player)) {
                return rank;
            }
        }

        return profile.rankSettings.get(profile.rankOrder.get(0));
    }

    private Set<Material> parseTrackedMaterials(FileConfiguration configuration) {
        List<String> materialKeys = configuration.getStringList("settings.tracked-blocks");
        Set<Material> materials = new HashSet<>();
        if (materialKeys.contains("*")) {
            // Wildcard: add all block materials
            for (Material mat : Material.values()) {
                if (mat.isBlock()) {
                    materials.add(mat);
                }
            }
            return materials;
        }
        if (materialKeys.isEmpty()) {
            materialKeys = Arrays.asList(
                "STONE", "COBBLESTONE",
                "COAL_ORE", "DEEPSLATE_COAL_ORE",
                "IRON_ORE", "DEEPSLATE_IRON_ORE",
                "COPPER_ORE", "DEEPSLATE_COPPER_ORE",
                "GOLD_ORE", "DEEPSLATE_GOLD_ORE", "NETHER_GOLD_ORE",
                "REDSTONE_ORE", "DEEPSLATE_REDSTONE_ORE",
                "LAPIS_ORE", "DEEPSLATE_LAPIS_ORE",
                "EMERALD_ORE", "DEEPSLATE_EMERALD_ORE",
                "DIAMOND_ORE", "DEEPSLATE_DIAMOND_ORE",
                "ANCIENT_DEBRIS"
            );
        }
        for (String key : materialKeys) {
            Material material = BukkitCompatibility.matchMaterial(key);
            if (material == null) {
                this.plugin.getLogger().warning("Unknown material in settings.tracked-blocks: " + key);
                continue;
            }
            materials.add(material);
        }
        return materials;
    }

    private Map<Material, Material> parseAutoSmeltResults(FileConfiguration configuration) {
        Map<Material, Material> defaults = this.defaultAutoSmeltResults();

        boolean useDefaults = configuration.getBoolean("settings.auto-smelt-use-defaults", true);
        Map<Material, Material> results = new EnumMap<>(Material.class);
        if (useDefaults) {
            results.putAll(defaults);
        }

        ConfigurationSection section = configuration.getConfigurationSection("settings.auto-smelt-results");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String value = section.getString(key);
                if (value == null) {
                    this.plugin.getLogger().warning("Missing value for auto-smelt material: " + key);
                    continue;
                }

                Material source = BukkitCompatibility.matchMaterial(key);
                if (source == null) {
                    this.plugin.getLogger().warning("Unknown material in settings.auto-smelt-results: " + key);
                    continue;
                }

                Material result = BukkitCompatibility.matchMaterial(value);
                if (result == null) {
                    this.plugin.getLogger().warning("Unknown smelt result material for " + key + ": " + value);
                    continue;
                }

                results.put(source, result);
            }
        }

        return Collections.unmodifiableMap(results);
    }

    private Map<Material, Material> defaultAutoSmeltResults() {
        Map<Material, Material> map = new EnumMap<>(Material.class);
        addAutoSmeltPair(map, "IRON_ORE", "IRON_INGOT");
        addAutoSmeltPair(map, "DEEPSLATE_IRON_ORE", "IRON_INGOT");
        addAutoSmeltPair(map, "RAW_IRON", "IRON_INGOT");
        addAutoSmeltPair(map, "GOLD_ORE", "GOLD_INGOT");
        addAutoSmeltPair(map, "DEEPSLATE_GOLD_ORE", "GOLD_INGOT");
        addAutoSmeltPair(map, "NETHER_GOLD_ORE", "GOLD_NUGGET");
        addAutoSmeltPair(map, "RAW_GOLD", "GOLD_INGOT");
        addAutoSmeltPair(map, "COPPER_ORE", "COPPER_INGOT");
        addAutoSmeltPair(map, "DEEPSLATE_COPPER_ORE", "COPPER_INGOT");
        addAutoSmeltPair(map, "RAW_COPPER", "COPPER_INGOT");
        addAutoSmeltPair(map, "ANCIENT_DEBRIS", "NETHERITE_SCRAP");
        addAutoSmeltPair(map, "COBBLESTONE", "STONE");
        addAutoSmeltPair(map, "SAND", "GLASS");
        addAutoSmeltPair(map, "RED_SAND", "GLASS");
        addAutoSmeltPair(map, "WET_SPONGE", "SPONGE");
        return map;
    }

    private Map<String, ProfileSettings> parseProfiles(
            FileConfiguration settingsConfiguration, FileConfiguration ranksConfiguration) {
        Map<String, ProfileSettings> parsedProfiles = new LinkedHashMap<>();
        ConfigurationSection profilesSection = ranksConfiguration.getConfigurationSection("profiles");
        if (profilesSection != null) {
            for (String profileKey : profilesSection.getKeys(false)) {
                ConfigurationSection profileSection = profilesSection.getConfigurationSection(profileKey);
                if (profileSection == null) {
                    continue;
                }
                ConfigurationSection ranksSection = profileSection.getConfigurationSection("ranks");
                if (ranksSection == null) {
                    continue;
                }
                Map<String, RankSettings> parsedRanks = this.parseRankSettings(ranksSection);
                if (parsedRanks.isEmpty()) {
                    parsedRanks.put("default", RankSettings.createDefault());
                }
                List<String> rankOrder = this.parseRankOrder(
                    profileSection, settingsConfiguration, parsedRanks.keySet());
                if (rankOrder.isEmpty()) {
                    rankOrder = new ArrayList<>(parsedRanks.keySet());
                }
                parsedProfiles.put(profileKey.toLowerCase(Locale.ROOT), new ProfileSettings(parsedRanks, rankOrder));
            }
        }

        if (!parsedProfiles.isEmpty()) {
            return parsedProfiles;
        }

        ConfigurationSection ranksSection = ranksConfiguration.getConfigurationSection("ranks");
        if (ranksSection == null) {
            this.plugin.getLogger().warning("No ranks configured for EzMine. Falling back to defaults.");
            Map<String, RankSettings> defaults = new LinkedHashMap<>();
            defaults.put("default", RankSettings.createDefault());
            List<String> order = new ArrayList<>(defaults.keySet());
            parsedProfiles.put("default", new ProfileSettings(defaults, order));
            return parsedProfiles;
        }

        Map<String, RankSettings> parsedRanks = this.parseRankSettings(ranksSection);
        if (parsedRanks.isEmpty()) {
            parsedRanks.put("default", RankSettings.createDefault());
        }
        List<String> rankOrder = this.parseRankOrder(settingsConfiguration, parsedRanks.keySet());
        if (rankOrder.isEmpty()) {
            rankOrder = new ArrayList<>(parsedRanks.keySet());
        }
        parsedProfiles.put("default", new ProfileSettings(parsedRanks, rankOrder));
        return parsedProfiles;
    }

    private Map<String, RankSettings> parseRankSettings(ConfigurationSection ranksSection) {
        Map<String, RankSettings> parsedRanks = new LinkedHashMap<>();
        for (String key : ranksSection.getKeys(false)) {
            ConfigurationSection rankSection = ranksSection.getConfigurationSection(key);
            if (rankSection == null) {
                continue;
            }

            String permission = rankSection.getString("permission", "").trim();
            int minimumSkillLevel = Math.max(0, rankSection.getInt("minimum-skill-level", 0));
            double dropMultiplier = rankSection.getDouble("drop-multiplier", 1.0D);
            double experienceMultiplier = rankSection.getDouble("experience-multiplier", 1.0D);
            boolean autoSmelt = rankSection.getBoolean("auto-smelt", false);
            boolean fortuneEnabled = rankSection.getBoolean("fortune", true);
            String luckPermsGroup = rankSection.getString("luckperms-group", "").trim();
            String luckPermsPermission = rankSection.getString("luckperms-permission", "").trim();

            Map<Material, BlockOverride> overrides =
                this.parseOverrides(rankSection.getConfigurationSection("block-overrides"));

            parsedRanks.put(key, new RankSettings(
                key, permission, minimumSkillLevel, dropMultiplier,
                autoSmelt, fortuneEnabled, experienceMultiplier,
                overrides, luckPermsGroup, luckPermsPermission));
        }

        return parsedRanks;
    }

    private Map<Material, BlockOverride> parseOverrides(ConfigurationSection overridesSection) {
        if (overridesSection == null) {
            return Collections.emptyMap();
        }

        Map<Material, BlockOverride> overrides = new EnumMap<>(Material.class);
        for (String key : overridesSection.getKeys(false)) {
            ConfigurationSection section = overridesSection.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            Material material = BukkitCompatibility.matchMaterial(key.toUpperCase(Locale.ROOT));
            if (material == null) {
                this.plugin.getLogger().log(Level.WARNING, "Unknown material in block-overrides: {0}", key);
                continue;
            }

            Double dropMultiplier = section.isSet("drop-multiplier") ? section.getDouble("drop-multiplier") : null;
            Double experienceMultiplier = section.isSet("experience-multiplier")
                ? section.getDouble("experience-multiplier") : null;
            Boolean autoSmelt = section.isSet("auto-smelt") ? section.getBoolean("auto-smelt") : null;
            Boolean fortune = section.isSet("fortune") ? section.getBoolean("fortune") : null;

            overrides.put(material, new BlockOverride(dropMultiplier, autoSmelt, fortune, experienceMultiplier));
        }
        return overrides;
    }

    private List<String> parseRankOrder(
            ConfigurationSection profileSection, FileConfiguration configuration,
            Set<String> configuredRanks) {
        List<String> definedOrder = Collections.emptyList();
        if (profileSection != null) {
            definedOrder = profileSection.getStringList("rank-order");
        }
        if (definedOrder == null || definedOrder.isEmpty()) {
            definedOrder = configuration.getStringList("settings.rank-order");
        }

        List<String> order = new ArrayList<>();
        if (definedOrder != null && !definedOrder.isEmpty()) {
            Set<String> seen = new LinkedHashSet<>();
            for (String name : definedOrder) {
                if (configuredRanks.contains(name) && seen.add(name)) {
                    order.add(name);
                }
            }
        }

        if (order.isEmpty()) {
            order.addAll(configuredRanks);
        } else {
            for (String name : configuredRanks) {
                if (!order.contains(name)) {
                    order.add(name);
                }
            }
        }
        return order;
    }

    private List<String> parseRankOrder(FileConfiguration configuration, Set<String> configuredRanks) {
        return this.parseRankOrder(null, configuration, configuredRanks);
    }

    private void parseLuckPermsSettings(FileConfiguration configuration) {
        if (configuration == null) {
            this.useLuckPermsGroup = false;
            this.luckPermsGroupPermissionPrefix = "group.";
            return;
        }

        ConfigurationSection section = configuration.getConfigurationSection("settings.luckperms");
        if (section == null) {
            this.useLuckPermsGroup = false;
            this.luckPermsGroupPermissionPrefix = "group.";
            return;
        }

        boolean enabled = section.getBoolean("enabled", false);
        this.useLuckPermsGroup = enabled && section.getBoolean("use-primary-group", false);
        this.luckPermsGroupPermissionPrefix = section.getString("group-permission-prefix", "group.");
    }

    private ProfileSettings getProfile(String profileName) {
        if (this.profiles.isEmpty()) {
            return null;
        }

        String key = profileName == null ? null : profileName.toLowerCase(Locale.ROOT);
        if (key != null) {
            ProfileSettings profile = this.profiles.get(key);
            if (profile != null) {
                return profile;
            }
        }

        ProfileSettings defaultProfile = this.profiles.get(this.defaultProfileName);
        if (defaultProfile != null) {
            return defaultProfile;
        }

        return this.profiles.values().iterator().next();
    }

    private String resolveDefaultProfileName(WorldSettings settings, Map<String, ProfileSettings> parsedProfiles) {
        String configuredDefault = settings.getDefaultProfile();
        if (configuredDefault != null && parsedProfiles.containsKey(configuredDefault.toLowerCase(Locale.ROOT))) {
            return configuredDefault.toLowerCase(Locale.ROOT);
        }

        if (configuredDefault != null && !parsedProfiles.isEmpty()) {
            this.plugin.getLogger().warning(
                "Default profile '" + configuredDefault
                    + "' is not defined in ranks.yml. Falling back to first configured profile.");
        }

        if (!parsedProfiles.isEmpty()) {
            return parsedProfiles.keySet().iterator().next();
        }

        return "default";
    }

    private void addAutoSmeltPair(Map<Material, Material> map, String sourceName, String resultName) {
        Material source = BukkitCompatibility.matchMaterial(sourceName);
        Material result = BukkitCompatibility.matchMaterial(resultName);
        if (source == null || result == null) {
            return;
        }
        map.put(source, result);
    }

    public static class AppliedSettings {
        public static final AppliedSettings DEFAULT = new AppliedSettings(1.0D, false, true, 1.0D);

        private final double dropMultiplier;
        private final boolean autoSmelt;
        private final boolean fortuneEnabled;
        private final double experienceMultiplier;

        public AppliedSettings(double dropMultiplier,
                               boolean autoSmelt,
                               boolean fortuneEnabled,
                               double experienceMultiplier) {
            this.dropMultiplier = dropMultiplier;
            this.autoSmelt = autoSmelt;
            this.fortuneEnabled = fortuneEnabled;
            this.experienceMultiplier = experienceMultiplier;
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
    }

    private static class ProfileSettings {
        private final Map<String, RankSettings> rankSettings;
        private final List<String> rankOrder;

        private ProfileSettings(Map<String, RankSettings> rankSettings, List<String> rankOrder) {
            this.rankSettings = rankSettings == null ? Collections.emptyMap() : rankSettings;
            this.rankOrder = rankOrder == null ? Collections.emptyList() : rankOrder;
        }
    }
}
