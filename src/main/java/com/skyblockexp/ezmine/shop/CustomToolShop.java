package com.skyblockexp.ezmine.shop;

import com.skyblockexp.ezmine.config.CustomToolConfiguration;
import com.skyblockexp.ezmine.config.CustomToolDefinition;
import com.skyblockexp.ezmine.config.CustomToolShopSettings;
import com.skyblockexp.ezmine.config.CustomTextureSettings;
import com.skyblockexp.ezmine.EzMine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomToolShop {

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,##0.##");

    private final EzMine plugin;

    public CustomToolShop(EzMine plugin) {
        this.plugin = plugin;
    }

    public Inventory createInventory(CustomToolConfiguration configuration) {
        CustomToolShopSettings settings = configuration.getShopSettings();
        int size = settings.getRows() * 9;
        Map<Integer, String> slotMapping = new HashMap<>();
        CustomToolShopHolder holder = new CustomToolShopHolder(slotMapping);
        Inventory inventory = Bukkit.createInventory(holder, size, settings.getTitle());
        holder.setInventory(inventory);

        int nextSlot = 0;
        for (CustomToolDefinition definition : configuration.getTools().values()) {
            if (!definition.isShopVisible()) {
                continue;
            }

            int slot = resolveSlot(definition.getShopSlot(), size, inventory, nextSlot);
            if (slot == -1) {
                break;
            }
            nextSlot = slot + 1;

            ItemStack display = createDisplayItem(definition, settings);
            inventory.setItem(slot, display);
            slotMapping.put(slot, definition.getId());
        }

        if (slotMapping.isEmpty()) {
            return null;
        }

        return inventory;
    }

    private int resolveSlot(int preferredSlot, int size, Inventory inventory, int startSlot) {
        if (preferredSlot >= 0 && preferredSlot < size && inventory.getItem(preferredSlot) == null) {
            return preferredSlot;
        }
        for (int i = startSlot; i < size; i++) {
            if (inventory.getItem(i) == null) {
                return i;
            }
        }
        for (int i = 0; i < startSlot; i++) {
            if (inventory.getItem(i) == null) {
                return i;
            }
        }
        return -1;
    }

    private ItemStack createDisplayItem(CustomToolDefinition definition, CustomToolShopSettings settings) {
        CustomTextureSettings textureSettings = plugin.getMineConfiguration().getCustomTextureSettings();
        ItemStack itemStack = definition.createItemStack(textureSettings);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }

        List<String> lore = new ArrayList<>();
        if (meta.hasLore() && meta.getLore() != null) {
            lore.addAll(meta.getLore());
        }

        if (!lore.isEmpty()) {
            lore.add("");
        }

        double cost = definition.getShopCost();
        if (cost > 0.0D) {
            lore.add(ChatColor.YELLOW + "Cost: " + ChatColor.GOLD + formatCost(cost) + " " + settings.getCurrencyName());
        } else {
            lore.add(ChatColor.GREEN + "Free");
        }
        lore.add(ChatColor.GRAY + "Click to claim");

        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private String formatCost(double cost) {
        return PRICE_FORMAT.format(cost);
    }
}
