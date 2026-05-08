package com.skyblockexp.ezmine.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class CustomToolShopSettings {

    private final boolean enabled;
    private final String title;
    private final int rows;
    private final boolean vaultEnabled;
    private final String currencyName;

    public CustomToolShopSettings(boolean enabled, String title, int rows, boolean vaultEnabled, String currencyName) {
        this.enabled = enabled;
        this.title = title;
        this.rows = rows;
        this.vaultEnabled = vaultEnabled;
        this.currencyName = currencyName;
    }

    public static CustomToolShopSettings disabled() {
        return new CustomToolShopSettings(false, ChatColor.DARK_GRAY + "Pickaxe Shop", 3, false, "coins");
    }

    public static CustomToolShopSettings from(ConfigurationSection section) {
        if (section == null) {
            return new CustomToolShopSettings(true, ChatColor.DARK_GRAY + "Pickaxe Shop", 3, false, "coins");
        }

        boolean enabled = section.getBoolean("enabled", true);
        String title = section.getString("title", "&8Pickaxe Shop");
        if (title != null) {
            title = ChatColor.translateAlternateColorCodes('&', title);
        } else {
            title = ChatColor.DARK_GRAY + "Pickaxe Shop";
        }

        int rows = section.getInt("rows", 3);
        rows = Math.min(6, Math.max(1, rows));

        ConfigurationSection vaultSection = section.getConfigurationSection("vault");
        boolean vaultEnabled = vaultSection != null && vaultSection.getBoolean("enabled", false);
        String currencyName = section.getString("currency-name", "coins");
        if (currencyName == null || currencyName.trim().isEmpty()) {
            currencyName = "coins";
        }

        return new CustomToolShopSettings(enabled, title, rows, vaultEnabled, currencyName);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public String getTitle() {
        return this.title;
    }

    public int getRows() {
        return this.rows;
    }

    public boolean isVaultEnabled() {
        return this.vaultEnabled;
    }

    public String getCurrencyName() {
        return this.currencyName;
    }
}
