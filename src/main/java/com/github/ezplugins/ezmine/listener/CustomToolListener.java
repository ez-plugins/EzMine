package com.github.ezplugins.ezmine.listener;

import com.github.ezplugins.ezmine.config.CustomToolConfiguration;
import com.github.ezplugins.ezmine.config.CustomToolDefinition;
import com.github.ezplugins.ezmine.tool.CustomToolManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import com.github.ezplugins.ezmine.tool.AreaMineUtil;
import java.util.HashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class CustomToolListener implements Listener {

    private final CustomToolConfiguration configuration;
    private final CustomToolManager toolManager;
    // Track enabled/disabled state for each toggleable action per player
    private final Set<UUID> enabled3x3 = new HashSet<>();
    private final Set<UUID> enabledAutoSmelt = new HashSet<>();
    private final Set<UUID> enabledOreSearcher = new HashSet<>();
    private final Set<UUID> enabledVeinMiner = new HashSet<>();
    // Track mining direction for 3x3 tool per player (true = vertical, false = horizontal)
    private final Map<UUID, Boolean> vertical3x3 = new ConcurrentHashMap<>();

    public CustomToolListener(CustomToolConfiguration configuration, CustomToolManager toolManager) {
        this.configuration = configuration;
        this.toolManager = toolManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!this.configuration.isEnabled()) {
            return;
        }

        Action action = event.getAction();
        if (action == Action.PHYSICAL) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        CustomToolDefinition definition = this.configuration.match(item);
        if (definition == null) {
            return;
        }

        Player player = event.getPlayer();
        Set<String> actions = definition.getActions();
        boolean is3x3 = actions.stream().anyMatch(a -> a.equalsIgnoreCase("3x3"));
        boolean isAutoSmelt = actions.stream().anyMatch(a -> a.equalsIgnoreCase("auto-smelt"));
        boolean isOreSearcher = actions.stream().anyMatch(a -> a.equalsIgnoreCase("ore-searcher"));
        boolean isVeinMiner = actions.stream().anyMatch(a -> a.equalsIgnoreCase("vein-miner"));

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking() && is3x3) {
                boolean vertical = vertical3x3.getOrDefault(player.getUniqueId(), false);
                vertical3x3.put(player.getUniqueId(), !vertical);
                player.sendMessage(ChatColor.AQUA + "3x3 mining direction: "
                    + (!vertical ? ChatColor.LIGHT_PURPLE + "Vertical" : ChatColor.YELLOW + "Horizontal"));
                event.setCancelled(true);
                return;
            }
            boolean toggled = false;
            // Toggle 3x3
            if (is3x3) {
                if (enabled3x3.contains(player.getUniqueId())) {
                    enabled3x3.remove(player.getUniqueId());
                    player.sendMessage(ChatColor.YELLOW + "3x3 mining disabled.");
                } else {
                    enabled3x3.add(player.getUniqueId());
                    player.sendMessage(ChatColor.GREEN + "3x3 mining enabled.");
                }
                toggled = true;
            }
            // Toggle auto-smelt
            if (isAutoSmelt) {
                if (enabledAutoSmelt.contains(player.getUniqueId())) {
                    enabledAutoSmelt.remove(player.getUniqueId());
                    player.sendMessage(ChatColor.YELLOW + "Auto-smelt disabled.");
                } else {
                    enabledAutoSmelt.add(player.getUniqueId());
                    player.sendMessage(ChatColor.LIGHT_PURPLE
                        + "Auto-smelt enabled: all drops will be instantly smelted.");
                }
                toggled = true;
            }
            // Toggle ore-searcher
            if (isOreSearcher) {
                if (enabledOreSearcher.contains(player.getUniqueId())) {
                    enabledOreSearcher.remove(player.getUniqueId());
                    player.sendMessage(ChatColor.YELLOW + "Ore searcher disabled.");
                } else {
                    enabledOreSearcher.add(player.getUniqueId());
                    player.sendMessage(ChatColor.AQUA + "Ore searcher enabled: nearby ores will be highlighted.");
                }
                toggled = true;
            }
            // Toggle vein-miner
            if (isVeinMiner) {
                if (enabledVeinMiner.contains(player.getUniqueId())) {
                    enabledVeinMiner.remove(player.getUniqueId());
                    player.sendMessage(ChatColor.YELLOW + "Vein miner disabled.");
                } else {
                    enabledVeinMiner.add(player.getUniqueId());
                    player.sendMessage(ChatColor.GREEN + "Vein miner enabled.");
                }
                toggled = true;
            }
            if (toggled) {
                // Update enabled actions in toolManager
                Set<String> enabled = new HashSet<>();
                if (is3x3 && enabled3x3.contains(player.getUniqueId())) {
                    enabled.add("3x3");
                }
                if (isAutoSmelt && enabledAutoSmelt.contains(player.getUniqueId())) {
                    enabled.add("auto-smelt");
                }
                if (isOreSearcher && enabledOreSearcher.contains(player.getUniqueId())) {
                    enabled.add("ore-searcher");
                }
                if (isVeinMiner && enabledVeinMiner.contains(player.getUniqueId())) {
                    enabled.add("vein-miner");
                }
                this.toolManager.setActiveActions(player.getUniqueId(), enabled);
                event.setCancelled(true);
                return;
            }
        }

        // On normal interact, just show actions and set all as enabled by default
        this.toolManager.setActiveTool(player.getUniqueId(), definition);
        if (!actions.isEmpty()) {
            List<String> labels = new ArrayList<>(actions);
            Collections.sort(labels);
            player.sendMessage(ChatColor.GOLD + "EzMine custom tool actions: "
                + ChatColor.AQUA + String.join(ChatColor.GRAY + ", " + ChatColor.AQUA, labels));
            if (isAutoSmelt && enabledAutoSmelt.contains(player.getUniqueId())) {
                player.sendMessage(ChatColor.LIGHT_PURPLE
                    + "Auto-smelt is active: all drops will be instantly smelted.");
            }
            if (isOreSearcher && enabledOreSearcher.contains(player.getUniqueId())) {
                player.sendMessage(ChatColor.AQUA + "Ore searcher is active: nearby ores will be highlighted.");
            }
            if (isVeinMiner && enabledVeinMiner.contains(player.getUniqueId())) {
                player.sendMessage(ChatColor.GREEN
                    + "Vein miner is active: connected ores will be mined together.");
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "EzMine custom tool ready.");
        }
    }


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        CustomToolDefinition definition = this.configuration.match(item);
        if (definition == null) {
            return;
        }
        Set<String> actions = definition.getActions();
        boolean is3x3 = actions.stream().anyMatch(a -> a.equalsIgnoreCase("3x3"));
        if (is3x3 && enabled3x3.contains(player.getUniqueId())) {
            Block center = event.getBlock();
            boolean vertical = vertical3x3.getOrDefault(player.getUniqueId(), false);
            BlockFace face = vertical ? BlockFace.NORTH : BlockFace.UP;
            AreaMineUtil.mine3x3(player, center, face, item);
        }
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (!this.configuration.isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        CustomToolDefinition definition = this.configuration.match(newItem);
        if (definition == null) {
            this.toolManager.clear(player.getUniqueId());
            enabled3x3.remove(player.getUniqueId());
            enabledAutoSmelt.remove(player.getUniqueId());
            enabledOreSearcher.remove(player.getUniqueId());
            enabledVeinMiner.remove(player.getUniqueId());
            vertical3x3.remove(player.getUniqueId());
        } else {
            // On tool switch, enable all actions by default
            Set<String> actions = definition.getActions();
            if (actions.contains("3x3")) {
                enabled3x3.add(player.getUniqueId());
            } else {
                enabled3x3.remove(player.getUniqueId());
            }
            if (actions.contains("auto-smelt")) {
                enabledAutoSmelt.add(player.getUniqueId());
            } else {
                enabledAutoSmelt.remove(player.getUniqueId());
            }
            if (actions.contains("ore-searcher")) {
                enabledOreSearcher.add(player.getUniqueId());
            } else {
                enabledOreSearcher.remove(player.getUniqueId());
            }
            if (actions.contains("vein-miner")) {
                enabledVeinMiner.add(player.getUniqueId());
            } else {
                enabledVeinMiner.remove(player.getUniqueId());
            }
            this.toolManager.setActiveActions(player.getUniqueId(), actions);
            if (actions.contains("auto-smelt")) {
                player.sendMessage(ChatColor.LIGHT_PURPLE
                    + "Auto-smelt is active: all drops will be instantly smelted.");
            }
            if (actions.contains("ore-searcher")) {
                player.sendMessage(ChatColor.AQUA + "Ore searcher is active: nearby ores will be highlighted.");
            }
            if (actions.contains("vein-miner")) {
                player.sendMessage(ChatColor.GREEN
                    + "Vein miner is active: connected ores will be mined together.");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        this.toolManager.clear(uuid);
        enabled3x3.remove(uuid);
        enabledAutoSmelt.remove(uuid);
        enabledOreSearcher.remove(uuid);
        enabledVeinMiner.remove(uuid);
        vertical3x3.remove(uuid);
    }
}
