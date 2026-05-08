package com.github.ezplugins.ezmine.command;

import com.github.ezplugins.ezmine.EzMine;
import com.github.ezplugins.ezmine.config.CustomToolConfiguration;
import com.github.ezplugins.ezmine.shop.CustomToolShop;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class PickaxeCommand implements CommandExecutor {

    private static final String PERMISSION_PICKAXE = "ezmine.pickaxe";
    private final EzMine plugin;
    private final CustomToolShop shop;

    public PickaxeCommand(EzMine plugin, CustomToolShop shop) {
        this.plugin = plugin;
        this.shop = shop;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!sender.hasPermission(PERMISSION_PICKAXE)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return true;
        }

        CustomToolConfiguration configuration = this.plugin.getMineConfiguration().getCustomToolConfiguration();
        if (configuration == null || !configuration.isEnabled() || !configuration.getShopSettings().isEnabled()) {
            sender.sendMessage(ChatColor.RED + "The pickaxe shop is currently disabled.");
            return true;
        }

        Inventory inventory = this.shop.createInventory(configuration);
        if (inventory == null) {
            sender.sendMessage(ChatColor.RED + "There are no tools available in the shop right now.");
            return true;
        }

        ((Player) sender).openInventory(inventory);
        return true;
    }
}
