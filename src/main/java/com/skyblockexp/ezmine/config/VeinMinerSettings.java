package com.skyblockexp.ezmine.config;

import com.skyblockexp.ezmine.EzMine;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Configuration for the vein-miner custom tool action.
 */
public class VeinMinerSettings {

    private static final int DEFAULT_MAX_BLOCKS = 64;
    private static final int ABSOLUTE_MAX_BLOCKS = 512;

    private final boolean enabled;
    private final int maxBlocks;

    private VeinMinerSettings(boolean enabled, int maxBlocks) {
        this.enabled = enabled;
        this.maxBlocks = maxBlocks;
    }

    public static VeinMinerSettings createDefault() {
        return new VeinMinerSettings(true, DEFAULT_MAX_BLOCKS);
    }

    public static VeinMinerSettings fromConfiguration(EzMine plugin, FileConfiguration configuration) {
        boolean enabled = true;
        int maxBlocks = DEFAULT_MAX_BLOCKS;

        if (configuration != null) {
            ConfigurationSection section =
                configuration.getConfigurationSection("settings.actions.vein-miner");
            if (section != null) {
                enabled = section.getBoolean("enabled", enabled);
                int configured = section.getInt("max-blocks", maxBlocks);
                maxBlocks = Math.max(1, Math.min(ABSOLUTE_MAX_BLOCKS, configured));
            }
        }

        return new VeinMinerSettings(enabled, maxBlocks);
    }

    public boolean enabled() {
        return this.enabled;
    }

    public int maxBlocks() {
        return this.maxBlocks;
    }
}
