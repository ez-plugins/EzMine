package com.github.ezplugins.ezmine.integration;

import com.github.ezplugins.ezmine.EzMine;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultIntegration {

    private final EzMine plugin;
    private Economy economy;
    private boolean enabled;

    public VaultIntegration(EzMine plugin) {
        this.plugin = plugin;
    }

    public void reload(FileConfiguration toolsConfiguration) {
        this.enabled = toolsConfiguration.getBoolean("custom-tools.shop.vault.enabled", false);
        this.economy = null;

        if (!this.enabled) {
            return;
        }

        Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (vaultPlugin == null || !vaultPlugin.isEnabled()) {
            this.plugin.getLogger().warning("Vault shop costs are enabled, but Vault is not installed.");
            return;
        }

        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            this.plugin.getLogger().warning("Vault shop costs are enabled, but no economy provider was found.");
            return;
        }

        this.economy = provider.getProvider();
    }

    public boolean isEnabled() {
        return this.enabled && this.economy != null;
    }

    public Economy getEconomy() {
        return this.economy;
    }
}
