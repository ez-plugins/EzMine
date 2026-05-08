package com.github.ezplugins.ezmine.listener;

import com.github.ezplugins.ezmine.EzMine;
import com.github.ezplugins.ezmine.config.CustomToolConfiguration;
import com.github.ezplugins.ezmine.config.CustomToolDefinition;
import com.github.ezplugins.ezmine.config.CustomToolShopSettings;
import com.github.ezplugins.ezmine.integration.VaultIntegration;
import com.github.ezplugins.ezmine.shop.CustomToolShopHolder;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.Map;

public class CustomToolShopListener implements Listener {

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,##0.##");
    private final EzMine plugin;
    private final VaultIntegration vaultIntegration;

    public CustomToolShopListener(EzMine plugin, VaultIntegration vaultIntegration) {
        this.plugin = plugin;
        this.vaultIntegration = vaultIntegration;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CustomToolShopHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (event.getClickedInventory() == null
            || event.getClickedInventory().getHolder() != event.getInventory().getHolder()) {
            return;
        }

        CustomToolShopHolder holder = (CustomToolShopHolder) event.getInventory().getHolder();
        Map<Integer, String> toolSlots = holder.getToolSlots();
        String toolId = toolSlots.get(event.getSlot());
        if (toolId == null) {
            return;
        }

        CustomToolConfiguration configuration = this.plugin.getMineConfiguration().getCustomToolConfiguration();
        CustomToolShopSettings settings = configuration.getShopSettings();
        if (!configuration.isEnabled() || !settings.isEnabled()) {
            return;
        }

        CustomToolDefinition definition = configuration.getTool(toolId);
        if (definition == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        double cost = definition.getShopCost();
        if (cost > 0.0D) {
            if (!settings.isVaultEnabled() || !this.vaultIntegration.isEnabled()) {
                player.sendMessage(ChatColor.RED + "Vault economy is required to buy this tool.");
                return;
            }

            Economy economy = this.vaultIntegration.getEconomy();
            if (economy == null) {
                player.sendMessage(ChatColor.RED + "No economy provider is available.");
                return;
            }

            if (!economy.has(player, cost)) {
                player.sendMessage(ChatColor.RED + "You do not have enough funds (" + formatCost(cost) + " required).");
                return;
            }

            EconomyResponse response = economy.withdrawPlayer(player, cost);
            if (!response.transactionSuccess()) {
                player.sendMessage(ChatColor.RED + "Failed to process payment. Please try again.");
                return;
            }

            player.sendMessage(ChatColor.YELLOW + "Charged " + ChatColor.GOLD
                + formatCost(cost) + " " + settings.getCurrencyName() + ".");
        }

        ItemStack tool = definition.createItemStack(this.plugin.getMineConfiguration().getCustomTextureSettings());
        Map<Integer, ItemStack> remaining = player.getInventory().addItem(tool);
        if (!remaining.isEmpty()) {
            remaining.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }

        player.sendMessage(ChatColor.GREEN + "You received the EzMine tool '" + definition.getId() + "'.");
    }

    private String formatCost(double cost) {
        return PRICE_FORMAT.format(cost);
    }
}
