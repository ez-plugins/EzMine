package com.github.ezplugins.ezmine.listener;

import com.github.ezplugins.ezmine.config.MineConfiguration;
import com.github.ezplugins.ezmine.config.OreSearcherSettings;
import com.github.ezplugins.ezmine.config.VeinMinerSettings;
import com.github.ezplugins.ezmine.integration.EzSkillsIntegration;
import com.github.ezplugins.ezmine.integration.LuckyPermsIntegration;
import com.github.ezplugins.ezmine.integration.McMMOIntegration;
import com.github.ezplugins.ezmine.integration.WorldGuardIntegration;
import com.github.ezplugins.ezmine.tool.CustomToolManager;
import com.github.ezplugins.ezmine.tool.VeinMineUtil;
import com.github.ezplugins.ezmine.util.BukkitCompatibility;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RankedMiningListener implements Listener {

    private final MineConfiguration configuration;
    private final EzSkillsIntegration ezSkills;
    private final McMMOIntegration mcMMO;
    private final WorldGuardIntegration worldGuard;
    private final LuckyPermsIntegration luckyPermsIntegration;
    private final CustomToolManager customToolManager;

    public RankedMiningListener(MineConfiguration configuration,
                                EzSkillsIntegration ezSkills,
                                McMMOIntegration mcMMO,
                                WorldGuardIntegration worldGuard,
                                LuckyPermsIntegration luckyPermsIntegration,
                                CustomToolManager customToolManager) {
        this.configuration = configuration;
        this.ezSkills = ezSkills;
        this.mcMMO = mcMMO;
        this.worldGuard = worldGuard;
        this.luckyPermsIntegration = luckyPermsIntegration;
        this.customToolManager = customToolManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (this.mcMMO != null && this.mcMMO.isPluginPresent() && this.mcMMO.isFakeBlockBreakEvent(event)) {
            return;
        }

        Block block = event.getBlock();
        Material material = block.getType();
        // Only skip if not tracked and not ore-searcher action
        if (!this.configuration.isTracked(material)) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE || BukkitCompatibility.isSpectator(player.getGameMode())) {
            return;
        }

        String worldName = block.getWorld().getName();
        if (!this.configuration.isWorldEnabled(worldName)) {
            return;
        }

        boolean worldGuardEnabled = this.worldGuard != null && this.worldGuard.isEnabled();
        if (worldGuardEnabled && !this.worldGuard.isMiningAllowed(block.getLocation())) {
            return;
        }

        Collection<String> regions = this.worldGuard != null
            ? this.worldGuard.getApplicableRegions(block.getLocation())
            : Collections.emptyList();
        String profileName = this.configuration.resolveProfileName(worldName, regions);

        boolean useEzSkills = this.ezSkills != null && this.ezSkills.isEnabled();
        boolean useMcMMO = !useEzSkills && this.mcMMO != null && this.mcMMO.isEnabled();
        boolean enforceSkillRequirements = useEzSkills || useMcMMO;
        int skillLevel = 0;
        if (useEzSkills) {
            skillLevel = this.ezSkills.getSkillLevel(player, worldName);
        } else if (useMcMMO) {
            skillLevel = this.mcMMO.getSkillLevel(player, worldName);
        }

        String luckPermsGroup = this.resolveLuckPermsGroup(player);
        MineConfiguration.AppliedSettings settings = this.configuration.resolveSettings(
            player, material, skillLevel, enforceSkillRequirements, profileName, luckPermsGroup);

        Set<String> activeActions = this.customToolManager != null
            ? this.customToolManager.getActiveActions(player.getUniqueId()) : Collections.emptySet();
        boolean areaMining = activeActions.contains("3x3");
        boolean autoSmeltOverride = activeActions.contains("auto-smelt");
        boolean oreSearcher = activeActions.contains("ore-searcher");
        boolean veinMining = activeActions.contains("vein-miner");
        boolean fortuneEnabled = settings.fortuneEnabled();
        boolean applyAutoSmelt = settings.autoSmelt() || autoSmeltOverride;

        List<Block> targets = new ArrayList<>();
        targets.add(block);
        if (areaMining) {
            for (Block adjacent : this.collectAdjacentBlocks(block)) {
                if (worldGuardEnabled && !this.worldGuard.isMiningAllowed(adjacent.getLocation())) {
                    continue;
                }
                targets.add(adjacent);
            }
        }
        if (veinMining) {
            VeinMinerSettings veinSettings = this.configuration.getVeinMinerSettings();
            if (veinSettings.enabled()) {
                Set<Block> targetSet = new HashSet<>(targets);
                List<Block> vein = VeinMineUtil.findVein(block, veinSettings.maxBlocks());
                for (int i = 1; i < vein.size(); i++) {
                    Block veinBlock = vein.get(i);
                    if (targetSet.add(veinBlock)) {
                        if (worldGuardEnabled
                            && !this.worldGuard.isMiningAllowed(veinBlock.getLocation())) {
                            continue;
                        }
                        targets.add(veinBlock);
                    }
                }
            }
        }

        boolean processedPrimary = false;
        boolean cancelPrimary = false;
        for (int index = 0; index < targets.size(); index++) {
            Block target = targets.get(index);
            boolean isPrimary = index == 0;
            if (!isPrimary && worldGuardEnabled && !this.worldGuard.isMiningAllowed(target.getLocation())) {
                continue;
            }
            Material targetMaterial = target.getType();
            if (!this.configuration.isTracked(targetMaterial)) {
                if (isPrimary) {
                    return;
                }
                continue;
            }

            Collection<ItemStack> drops = this.calculateDrops(target, player, fortuneEnabled);
            if (drops.isEmpty()) {
                if (isPrimary) {
                    return;
                }
                continue;
            }

            if (isPrimary) {
                if (!BukkitCompatibility.setDropItems(event, false)) {
                    event.setCancelled(true);
                    cancelPrimary = true;
                }
                processedPrimary = true;
            }

            for (ItemStack drop : drops) {
                ItemStack processedDrop = this.processDrop(drop, targetMaterial, settings, applyAutoSmelt);
                if (processedDrop == null || processedDrop.getAmount() <= 0) {
                    continue;
                }
                target.getWorld().dropItemNaturally(target.getLocation(), processedDrop);
            }

            if (!isPrimary) {
                target.setType(Material.AIR);
            }

            if (this.ezSkills != null && this.ezSkills.hasExperienceRewards()) {
                this.ezSkills.awardExperience(player, worldName, targetMaterial, settings.experienceMultiplier());
            } else if (this.mcMMO != null && this.mcMMO.hasExperienceRewards()) {
                this.mcMMO.awardExperience(player, worldName, targetMaterial, settings.experienceMultiplier());
            }
        }

        if (oreSearcher) {
            // Always show ore search effect when mining any tracked block
            // The effect will search for ores around the broken block, regardless of what was mined
            this.spawnOreSearchParticles(player, block, this.configuration.getOreSearcherSettings());
        }

        if (!processedPrimary) {
            return;
        }

        int exp = event.getExpToDrop();
        int adjustedExp = (int) Math.max(0, Math.round(exp * settings.experienceMultiplier()));
        if (cancelPrimary) {
            block.setType(Material.AIR);
            if (adjustedExp > 0) {
                player.giveExp(adjustedExp);
            }
            return;
        }
        event.setExpToDrop(adjustedExp);
    }

    private Collection<ItemStack> calculateDrops(Block block, Player player, boolean fortuneEnabled) {
        ItemStack tool = BukkitCompatibility.getHeldItem(player);
        ItemStack effectiveTool = tool;
        if (!fortuneEnabled && tool != null && tool.getType() != Material.AIR) {
            effectiveTool = this.removeFortune(tool);
        }

        Collection<ItemStack> drops = BukkitCompatibility.getDrops(block, effectiveTool, player);

        if (drops == null || drops.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(drops);
    }

    private ItemStack removeFortune(ItemStack tool) {
        ItemStack clone = tool.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta != null && meta.hasEnchants() && meta.hasEnchant(Enchantment.FORTUNE)) {
            meta.removeEnchant(Enchantment.FORTUNE);
            clone.setItemMeta(meta);
        } else {
            clone.removeEnchantment(Enchantment.FORTUNE);
        }
        return clone;
    }

    private ItemStack processDrop(
            ItemStack originalDrop, Material sourceMaterial,
            MineConfiguration.AppliedSettings settings, boolean autoSmeltOverride) {
        if (originalDrop == null) {
            return null;
        }

        ItemStack clone = originalDrop.clone();
        int adjustedAmount = (int) Math.max(0, Math.round(clone.getAmount() * settings.dropMultiplier()));
        if (adjustedAmount <= 0) {
            return null;
        }
        clone.setAmount(adjustedAmount);

        if (settings.autoSmelt() || autoSmeltOverride) {
            Material smeltedType = this.configuration.resolveAutoSmeltResult(clone.getType(), sourceMaterial);
            if (smeltedType != null) {
                clone.setType(smeltedType);
            }
        }

        return clone;
    }

    private List<Block> collectAdjacentBlocks(Block origin) {
        List<Block> blocks = new ArrayList<>(8);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                blocks.add(origin.getRelative(dx, 0, dz));
            }
        }
        return blocks;
    }

    private void spawnOreSearchParticles(Player player, Block origin, OreSearcherSettings settings) {
        if (settings == null || !settings.enabled()) {
            return;
        }

        int range = settings.range();
        List<Location> matches = new ArrayList<>();
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    Block target = origin.getRelative(dx, dy, dz);
                    if (settings.isTarget(target.getType())) {
                        matches.add(target.getLocation().add(0.5D, 0.5D, 0.5D));
                    }
                }
            }
        }

        if (matches.isEmpty()) {
            return;
        }

        // Use the center of the broken block as the start
        Location startLocation = origin.getLocation().add(0.5D, 0.5D, 0.5D);
        matches.sort(Comparator.comparingDouble(location -> location.distanceSquared(startLocation)));
        int limit = Math.min(settings.maxResults(), matches.size());

        Particle particle = settings.particle();
        int particleCount = settings.particleCount();
        for (int i = 0; i < limit; i++) {
            Location target = matches.get(i);
            Vector direction = target.toVector().subtract(startLocation.toVector());
            double length = direction.length();
            if (length < 0.01D) {
                continue;
            }
            direction.normalize();

            // Animate particles from the broken block to the ore
            double step = 0.3D;
            for (double traveled = 0; traveled < length; traveled += step) {
                Location particleLoc = startLocation.clone().add(direction.clone().multiply(traveled));
                // Optionally, skip if inside a solid block (for visibility)
                if (!particleLoc.getBlock().getType().isSolid() || particleLoc.getBlock().equals(origin)) {
                    origin.getWorld().spawnParticle(particle, particleLoc, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
            // Play a sound at the ore location to help the player find it
            target.getWorld().playSound(target, org.bukkit.Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 2.0f);
        }
    }

    private String resolveLuckPermsGroup(Player player) {
        if (this.luckyPermsIntegration == null || !this.luckyPermsIntegration.isEnabled()) {
            return null;
        }
        return this.luckyPermsIntegration.getPrimaryGroup(player).orElse(null);
    }
}
