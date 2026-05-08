package com.github.ezplugins.ezmine.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

public final class BukkitCompatibility {

    private static final Method MATCH_MATERIAL_WITH_FLAG;
    private static final Method MATCH_MATERIAL_SIMPLE;

    static {
        Method matchWithFlag = null;
        Method matchSimple = null;
        try {
            matchWithFlag = Material.class.getMethod("matchMaterial", String.class, boolean.class);
        } catch (NoSuchMethodException ignored) {
            // Older Bukkit versions do not expose the boolean overload.
        }
        try {
            matchSimple = Material.class.getMethod("matchMaterial", String.class);
        } catch (NoSuchMethodException ignored) {
            // Fallback to Material#getMaterial in matchMaterial method.
        }
        MATCH_MATERIAL_WITH_FLAG = matchWithFlag;
        MATCH_MATERIAL_SIMPLE = matchSimple;
    }

    private BukkitCompatibility() {
    }

    public static Material matchMaterial(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        try {
            if (MATCH_MATERIAL_WITH_FLAG != null) {
                return (Material) MATCH_MATERIAL_WITH_FLAG.invoke(null, name, false);
            }
            if (MATCH_MATERIAL_SIMPLE != null) {
                return (Material) MATCH_MATERIAL_SIMPLE.invoke(null, name);
            }
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            // Fall through to legacy resolution.
        }

        return Material.getMaterial(name);
    }

    public static ItemStack getHeldItem(Player player) {
        if (player == null) {
            return null;
        }

        Object inventory = player.getInventory();
        try {
            Method mainHand = inventory.getClass().getMethod("getItemInMainHand");
            return (ItemStack) mainHand.invoke(inventory);
        } catch (NoSuchMethodException ignored) {
            // Legacy fallback below.
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }

        try {
            Method legacyHand = player.getClass().getMethod("getItemInHand");
            return (ItemStack) legacyHand.invoke(player);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Collection<ItemStack> getDrops(Block block, ItemStack tool, Player player) {
        if (block == null) {
            return Collections.emptyList();
        }

        if (tool == null) {
            return block.getDrops();
        }

        try {
            Method dropsWithPlayer = block.getClass().getMethod("getDrops", ItemStack.class, Player.class);
            return (Collection<ItemStack>) dropsWithPlayer.invoke(block, tool, player);
        } catch (NoSuchMethodException ignored) {
            // Continue to next signature.
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return block.getDrops();
        }

        try {
            Method dropsWithTool = block.getClass().getMethod("getDrops", ItemStack.class);
            return (Collection<ItemStack>) dropsWithTool.invoke(block, tool);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return block.getDrops();
        }
    }

    public static boolean setDropItems(BlockBreakEvent event, boolean dropItems) {
        if (event == null) {
            return false;
        }

        try {
            Method setDropItems = event.getClass().getMethod("setDropItems", boolean.class);
            setDropItems.invoke(event, dropItems);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return false;
        }
    }

    public static boolean isSpectator(org.bukkit.GameMode gameMode) {
        return gameMode != null && "SPECTATOR".equals(gameMode.name());
    }
}
