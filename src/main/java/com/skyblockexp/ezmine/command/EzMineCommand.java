package com.skyblockexp.ezmine.command;

import com.skyblockexp.ezmine.EzMine;
import com.skyblockexp.ezmine.config.CustomToolConfiguration;
import com.skyblockexp.ezmine.config.CustomToolDefinition;
import com.skyblockexp.ezmine.config.MineConfiguration;
import com.skyblockexp.ezmine.config.CustomTextureSettings;
import com.skyblockexp.ezmine.integration.EzSkillsIntegration;
import com.skyblockexp.ezmine.integration.LuckyPermsIntegration;
import com.skyblockexp.ezmine.integration.McMMOIntegration;
import com.skyblockexp.ezmine.integration.WorldGuardIntegration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EzMineCommand implements CommandExecutor, TabCompleter {

    private final EzMine plugin;
    private static final String PERMISSION_COMMAND = "ezmine.command";
    private static final String PERMISSION_RELOAD = "ezmine.reload";
    private static final String PERMISSION_CUSTOM_TOOL = "ezmine.custom-tool";

    public EzMineCommand(EzMine plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!sender.hasPermission(PERMISSION_COMMAND)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
                return true;
            }
            sender.sendMessage(ChatColor.GOLD + "EzMine " + ChatColor.WHITE + this.plugin.getDescription().getVersion());
            if (sender instanceof Player) {
                Player player = (Player) sender;
                MineConfiguration configuration = this.plugin.getMineConfiguration();
                EzSkillsIntegration ezSkills = this.plugin.getEzSkillsIntegration();
                McMMOIntegration mcMMO = this.plugin.getMcMMOIntegration();
                WorldGuardIntegration worldGuard = this.plugin.getWorldGuardIntegration();
                LuckyPermsIntegration luckyPerms = this.plugin.getLuckyPermsIntegration();

                boolean useEzSkills = ezSkills != null && ezSkills.isEnabled();
                boolean useMcMMO = !useEzSkills && mcMMO != null && mcMMO.isEnabled();
                boolean enforceSkill = useEzSkills || useMcMMO;
                int skillLevel = 0;
                if (useEzSkills) {
                    skillLevel = ezSkills.getSkillLevel(player, player.getWorld().getName());
                } else if (useMcMMO) {
                    skillLevel = mcMMO.getSkillLevel(player, player.getWorld().getName());
                }

                List<String> regions = worldGuard != null
                    ? worldGuard.getApplicableRegions(player.getLocation())
                    : Collections.emptyList();
                String profileName = configuration.resolveProfileName(player.getWorld().getName(), regions);

                String luckPermsGroup = luckyPerms != null && luckyPerms.isEnabled()
                    ? luckyPerms.getPrimaryGroup(player).orElse(null)
                    : null;
                String rankName = configuration.resolveRankName(player, skillLevel, enforceSkill, profileName, luckPermsGroup);
                sender.sendMessage(ChatColor.GRAY + "Active rank: " + ChatColor.AQUA + rankName);
                if (enforceSkill) {
                    sender.sendMessage(ChatColor.GRAY + "Mining level: " + ChatColor.GREEN + skillLevel);
                }
                sender.sendMessage(ChatColor.GRAY + "Active profile: " + ChatColor.LIGHT_PURPLE + profileName);
            }
            sender.sendMessage(ChatColor.YELLOW + "Use /" + label + " reload to reload the configuration.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(PERMISSION_RELOAD)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
                return true;
            }
            this.plugin.reloadMineConfiguration();
            sender.sendMessage(ChatColor.GREEN + "EzMine configuration reloaded.");
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("tool")) {
            return this.handleToolCommand(sender, label, args);
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand. Try /" + label + " reload");
        return true;
    }

    private boolean handleToolCommand(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION_CUSTOM_TOOL)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return true;
        }

        CustomToolConfiguration tools = this.plugin.getMineConfiguration().getCustomToolConfiguration();
        if (tools == null || !tools.isEnabled()) {
            sender.sendMessage(ChatColor.RED + "Custom tools are disabled in the configuration.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " tool <id> [player]");
            Set<String> keys = tools.getTools().keySet();
            if (!keys.isEmpty()) {
                sender.sendMessage(ChatColor.GRAY + "Available tools: " + ChatColor.AQUA + String.join(ChatColor.GRAY + ", " + ChatColor.AQUA, keys));
            }
            return true;
        }

        String toolId = args[1].toLowerCase(Locale.ROOT);
        CustomToolDefinition definition = tools.getTool(toolId);
        if (definition == null) {
            sender.sendMessage(ChatColor.RED + "Unknown tool id '" + toolId + "'.");
            return true;
        }

        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[2] + "' is not online.");
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "You must specify a player when running this command from console.");
            return true;
        }

        // Use custom texture settings if enabled
        CustomTextureSettings textureSettings = this.plugin.getMineConfiguration().getCustomTextureSettings();
        ItemStack tool = definition.createItemStack(textureSettings);
        Map<Integer, ItemStack> remaining = target.getInventory().addItem(tool);
        if (!remaining.isEmpty()) {
            remaining.values().forEach(item -> target.getWorld().dropItemNaturally(target.getLocation(), item));
        }

        target.sendMessage(ChatColor.GREEN + "You have received the EzMine tool '" + definition.getId() + "'.");
        if (!target.equals(sender)) {
            sender.sendMessage(ChatColor.GREEN + "Gave '" + definition.getId() + "' to " + target.getName() + ".");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String prefix = args[0].toLowerCase(Locale.ROOT);
            if ("reload".startsWith(prefix) && sender.hasPermission(PERMISSION_RELOAD)) {
                completions.add("reload");
            }
            if ("tool".startsWith(prefix) && sender.hasPermission(PERMISSION_CUSTOM_TOOL)) {
                completions.add("tool");
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("tool") && sender.hasPermission(PERMISSION_CUSTOM_TOOL)) {
            CustomToolConfiguration configuration = this.plugin.getMineConfiguration().getCustomToolConfiguration();
            if (configuration != null && configuration.isEnabled()) {
                String prefix = args[1].toLowerCase(Locale.ROOT);
                List<String> completions = new ArrayList<>();
                for (String id : configuration.getTools().keySet()) {
                    if (id.startsWith(prefix)) {
                        completions.add(id);
                    }
                }
                return completions;
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("tool") && sender.hasPermission(PERMISSION_CUSTOM_TOOL)) {
            String prefix = args[2].toLowerCase(Locale.ROOT);
            List<String> completions = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    completions.add(player.getName());
                }
            }
            return completions;
        }

        return Collections.emptyList();
    }
}
