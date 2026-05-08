# EzMine

**EzMine** lets you reward dedicated players with smarter ore drops, auto-smelting, and configurable experience boosts. Whether you're running a Skyblock network or a survival SMP, EzMine turns the grind into an engaging progression system without touching vanilla block mechanics.

![Latest version](https://img.shields.io/badge/Latest%20version-1.3.0-blue)
![Minecraft version](https://img.shields.io/badge/Minecraft%20version-1.7%20to%201.21%2B-blue)
![Requires Java 17+](https://img.shields.io/badge/Requires-Java%2017+-brightgreen)

## Feature highlights
- **Permission-driven ranks**: Configure any number of mining ranks with distinct drop multipliers, experience boosts, and auto-smelt behavior. Players automatically receive the first rank they have permission for (`settings.rank-order`).
- **World/region profiles**: Route different rank perks per world or WorldGuard region using profile mappings.
- **World toggles**: Enable or disable EzMine per world so you can keep vanilla mining elsewhere.
- **Ore tracking**: Limit EzMine to specific ores and blocks via `settings.tracked-blocks` so regular blocks remain untouched.
- **Smart auto-smelt**: Convert ore drops to ingots (or custom outputs) instantly. Merge EzMine's defaults with your own recipes through `settings.auto-smelt-results`.
- **Fortune control**: Disable fortune on a per-rank or per-block basis to keep progression balanced.
- **Block overrides**: Fine-tune individual materials with custom multipliers or auto-smelt toggles while keeping global rank defaults.
- **Custom tools**: Distribute bespoke pickaxes with scripted actions like 3×3 harvesting, ore searcher hints, and forced auto-smelt, all driven from configuration.
- **Pickaxe shop**: Let players browse a GUI shop and purchase tools using optional Vault costs.
- [**EzSkills**](https://www.spigotmc.org/resources/1-7-1-21-ezskills-lightweight-rpg-skill-ability-framework.131061/) / mcMMO integration: Use external skills for rank gates and experience, with optional per-world progression overrides.

## Commands & permissions
| Command | Description | Permission |
|---------|-------------|------------|
| `/ezmine` | Show the plugin version and the caller's active rank. | `ezmine.command` (default: `true`) |
| `/ezmine reload` | Reload EzMine's configuration from disk. | `ezmine.reload` (default: `op`) |
| `/ezmine tool <id> [player]` | Give a configured custom tool to yourself or another player. | `ezmine.custom-tool` (default: `op`) |
| `/pickaxe` | Open the pickaxe shop GUI. | `ezmine.pickaxe` (default: `true`) |

Grant miners custom ranks with permission nodes like `ezmine.rank.vip` or `ezmine.rank.elite`. Players without a matching permission fall back to the `default` rank.
Need a special pickaxe? Give staff `ezmine.custom-tool` so they can distribute the tools defined under `custom-tools`.

## Configuration cheat sheet
- `settings.rank-order` — Evaluate ranks from top to bottom. The first permission a player has wins.
- `settings.worlds` — Enable/disable EzMine on specific worlds.
- `settings.profiles` — Map worlds or WorldGuard regions to rank profiles.
- `settings.actions.ore-searcher` — Configure the ore searcher action particles and targets.
- `settings.tracked-blocks` — Material list (Spigot enum names) that EzMine should manage.
- `settings.auto-smelt-use-defaults` — Merge or replace EzMine's built-in smelt conversions.
- `ranks.<name>.drop-multiplier` — Multiply the quantity of each drop.
- `ranks.<name>.experience-multiplier` — Adjust mining XP reward.
- `ranks.<name>.auto-smelt` — Toggle instant smelting for the rank.
- `ranks.<name>.fortune` — Allow or disable fortune while calculating drops.
- `ranks.<name>.block-overrides` — Per-material tweaks (drop, XP, auto-smelt, fortune) that supersede the rank defaults.
- `profiles.<name>` — Optional rank profile groupings for world/region-specific perks.
- `custom-tools` — Enable bespoke pickaxes with metadata, lore, and a list of actions each time they are activated.
- `custom-tools.shop` — Configure the pickaxe shop GUI (title, rows, Vault economy usage).
- `ezskills.per-world` — Optional per-world skill overrides for [EzSkills](https://www.spigotmc.org/resources/1-7-1-21-ezskills-lightweight-rpg-skill-ability-framework.131061/) progression.
- `mcmmo.per-world` — Optional per-world skill overrides for mcMMO progression.

## Quick start
1. **Drop EzMine.jar** into your server's `plugins` folder and restart.
2. **Assign permissions** using LuckPerms, PermissionsEx, or your favorite manager.
3. **Tweak** `config.yml` with your rank order, tracked blocks, and smelting preferences.
4. **Run** `/ezmine reload` to apply changes live.

## Integration highlights
- **Vault economy**: Power the pickaxe shop and tool upgrades with any Vault-compatible economy. Pair with [EzEconomy](https://www.spigotmc.org/resources/1-7-1-21-ezeconomy-modern-vault-economy-plugin-for-minecraft-servers.130975/) for a streamlined setup.
- **WorldGuard**: Target specific mines or regions with unique perk profiles through `settings.profiles` mappings.
- **EzSkills**: Gate ranks behind skill levels and apply per-world progression rules. Configure overrides under `ezskills.per-world`.
- **mcMMO**: Use mcMMO mining levels for rank requirements and optionally award mcMMO XP with `mcmmo.yml`.
- **Permissions managers**: LuckPerms, PermissionsEx, and other managers control rank access via `ezmine.rank.<name>` nodes.
- **LuckPerms integration**: Optionally integrate with [LuckPerms](https://luckperms.net/) for advanced group-based rank assignment. When enabled in `settings.luckperms`, EzMine can use a player's primary LuckPerms group (or a custom group permission prefix) to determine their mining rank. This allows seamless synchronization between your server's group system and EzMine's rank progression. See the `settings.luckperms` section in your configuration for options like `enabled`, `use-primary-group`, and `group-permission-prefix`.

## Prison server setup guide
Use EzMine as the backbone of your prison mine progression without rewriting your entire economy.

1. **Define mine worlds and regions**: Map each mine to a profile with `settings.profiles.worlds` or `settings.profiles.regions` so every mine can have distinct rank perks and multipliers.
2. **Create rank ladders**: Set `settings.rank-order` to match your prison ranks (A → Z, etc.). Assign permissions like `ezmine.rank.block_a`, `ezmine.rank.block_b`, and so on.
3. **Tune payouts**: Use `drop-multiplier` and `experience-multiplier` to control ore output and XP. Combine with auto-smelt to align drops with your economy pricing.
4. **Control fortune**: Disable fortune for early ranks to slow progression, then enable it later for higher tiers.
5. **Offer premium tools**: Configure `custom-tools.shop` with Vault pricing so players can buy 3×3 or auto-smelt pickaxes as upgrades.
6. **Protect vanilla worlds**: Disable EzMine in non-prison worlds using `settings.worlds` to keep survival areas untouched.

## Sample rank setup
<details>
<summary>Three-tier mining progression</summary>

```yaml
settings:
  rank-order:
    - default
    - vip
    - elite
  tracked-blocks:
    - COAL_ORE
    - DEEPSLATE_IRON_ORE
    - GOLD_ORE
    - DIAMOND_ORE
    - ANCIENT_DEBRIS
ranks:
  default:
    permission: ""
    drop-multiplier: 1.0
    experience-multiplier: 1.0
    auto-smelt: false
    fortune: true
  vip:
    permission: ezmine.rank.vip
    drop-multiplier: 1.5
    experience-multiplier: 1.25
    auto-smelt: true
    fortune: true
    block-overrides:
      DIAMOND_ORE:
        drop-multiplier: 2.0
  elite:
    permission: ezmine.rank.elite
    drop-multiplier: 2.0
    experience-multiplier: 1.5
    auto-smelt: true
    fortune: true
    block-overrides:
      ANCIENT_DEBRIS:
        drop-multiplier: 2.0
        experience-multiplier: 2.0
```
</details>

### LuckPerms setup example

If you want EzMine to use LuckPerms groups for rank assignment, enable the integration in your config:

```yaml
settings:
  luckperms:
    enabled: true
    use-primary-group: true  # Use the player's primary LuckPerms group
    group-permission-prefix: "group."  # Default prefix for group permissions
  rank-order:
    - default
    - vip
    - elite
ranks:
  default:
    permission: "group.default"
    drop-multiplier: 1.0
    experience-multiplier: 1.0
    auto-smelt: false
    fortune: true
  vip:
    permission: "group.vip"
    drop-multiplier: 1.5
    experience-multiplier: 1.25
    auto-smelt: true
    fortune: true
  elite:
    permission: "group.elite"
    drop-multiplier: 2.0
    experience-multiplier: 1.5
    auto-smelt: true
    fortune: true
```

With this setup, players will be assigned a mining rank based on their LuckPerms primary group (e.g., `default`, `vip`, or `elite`). You can customize the `group-permission-prefix` if your server uses a different group node format.

## World & region profiles
Use profiles to run different mining perks per world or WorldGuard region:

```yaml
# settings.yml
settings:
  profiles:
    default: default
    worlds:
      world_nether: nether
    regions:
      world:
        vip_mine: vip
```
```yaml
# ranks.yml
profiles:
  default:
    rank-order:
      - default
      - vip
    ranks:
      default:
        permission: ""
        drop-multiplier: 1.0
        experience-multiplier: 1.0
        auto-smelt: false
        fortune: true
      vip:
        permission: ezmine.rank.vip
        drop-multiplier: 1.5
        experience-multiplier: 1.25
        auto-smelt: true
        fortune: true
  nether:
    rank-order:
      - default
    ranks:
      default:
        permission: ""
        drop-multiplier: 1.2
        experience-multiplier: 1.2
        auto-smelt: true
        fortune: true
```

## mcMMO integration
Enable `mcmmo.yml` to use mcMMO mining levels for rank requirements and (optionally) award mcMMO experience. If both EzSkills and mcMMO are enabled, EzSkills takes priority.

```yaml
mcmmo:
  enabled: true
  skill: MINING
  per-world:
    enabled: true
    skill-overrides:
      world: MINING
      world_nether: MINING_NETHER
  experience:
    base-per-block: 2.0
    apply-rank-multiplier: true
    material-overrides:
      ANCIENT_DEBRIS: 12.0
```

## Custom tools
Define custom mining tools in `config.yml` to bundle metadata and scripted actions:

```yaml
custom-tools:
  enabled: true
  shop:
    enabled: true
    title: "&8Pickaxe Shop"
    rows: 3
    currency-name: "coins"
    vault:
      enabled: false
  tools:
    quarry-hammer:
      material: NETHERITE_PICKAXE
      name: "&bQuarry Hammer"
      lore:
        - "&7Configured hammer for EzMine."
        - "&7Right-click to activate its abilities."
      actions:
        - "3x3"
        - "auto-smelt"
        - "ore-searcher"
      shop:
        visible: true
        slot: 13
        cost: 0
```

Players right-click the configured item to activate its actions until they switch tools. Supported actions include:
- `3x3` — Break a 3×3 area centred on the target block (horizontal plane).
- `auto-smelt` — Force smelting even if the player's rank would normally drop raw ores.
- `ore-searcher` — Show hint particles pointing toward nearby ores while mining.

Distribute these tools with `/ezmine tool <id> [player]` or bundle them into starter kits.

### Configuration validation

When EzMine loads `custom-tools` it performs sanity checks and reports problems in the server log. Fatal errors (such as a missing or unknown `material`, or duplicate tool IDs after normalization) will disable `custom-tools` until you fix them.

Watch for these common messages and how to fix them:
- `Missing material for custom tool <id>` — add a valid Spigot `Material` name (e.g. `NETHERITE_PICKAXE`).
- `Unknown material <name> for custom tool <id>` — correct the material name or use a supported material.
- `Duplicate custom tool id after normalization: <id>` — ensure tool keys are unique when lowercased.
- `Unknown action '<action>' for custom tool <id>` — use a built-in action (e.g. `3x3`, `auto-smelt`, `ore-searcher`) or define it in the `actions` section.
- `custom-model-data must be zero or positive for custom tool <id>` — use a non-negative integer for `custom-model-data`.
- `shop.slot out of range for custom tool <id>` — choose a `slot` between `0` and `rows*9 - 1`.

If you see warnings (non-fatal issues), EzMine will still load the tool but the behaviour may differ. After fixing the YAML, run `/ezmine reload` to re-apply the configuration.

## Why admins love EzMine
- **Minimal setup**: Ships with sensible defaults for all major ores and auto-smelt conversions.
- **Economy friendly**: Combine rank-specific multipliers with your economy plugins to price upgrades. Pair with [EzEconomy](https://modrinth.com/plugin/ezeconomy) or any Vault-compatible economy.
- **Low overhead**: Only hooks tracked blocks and leaves everything else untouched for vanilla behavior.

Ready to bring prestige mining to your world? Install EzMine and let your players feel the climb from stone pickaxes to elite miners in minutes.

## Need help?
Have questions or feedback? Visit our SpigotMC discussion thread or [join the EzMine Discord server](https://discord.gg/yWP95XfmBS) for direct support and release updates.