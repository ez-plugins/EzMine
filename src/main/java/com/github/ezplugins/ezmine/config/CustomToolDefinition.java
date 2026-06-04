package com.github.ezplugins.ezmine.config;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CustomToolDefinition {

    private final String id;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final Integer customModelData;
    private final Set<String> actions;
    private final boolean shopVisible;
    private final int shopSlot;
    private final double shopCost;
    public CustomToolDefinition(String id,
                                Material material,
                                String displayName,
                                List<String> lore,
                                Integer customModelData,
                                Set<String> actions,
                                boolean shopVisible,
                                int shopSlot,
                                double shopCost) {
        this.id = Objects.requireNonNull(id, "id");
        this.material = Objects.requireNonNull(material, "material");
        this.displayName = displayName != null && !displayName.trim().isEmpty()
            ? displayName : null;
        this.lore = lore == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(new java.util.ArrayList<>(lore));
        this.customModelData = customModelData;
        this.actions = actions == null
            ? Collections.emptySet()
            : Collections.unmodifiableSet(new java.util.HashSet<>(actions));
        this.shopVisible = shopVisible;
        this.shopSlot = shopSlot;
        this.shopCost = Math.max(0.0D, shopCost);
    }

    public String getId() {
        return this.id;
    }

    public Material getMaterial() {
        return this.material;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public List<String> getLore() {
        return Collections.unmodifiableList(this.lore);
    }

    public Set<String> getActions() {
        return Collections.unmodifiableSet(this.actions);
    }

    public boolean isShopVisible() {
        return this.shopVisible;
    }

    public int getShopSlot() {
        return this.shopSlot;
    }

    public double getShopCost() {
        return this.shopCost;
    }

    public Integer getCustomModelData() {
        return this.customModelData;
    }

    public boolean matches(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() != this.material) {
            return false;
        }

        if (this.displayName == null && this.lore.isEmpty()) {
            return true;
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return false;
        }

        if (this.displayName != null) {
            if (!meta.hasDisplayName() || !this.displayName.equals(meta.getDisplayName())) {
                return false;
            }
        }

        if (!this.lore.isEmpty()) {
            if (!meta.hasLore()) {
                return false;
            }
            List<String> itemLore = meta.getLore();
            if (itemLore == null || itemLore.size() != this.lore.size()) {
                return false;
            }
            for (int i = 0; i < this.lore.size(); i++) {
                String expected = this.lore.get(i);
                String actual = itemLore.get(i);
                if (!Objects.equals(expected, actual)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Creates an ItemStack for this tool, applying custom model data if configured.
     * @param customTextureSettings Optional custom texture settings (may be null)
     */
    public ItemStack createItemStack(CustomTextureSettings customTextureSettings) {
        ItemStack itemStack = new ItemStack(this.material);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (this.displayName != null) {
                meta.setDisplayName(this.displayName);
            }
            if (!this.lore.isEmpty()) {
                meta.setLore(this.lore);
            }
            // Apply explicit custom model data if configured on the tool
            if (this.customModelData != null) {
                try {
                    meta.getClass().getMethod("setCustomModelData", Integer.class)
                        .invoke(meta, this.customModelData);
                } catch (Exception ignored) { }
            } else if (customTextureSettings != null && customTextureSettings.isEnabled()) {
                Object modelData = customTextureSettings.getTexture(this.material);
                if (modelData instanceof Number) {
                    try {
                        meta.getClass().getMethod("setCustomModelData", Integer.class)
                            .invoke(meta, ((Number) modelData).intValue());
                    } catch (Exception ignored) { }
                }
            }
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    /**
     * Validate this definition and return a ConfigurationReloadResult containing any warnings/errors.
     */
    public ConfigurationReloadResult validate() {
        ConfigurationReloadResult result = new ConfigurationReloadResult();
        if (this.id == null || this.id.trim().isEmpty()) {
            result.addError("Custom tool has empty id");
        }
        if (this.material == null) {
            result.addError("Custom tool " + this.id + " has null material");
        }
        if (this.shopSlot < -1) {
            result.addWarning("Custom tool " + this.id + " has invalid shop slot: " + this.shopSlot);
        }
        if (this.displayName != null && this.displayName.length() > 1024) {
            result.addWarning("Display name for custom tool " + this.id + " is very long");
        }
        for (String a : this.actions) {
            if (a == null || a.trim().isEmpty()) {
                result.addWarning("Custom tool " + this.id + " contains empty action entry");
            }
        }
        return result;
    }

    // ...existing code...
}
