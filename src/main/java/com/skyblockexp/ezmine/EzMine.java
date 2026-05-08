package com.skyblockexp.ezmine;

import com.skyblockexp.ezmine.command.EzMineCommand;
import com.skyblockexp.ezmine.command.PickaxeCommand;
import com.skyblockexp.ezmine.config.MineConfiguration;
import com.skyblockexp.ezmine.integration.EzSkillsIntegration;
import com.skyblockexp.ezmine.integration.LuckyPermsIntegration;
import com.skyblockexp.ezmine.integration.McMMOIntegration;
import com.skyblockexp.ezmine.integration.MetricsIntegration;
import com.skyblockexp.ezmine.integration.VaultIntegration;
import com.skyblockexp.ezmine.integration.WorldGuardIntegration;
import com.skyblockexp.ezmine.listener.CustomToolListener;
import com.skyblockexp.ezmine.listener.CustomToolShopListener;
import com.skyblockexp.ezmine.listener.RankedMiningListener;
import com.skyblockexp.ezmine.shop.CustomToolShop;
import com.skyblockexp.ezmine.tool.CustomToolManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class EzMine extends JavaPlugin {

    private MineConfiguration mineConfiguration;
    private EzSkillsIntegration ezSkillsIntegration;
    private McMMOIntegration mcMMOIntegration;
    private WorldGuardIntegration worldGuardIntegration;
    private LuckyPermsIntegration luckyPermsIntegration;
    private CustomToolManager customToolManager;
    private VaultIntegration vaultIntegration;
    private CustomToolShop customToolShop;
    private MetricsIntegration metricsIntegration;
    private FileConfiguration settingsConfiguration;
    private FileConfiguration ranksConfiguration;
    private FileConfiguration toolsConfiguration;
    private FileConfiguration ezSkillsConfiguration;
    private FileConfiguration mcMMOConfiguration;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.reloadExternalConfigurations();
        this.mineConfiguration = new MineConfiguration(this);
        this.mineConfiguration.reload(this.settingsConfiguration, this.ranksConfiguration, this.toolsConfiguration);
        this.ezSkillsIntegration = new EzSkillsIntegration(this);
        this.ezSkillsIntegration.reload(this.ezSkillsConfiguration);
        this.mcMMOIntegration = new McMMOIntegration(this);
        this.mcMMOIntegration.reload(this.mcMMOConfiguration);
        this.worldGuardIntegration = new WorldGuardIntegration(this);
        this.worldGuardIntegration.reload(this.settingsConfiguration);
        this.luckyPermsIntegration = new LuckyPermsIntegration(this);
        this.luckyPermsIntegration.reload(this.settingsConfiguration);
        this.customToolManager = new CustomToolManager();
        this.vaultIntegration = new VaultIntegration(this);
        this.vaultIntegration.reload(this.toolsConfiguration);
        this.customToolShop = new CustomToolShop(this);
        this.metricsIntegration = new MetricsIntegration();
        this.metricsIntegration.start(this);

        PluginCommand command = this.getCommand("ezmine");
        if (command != null) {
            EzMineCommand executor = new EzMineCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            this.getLogger().warning("Command 'ezmine' is missing from plugin.yml");
        }

        PluginCommand pickaxeCommand = this.getCommand("pickaxe");
        if (pickaxeCommand != null) {
            pickaxeCommand.setExecutor(new PickaxeCommand(this, this.customToolShop));
        } else {
            this.getLogger().warning("Command 'pickaxe' is missing from plugin.yml");
        }

        boolean enableCustomTools = this.mineConfiguration.getCustomToolConfiguration().isEnabled();
        var toolReloadResult = this.mineConfiguration.getCustomToolReloadResult();
        if (toolReloadResult != null && toolReloadResult.hasErrors()) {
            enableCustomTools = false;
            this.getLogger().severe("Disabling custom-tools due to configuration errors: "
                + toolReloadResult.getErrorMessages());
            if (this.customToolManager != null) {
                this.customToolManager.clearAll();
            }
        }

        if (enableCustomTools) {
            this.getServer().getPluginManager().registerEvents(
                new CustomToolListener(
                    this.mineConfiguration.getCustomToolConfiguration(),
                    this.customToolManager),
                this);
            this.getServer().getPluginManager().registerEvents(
                new CustomToolShopListener(this, this.vaultIntegration), this);
        } else {
            this.getLogger().info("Custom tools are disabled.");
        }
        this.getServer().getPluginManager().registerEvents(
            new RankedMiningListener(
                this.mineConfiguration, this.ezSkillsIntegration,
                this.mcMMOIntegration, this.worldGuardIntegration,
                this.luckyPermsIntegration, this.customToolManager),
            this);

        this.getLogger().info("EzMine enabled. Active ranks: "
            + String.join(", ", this.mineConfiguration.getRankNames(
                this.mineConfiguration.getDefaultProfileName())));
    }

    @Override
    public void onDisable() {
        this.getLogger().info("EzMine disabled.");
    }

    public MineConfiguration getMineConfiguration() {
        return this.mineConfiguration;
    }

    public void reloadMineConfiguration() {
        this.reloadConfig();
        this.reloadExternalConfigurations();
        this.mineConfiguration.reload(this.settingsConfiguration, this.ranksConfiguration, this.toolsConfiguration);
        if (this.ezSkillsIntegration != null) {
            this.ezSkillsIntegration.reload(this.ezSkillsConfiguration);
        }
        if (this.mcMMOIntegration != null) {
            this.mcMMOIntegration.reload(this.mcMMOConfiguration);
        }
        if (this.worldGuardIntegration != null) {
            this.worldGuardIntegration.reload(this.settingsConfiguration);
        }
        if (this.luckyPermsIntegration != null) {
            this.luckyPermsIntegration.reload(this.settingsConfiguration);
        }
        if (this.vaultIntegration != null) {
            this.vaultIntegration.reload(this.toolsConfiguration);
        }
        if (this.customToolManager != null) {
            this.customToolManager.clearAll();
        }
    }

    private void reloadExternalConfigurations() {
        this.settingsConfiguration = this.loadExternalConfiguration("settings", "settings.yml");
        this.ranksConfiguration = this.loadExternalConfiguration("ranks", "ranks.yml");
        this.toolsConfiguration = this.loadExternalConfiguration("tools", "tools.yml");
        this.ezSkillsConfiguration = this.loadExternalConfiguration("ezskills", "ezskills.yml");
        this.mcMMOConfiguration = this.loadExternalConfiguration("mcmmo", "mcmmo.yml");
    }

    private FileConfiguration loadExternalConfiguration(String key, String fallbackFile) {
        String configuredName = this.getConfig().getString("files." + key, fallbackFile);
        String fileName = configuredName == null || configuredName.trim().isEmpty()
            ? fallbackFile : configuredName.trim();
        File file = new File(this.getDataFolder(), fileName);
        if (!file.exists()) {
            this.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public EzSkillsIntegration getEzSkillsIntegration() {
        return this.ezSkillsIntegration;
    }

    public McMMOIntegration getMcMMOIntegration() {
        return this.mcMMOIntegration;
    }

    public WorldGuardIntegration getWorldGuardIntegration() {
        return this.worldGuardIntegration;
    }

    public LuckyPermsIntegration getLuckyPermsIntegration() {
        return this.luckyPermsIntegration;
    }

    public CustomToolManager getCustomToolManager() {
        return this.customToolManager;
    }

    public VaultIntegration getVaultIntegration() {
        return this.vaultIntegration;
    }

    public CustomToolShop getCustomToolShop() {
        return this.customToolShop;
    }
}
