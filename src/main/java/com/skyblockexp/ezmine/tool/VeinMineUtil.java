package com.skyblockexp.ezmine.tool;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Utility class for vein mining — finds all face-connected blocks of the same material
 * as the origin block using BFS (6-directional flood fill).
 */
public class VeinMineUtil {

    private static final BlockFace[] FACES = {
        BlockFace.UP, BlockFace.DOWN,
        BlockFace.NORTH, BlockFace.SOUTH,
        BlockFace.EAST, BlockFace.WEST
    };

    private VeinMineUtil() {}

    /**
     * Returns all blocks (including the origin) forming a connected vein of the same
     * material, up to {@code maxBlocks} total. Uses 6-connected BFS (face-adjacent only).
     *
     * @param origin    the block where mining started
     * @param maxBlocks maximum number of blocks to include (including origin)
     * @return list of connected same-material blocks, origin first
     */
    public static List<Block> findVein(Block origin, int maxBlocks) {
        if (maxBlocks <= 0) {
            return new ArrayList<>();
        }

        Material type = origin.getType();
        List<Block> result = new ArrayList<>();
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new ArrayDeque<>();

        visited.add(origin);
        queue.add(origin);

        while (!queue.isEmpty() && result.size() < maxBlocks) {
            Block current = queue.poll();
            result.add(current);

            for (BlockFace face : FACES) {
                Block neighbor = current.getRelative(face);
                if (!visited.contains(neighbor) && neighbor.getType() == type) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return result;
    }
}
