package com.github.ezplugins.ezmine.shop;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Collections;
import java.util.Map;

public class CustomToolShopHolder implements InventoryHolder {

    private final Map<Integer, String> toolSlots;
    private Inventory inventory;

    public CustomToolShopHolder(Map<Integer, String> toolSlots) {
        this.toolSlots = toolSlots;
    }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Map<Integer, String> getToolSlots() {
        return Collections.unmodifiableMap(this.toolSlots);
    }
}
