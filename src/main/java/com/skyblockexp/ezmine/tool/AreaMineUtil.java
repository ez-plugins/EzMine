package com.skyblockexp.ezmine.tool;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;

public class AreaMineUtil {

    private AreaMineUtil() {}

    /**
     * Mines a 3x3 area centered on the given block, oriented by the block face broken.
     * If face is UP or DOWN, mines horizontally (XZ plane). Otherwise, mines vertically (YZ or XY plane).
     */
    public static void mine3x3(Player player, Block center, BlockFace face, ItemStack tool) {
        Location loc = center.getLocation();
        int cx = loc.getBlockX();
        int cy = loc.getBlockY();
        int cz = loc.getBlockZ();

        // Default to horizontal if face is null
        if (face == null) {
            face = BlockFace.UP;
        }

        // Determine orientation
        int[][] offsets;
        if (face == BlockFace.UP || face == BlockFace.DOWN) {
            // Horizontal (XZ plane)
            offsets = new int[][] {{-1, 0, -1}, {0, 0, -1}, {1, 0, -1},
                                   {-1, 0, 0},  /*center*/  {1, 0, 0},
                                   {-1, 0, 1},  {0, 0, 1},  {1, 0, 1}};
        } else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
            // Vertical (XY plane)
            offsets = new int[][] {{-1, -1, 0}, {0, -1, 0}, {1, -1, 0},
                                   {-1, 0, 0},  /*center*/  {1, 0, 0},
                                   {-1, 1, 0},  {0, 1, 0},  {1, 1, 0}};
        } else if (face == BlockFace.EAST || face == BlockFace.WEST) {
            // Vertical (YZ plane)
            offsets = new int[][] {{0, -1, -1}, {0, -1, 0}, {0, -1, 1},
                                   {0, 0, -1},  /*center*/  {0, 0, 1},
                                   {0, 1, -1},  {0, 1, 0},  {0, 1, 1}};
        } else {
            // Fallback to horizontal
            offsets = new int[][] {{-1, 0, -1}, {0, 0, -1}, {1, 0, -1},
                                   {-1, 0, 0},  /*center*/  {1, 0, 0},
                                   {-1, 0, 1},  {0, 0, 1},  {1, 0, 1}};
        }

        for (int[] offset : offsets) {
            int dx = offset[0], dy = offset[1], dz = offset[2];
            Block b = center.getWorld().getBlockAt(cx + dx, cy + dy, cz + dz);
            if (b.equals(center)) { // skip center, already broken
                continue;
            }
            if (!b.getType().isAir() && b.getType() != Material.BEDROCK) {
                b.breakNaturally(tool);
            }
        }
    }
}
