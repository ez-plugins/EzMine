package com.skyblockexp.ezmine.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class WorldSettings {

    private final boolean defaultEnabled;
    private final Set<String> enabledWorlds;
    private final Set<String> disabledWorlds;
    private final String defaultProfile;
    private final Map<String, String> worldProfiles;
    private final Map<String, Map<String, String>> regionProfiles;

    private WorldSettings(boolean defaultEnabled,
                          Set<String> enabledWorlds,
                          Set<String> disabledWorlds,
                          String defaultProfile,
                          Map<String, String> worldProfiles,
                          Map<String, Map<String, String>> regionProfiles) {
        this.defaultEnabled = defaultEnabled;
        this.enabledWorlds = enabledWorlds == null ? Collections.emptySet() : Collections.unmodifiableSet(enabledWorlds);
        this.disabledWorlds = disabledWorlds == null ? Collections.emptySet() : Collections.unmodifiableSet(disabledWorlds);
        this.defaultProfile = defaultProfile == null ? "default" : defaultProfile;
        this.worldProfiles = worldProfiles == null ? Collections.emptyMap() : Collections.unmodifiableMap(worldProfiles);
        this.regionProfiles = regionProfiles == null ? Collections.emptyMap() : Collections.unmodifiableMap(regionProfiles);
    }

    public static WorldSettings createDefault() {
        return new WorldSettings(true, Collections.emptySet(), Collections.emptySet(), "default", Collections.emptyMap(), Collections.emptyMap());
    }

    public static WorldSettings fromConfiguration(FileConfiguration configuration) {
        boolean defaultEnabled = true;
        Set<String> enabledWorlds = new HashSet<>();
        Set<String> disabledWorlds = new HashSet<>();
        String defaultProfile = "default";
        Map<String, String> worldProfiles = new HashMap<>();
        Map<String, Map<String, String>> regionProfiles = new HashMap<>();

        if (configuration != null) {
            ConfigurationSection worldsSection = configuration.getConfigurationSection("settings.worlds");
            if (worldsSection != null) {
                defaultEnabled = worldsSection.getBoolean("default-enabled", defaultEnabled);
                enabledWorlds = toLowercaseSet(worldsSection.getStringList("enabled"));
                disabledWorlds = toLowercaseSet(worldsSection.getStringList("disabled"));
            }

            ConfigurationSection profilesSection = configuration.getConfigurationSection("settings.profiles");
            if (profilesSection != null) {
                String configuredDefault = profilesSection.getString("default");
                if (configuredDefault != null && !configuredDefault.trim().isEmpty()) {
                    defaultProfile = configuredDefault.trim().toLowerCase(Locale.ROOT);
                }

                ConfigurationSection worldProfilesSection = profilesSection.getConfigurationSection("worlds");
                if (worldProfilesSection != null) {
                    for (String worldName : worldProfilesSection.getKeys(false)) {
                        String profileName = worldProfilesSection.getString(worldName);
                        if (profileName != null && !profileName.trim().isEmpty()) {
                            worldProfiles.put(worldName.toLowerCase(Locale.ROOT), profileName.trim().toLowerCase(Locale.ROOT));
                        }
                    }
                }

                ConfigurationSection regionsSection = profilesSection.getConfigurationSection("regions");
                if (regionsSection != null) {
                    for (String worldKey : regionsSection.getKeys(false)) {
                        ConfigurationSection regionSection = regionsSection.getConfigurationSection(worldKey);
                        if (regionSection == null) {
                            continue;
                        }
                        Map<String, String> mappings = new HashMap<>();
                        for (String regionId : regionSection.getKeys(false)) {
                            String profileName = regionSection.getString(regionId);
                            if (profileName != null && !profileName.trim().isEmpty()) {
                                mappings.put(regionId.toLowerCase(Locale.ROOT), profileName.trim().toLowerCase(Locale.ROOT));
                            }
                        }
                        if (!mappings.isEmpty()) {
                            regionProfiles.put(worldKey.toLowerCase(Locale.ROOT), mappings);
                        }
                    }
                }
            }
        }

        return new WorldSettings(defaultEnabled, enabledWorlds, disabledWorlds, defaultProfile, worldProfiles, regionProfiles);
    }

    public boolean isWorldEnabled(String worldName) {
        if (worldName == null) {
            return this.defaultEnabled;
        }
        String key = worldName.toLowerCase(Locale.ROOT);
        if (this.disabledWorlds.contains(key)) {
            return false;
        }
        if (!this.enabledWorlds.isEmpty()) {
            return this.enabledWorlds.contains(key);
        }
        return this.defaultEnabled;
    }

    public String resolveProfile(String worldName, Collection<String> regionNames) {
        if (regionNames != null && !regionNames.isEmpty()) {
            String profile = this.resolveRegionProfile(worldName, regionNames);
            if (profile != null) {
                return profile;
            }
        }

        if (worldName != null) {
            String profile = this.worldProfiles.get(worldName.toLowerCase(Locale.ROOT));
            if (profile != null) {
                return profile;
            }
        }

        return this.defaultProfile;
    }

    private String resolveRegionProfile(String worldName, Collection<String> regionNames) {
        if (regionNames == null || regionNames.isEmpty()) {
            return null;
        }

        Map<String, String> worldMappings = worldName == null ? null : this.regionProfiles.get(worldName.toLowerCase(Locale.ROOT));
        Map<String, String> globalMappings = this.regionProfiles.get("global");

        for (String regionName : regionNames) {
            String normalized = regionName.toLowerCase(Locale.ROOT);
            if (worldMappings != null) {
                String profile = worldMappings.get(normalized);
                if (profile != null) {
                    return profile;
                }
            }
            if (globalMappings != null) {
                String profile = globalMappings.get(normalized);
                if (profile != null) {
                    return profile;
                }
            }
        }

        return null;
    }

    public String getDefaultProfile() {
        return this.defaultProfile;
    }

    private static Set<String> toLowercaseSet(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet<>();
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                result.add(value.trim().toLowerCase(Locale.ROOT));
            }
        }
        return result;
    }
}
