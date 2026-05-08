package com.github.ezplugins.ezmine.integration;

import com.github.ezplugins.ezmine.EzMine;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Optional;

public class LuckyPermsIntegration {

    private final EzMine plugin;
    private boolean enabled;
    private boolean pluginPresent;
    private boolean usePrimaryGroup;
    private String groupPermissionPrefix;
    private LuckPerms api;

    public LuckyPermsIntegration(EzMine plugin) {
        this.plugin = plugin;
    }

    public void reload(FileConfiguration configuration) {
        this.enabled = false;
        this.pluginPresent = false;
        this.api = null;

        ConfigurationSection section = configuration.getConfigurationSection("settings.luckperms");
        boolean configuredEnabled = section != null && section.getBoolean("enabled", false);
        this.usePrimaryGroup = section == null || section.getBoolean("use-primary-group", true);
        this.groupPermissionPrefix = section == null
            ? "group." : section.getString("group-permission-prefix", "group.");

        if (!configuredEnabled) {
            return;
        }

        try {
            this.api = LuckPermsProvider.get();
        } catch (IllegalStateException ex) {
            this.plugin.getLogger().warning("LuckyPerms integration enabled but LuckyPerms is missing.");
            return;
        }

        this.pluginPresent = true;
        this.enabled = true;
        this.plugin.getLogger().info("LuckyPerms integration enabled.");
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isPluginPresent() {
        return this.pluginPresent;
    }

    public Optional<String> getPrimaryGroup(Player player) {
        if (!this.enabled || this.api == null || player == null) {
            return Optional.empty();
        }
        User user = this.api.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return Optional.empty();
        }
        String group = user.getPrimaryGroup();
        if (group == null || group.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(group);
    }

    public String getGroupPermissionPrefix() {
        return this.groupPermissionPrefix;
    }

    public boolean usePrimaryGroup() {
        return this.usePrimaryGroup;
    }
}
