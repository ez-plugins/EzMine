package com.skyblockexp.ezmine.config;


import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds EzMine custom texture configuration (resource pack URL, per-item texture keys).
 */
public class CustomTextureSettings {
    private final boolean enabled;
    private final String resourcePackUrl;
    private final Map<Material, Object> itemTextures;

    public CustomTextureSettings(boolean enabled, String resourcePackUrl, Map<Material, Object> itemTextures) {
        this.enabled = enabled;
        this.resourcePackUrl = resourcePackUrl;
        this.itemTextures = itemTextures == null ? Collections.emptyMap() : itemTextures;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getResourcePackUrl() {
        return resourcePackUrl;
    }

    /**
     * Returns the texture key or model data for a given material, or null if not set.
     */
    public Object getTexture(Material material) {
        return itemTextures.get(material);
    }

    public Map<Material, Object> getItemTextures() {
        return itemTextures;
    }

    /**
     * Loads custom texture settings from config.
     */
    public static CustomTextureSettings fromConfiguration(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("settings.custom-textures");
        if (section == null) {
            return new CustomTextureSettings(false, "", Collections.emptyMap());
        }
        boolean enabled = section.getBoolean("enabled", false);
        String url = section.getString("resource-pack-url", "");
        Map<Material, Object> textures = new HashMap<>();
        ConfigurationSection itemSection = section.getConfigurationSection("item-textures");
        if (itemSection != null) {
            for (String key : itemSection.getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if (mat == null) continue;
                Object value = itemSection.get(key);
                textures.put(mat, value);
            }
        }
        return new CustomTextureSettings(enabled, url, textures);
    }

    /**
     * Loads custom texture settings from both settings.yml and tools.yml.
     * toolsConfig takes precedence for custom tools.
     */
    public static CustomTextureSettings fromMultipleConfigurations(FileConfiguration settingsConfig, FileConfiguration toolsConfig) {
        // Load from settings.yml
        CustomTextureSettings base = fromConfiguration(settingsConfig);
        // Merge/override with tools.yml if present
        ConfigurationSection section = toolsConfig.getConfigurationSection("custom-textures");
        if (section == null) {
            return base;
        }
        boolean enabled = section.getBoolean("enabled", base.isEnabled());
        String url = section.getString("resource-pack-url", base.getResourcePackUrl());
        Map<Material, Object> textures = new HashMap<>(base.getItemTextures());
        ConfigurationSection itemSection = section.getConfigurationSection("item-textures");
        if (itemSection != null) {
            for (String key : itemSection.getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if (mat == null) continue;
                Object value = itemSection.get(key);
                textures.put(mat, value);
            }
        }
        return new CustomTextureSettings(enabled, url, textures);
    }
}
