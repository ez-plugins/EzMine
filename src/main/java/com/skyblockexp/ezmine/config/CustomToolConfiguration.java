package com.skyblockexp.ezmine.config;

import com.skyblockexp.ezmine.EzMine;
import com.skyblockexp.ezmine.util.BukkitCompatibility;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class CustomToolConfiguration {

    private final EzMine plugin;
    private boolean enabled;
    private Map<String, CustomToolDefinition> tools;
    private Map<Material, List<CustomToolDefinition>> toolsByMaterial;
    private CustomToolShopSettings shopSettings;

    public CustomToolConfiguration(EzMine plugin) {
        this.plugin = plugin;
        this.enabled = false;
        this.tools = Collections.emptyMap();
        this.toolsByMaterial = Collections.emptyMap();
        this.shopSettings = CustomToolShopSettings.disabled();
    }

    public ConfigurationReloadResult reload(FileConfiguration configuration) {
        ConfigurationReloadResult result = new ConfigurationReloadResult();
        ConfigurationSection section = configuration.getConfigurationSection("custom-tools");
        if (section == null || !section.getBoolean("enabled", false)) {
            this.enabled = false;
            this.tools = Collections.emptyMap();
            this.toolsByMaterial = Collections.emptyMap();
            this.shopSettings = CustomToolShopSettings.disabled();
            result.addSuccess("custom-tools.disabled or missing");
            return result;
        }

        ConfigurationSection toolsSection = section.getConfigurationSection("tools");
        if (toolsSection == null) {
            this.enabled = false;
            this.tools = Collections.emptyMap();
            this.toolsByMaterial = Collections.emptyMap();
            this.shopSettings = CustomToolShopSettings.disabled();
            String msg = "custom-tools.enabled is true but no tools are defined.";
            this.plugin.getLogger().warning(msg);
            result.addError(msg);
            return result;
        }

        this.shopSettings = CustomToolShopSettings.from(section.getConfigurationSection("shop"));

        Map<String, List<String>> actionGroups =
            this.parseActionGroups(section.getConfigurationSection("actions"), result);
        Map<String, CustomToolDefinition> parsedTools = new LinkedHashMap<>();
        Map<Material, List<CustomToolDefinition>> byMaterial = new HashMap<>();
        Set<String> seenIds = new HashSet<>();

        for (String key : toolsSection.getKeys(false)) {
            ConfigurationSection toolSection = toolsSection.getConfigurationSection(key);
            if (toolSection == null) {
                continue;
            }

            String normalizedId = key.toLowerCase(Locale.ROOT);
            if (!seenIds.add(normalizedId)) {
                String msg = "Duplicate custom tool id after normalization: "
                    + normalizedId + " (original: " + key + ")";
                this.plugin.getLogger().warning(msg);
                result.addError(msg);
                continue;
            }

            String materialKey = toolSection.getString("material");
            if (materialKey == null || materialKey.trim().isEmpty()) {
                String msg = "Missing material for custom tool " + key;
                this.plugin.getLogger().log(Level.WARNING, msg);
                result.addWarning(msg);
                continue;
            }

            Material material = BukkitCompatibility.matchMaterial(materialKey);
            if (material == null) {
                String msg = "Unknown material " + materialKey + " for custom tool " + key;
                this.plugin.getLogger().log(Level.WARNING, msg);
                result.addWarning(msg);
                continue;
            }

            String displayName = toolSection.getString("name");
            if (displayName != null && !displayName.trim().isEmpty()) {
                displayName = ChatColor.translateAlternateColorCodes('&', displayName);
            }

            List<String> lore = new ArrayList<>();
            for (String line : toolSection.getStringList("lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }

            Integer customModelData = null;
            if (toolSection.contains("custom-model-data")) {
                try {
                    int configuredData = toolSection.getInt("custom-model-data");
                    if (configuredData < 0) {
                        String msg = "custom-model-data must be zero or positive for custom tool " + key;
                        this.plugin.getLogger().log(Level.WARNING, msg);
                        result.addWarning(msg);
                    } else {
                        customModelData = configuredData;
                    }
                } catch (Exception ex) {
                    String msg = "Invalid custom-model-data for custom tool " + key + ": must be an integer";
                    this.plugin.getLogger().log(Level.WARNING, msg);
                    result.addWarning(msg);
                }
            }

            Set<String> actions = this.resolveActions(toolSection.getStringList("actions"), actionGroups, result);

            ConfigurationSection shopSection = toolSection.getConfigurationSection("shop");
            boolean shopVisible = shopSection == null || shopSection.getBoolean("visible", true);
            int shopSlot = shopSection != null ? shopSection.getInt("slot", -1) : -1;
            double shopCost = shopSection != null ? shopSection.getDouble("cost", 0.0D) : 0.0D;

            // validate shop fields
            int maxSlots = this.shopSettings.getRows() * 9;
            if (shopSlot < -1 || shopSlot >= maxSlots) {
                String msg = "shop.slot out of range for custom tool " + key
                + ". slot=" + shopSlot + " max=" + (maxSlots - 1);
                this.plugin.getLogger().warning(msg);
                result.addWarning(msg);
                shopSlot = -1; // allow auto-resolve
            }
            if (shopCost < 0.0D) {
                String msg = "shop.cost negative for custom tool " + key + ". Coercing to 0";
                this.plugin.getLogger().warning(msg);
                result.addWarning(msg);
                shopCost = 0.0D;
            }

            // validate expanded actions against allowed set
            for (String action : new HashSet<>(actions)) {
                if (!AllowedActions.isAllowed(action)) {
                    String msg = "Unknown action '" + action + "' for custom tool " + key;
                    this.plugin.getLogger().warning(msg);
                    result.addWarning(msg);
                }
            }

            // warn if material is not a pickaxe-type
            if (!material.name().endsWith("_PICKAXE")) {
                String msg = "Material for custom tool " + key + " is not a pickaxe: " + material.name();
                this.plugin.getLogger().warning(msg);
                result.addWarning(msg);
            }

            CustomToolDefinition definition = new CustomToolDefinition(
                normalizedId,
                material,
                displayName,
                lore,
                customModelData,
                actions,
                shopVisible,
                shopSlot,
                shopCost
            );

            // per-definition validation
            ConfigurationReloadResult defResult = definition.validate();
            if (defResult != null) {
                result.merge(defResult);
                if (defResult.hasErrors()) {
                    result.addError("Definition invalid for custom tool " + normalizedId + ", skipping");
                    continue;
                }
            }

            parsedTools.put(definition.getId(), definition);
            byMaterial.computeIfAbsent(material, unused -> new ArrayList<>()).add(definition);
        }

        this.enabled = !parsedTools.isEmpty();
        if (!this.enabled) {
            this.tools = Collections.emptyMap();
            this.toolsByMaterial = Collections.emptyMap();
            this.shopSettings = CustomToolShopSettings.disabled();
            result.addWarning("No valid custom tools parsed; disabling custom-tools");
            return result;
        }

        this.tools = Collections.unmodifiableMap(parsedTools);

        Map<Material, List<CustomToolDefinition>> materialIndex = new HashMap<>();
        for (Map.Entry<Material, List<CustomToolDefinition>> entry : byMaterial.entrySet()) {
            materialIndex.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        this.toolsByMaterial = Collections.unmodifiableMap(materialIndex);
        result.addSuccess("Loaded " + this.tools.size() + " custom tool(s)");
        return result;
    }

    private Map<String, List<String>> parseActionGroups(
            ConfigurationSection actionsSection, ConfigurationReloadResult result) {
        if (actionsSection == null) {
            return Collections.emptyMap();
        }

        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (String key : actionsSection.getKeys(false)) {
            List<String> actions = new ArrayList<>();
            ConfigurationSection groupSection = actionsSection.getConfigurationSection(key);
            if (groupSection != null) {
                actions.addAll(groupSection.getStringList("actions"));
            } else {
                actions.addAll(actionsSection.getStringList(key));
            }

            List<String> cleaned = new ArrayList<>();
            for (String action : actions) {
                if (action != null && !action.trim().isEmpty()) {
                    cleaned.add(action.trim());
                }
            }

            if (cleaned.isEmpty()) {
                String msg = "Custom action group " + key + " has no actions.";
                this.plugin.getLogger().log(Level.WARNING, msg);
                result.addWarning(msg);
                continue;
            }

            groups.put(key.toLowerCase(Locale.ROOT), cleaned);
        }
        return groups.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(groups);
    }

    private Set<String> resolveActions(
            List<String> configuredActions, Map<String, List<String>> actionGroups,
            ConfigurationReloadResult result) {
        if (configuredActions == null || configuredActions.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> actions = new HashSet<>();
        Set<String> visiting = new HashSet<>();
        for (String action : configuredActions) {
            this.resolveActionEntry(action, actionGroups, actions, visiting, result);
        }
        return actions;
    }

    private void resolveActionEntry(String entry,
                                    Map<String, List<String>> actionGroups,
                                    Set<String> actions,
                                    Set<String> visiting,
                                    ConfigurationReloadResult result) {
        if (entry == null || entry.trim().isEmpty()) {
            return;
        }

        String trimmed = entry.trim();
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        String groupName = null;
        if (normalized.startsWith("@")) {
            groupName = normalized.substring(1);
        } else if (normalized.startsWith("group:")) {
            groupName = normalized.substring("group:".length());
        }

        if (groupName != null && !groupName.isEmpty()) {
            List<String> groupActions = actionGroups.get(groupName);
            if (groupActions == null) {
                String msg = "Unknown custom action group referenced: " + groupName;
                this.plugin.getLogger().log(Level.WARNING, msg);
                result.addWarning(msg);
                return;
            }
            if (!visiting.add(groupName)) {
                String msg = "Detected recursive custom action group: " + groupName;
                this.plugin.getLogger().log(Level.WARNING, msg);
                result.addWarning(msg);
                return;
            }
            for (String action : groupActions) {
                this.resolveActionEntry(action, actionGroups, actions, visiting, result);
            }
            visiting.remove(groupName);
            return;
        }

        actions.add(normalized);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public CustomToolDefinition getTool(String id) {
        if (!this.enabled || id == null) {
            return null;
        }
        return this.tools.get(id.toLowerCase(Locale.ROOT));
    }

    public Map<String, CustomToolDefinition> getTools() {
        return this.tools;
    }

    public CustomToolShopSettings getShopSettings() {
        return this.shopSettings;
    }

    public CustomToolDefinition match(ItemStack itemStack) {
        if (!this.enabled || itemStack == null) {
            return null;
        }
        List<CustomToolDefinition> candidates = this.toolsByMaterial.get(itemStack.getType());
        if (candidates == null) {
            return null;
        }
        for (CustomToolDefinition definition : candidates) {
            if (definition.matches(itemStack)) {
                return definition;
            }
        }
        return null;
    }
}
