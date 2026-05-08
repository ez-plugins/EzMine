---
layout: default
title: Integrations
parent: Server Owners
nav_order: 4
---

# Integrations
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## EzSkills

EzMine integrates with [EzSkills](https://github.com/ez-plugins/EzSkills) to:

- **Gate ranks** by a player's EzSkills mining skill level (`minimum-skill-level` in `ranks.yml`)
- **Award EzSkills XP** every time a player breaks a tracked block

### Setup

Add EzSkills to your server's `plugins/` folder alongside EzMine. Configure `ezskills.yml`:

```yaml
ezskills:
  enabled: true            # true / false / auto (auto-detect if EzSkills is loaded)
  skill: MINING            # EzSkills skill ID to read levels from and award XP to

  per-world:
    enabled: false
    skill-overrides:
      world: MINING        # Use a different skill in specific worlds

  experience:
    base-per-block: 4.0             # Base XP awarded per tracked block broken
    apply-rank-multiplier: true     # Multiply by the rank's experience-multiplier
    material-overrides:
      ANCIENT_DEBRIS: 12.0          # Override XP for specific blocks
      DIAMOND_ORE:    8.0
      DEEPSLATE_DIAMOND_ORE: 8.0
```

### Field reference

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `enabled` | boolean / `auto` | `auto` | `auto` enables the integration if EzSkills is loaded. |
| `skill` | string | `MINING` | The EzSkills skill ID used for level checks and XP awards. Must match a skill registered in EzSkills. |
| `per-world.enabled` | boolean | `false` | When `true`, each world can use a different EzSkills skill. |
| `per-world.skill-overrides` | map | `{}` | World name to skill ID overrides. |
| `experience.base-per-block` | double | `0.0` | Base EzSkills XP awarded per block broken. Set to `0` to disable XP awards. |
| `experience.apply-rank-multiplier` | boolean | `true` | Multiply `base-per-block` by the player's `experience-multiplier` rank setting. |
| `experience.material-overrides` | map | `{}` | Per-material XP overrides (Material name -> XP amount). Overrides `base-per-block` for that material. |

{: .note }
EzSkills 2.0+ is required. EzMine uses the direct `EzSkillsAPI` class from the
[EzSkills API module](https://github.com/ez-plugins/EzSkills). When EzSkills is not
installed, this integration is silently skipped.

---

## mcMMO

EzMine integrates with mcMMO to:

- **Gate ranks** by a player's mcMMO mining skill level
- **Award mcMMO XP** per block broken

### Setup

Configure `mcmmo.yml`:

```yaml
mcmmo:
  enabled: auto            # true / false / auto
  skill: MINING            # mcMMO skill name

  per-world:
    enabled: false
    skill-overrides:
      world: MINING

  experience:
    base-per-block: 10.0
    apply-rank-multiplier: true
    material-overrides:
      DIAMOND_ORE: 30.0
      ANCIENT_DEBRIS: 50.0
```

{: .note }
EzMine and mcMMO both listen to `BlockBreakEvent`. EzMine runs at `HIGH` priority and
cancels the vanilla drop, so mcMMO's block processing is unaffected.

---

## Vault

[Vault](https://www.spigotmc.org/resources/vault.34315/) powers the pickaxe shop. When
Vault is installed and `custom-tools.shop.vault.enabled: true` is set, players pay from
their economy balance to purchase tools.

### Setup

```yaml
custom-tools:
  shop:
    enabled: true
    currency-name: "coins"
    vault:
      enabled: true
```

Each tool in `tools.yml` then has a `cost` field:

```yaml
tools:
  diamond-pickaxe:
    cost: 1000   # Price in the Vault economy currency
```

A `cost` of `0` makes the tool free.

---

## WorldGuard

[WorldGuard](https://enginehub.org/worldguard/) lets you restrict EzMine perks to specific
regions or require players to be inside a region before perks activate.

### Setup

```yaml
settings:
  worldguard:
    enabled: true
    require-region: false     # Set true to require a region for mining perks
    allowed-regions:
      global: []
      world: []               # Only these regions grant EzMine perks in "world"
    blocked-regions:
      global: []
      world: []               # These regions never grant EzMine perks
```

Pair with `settings.profiles.regions` to route different rank profiles to different regions:

```yaml
settings:
  profiles:
    regions:
      world:
        mine_a: default
        mine_b: vip_profile
```

---

## LuckPerms

When enabled, EzMine can resolve a player's rank by their LuckPerms primary group name
instead of (or in addition to) a direct permission check.

### Setup

```yaml
settings:
  luckperms:
    enabled: true
    use-primary-group: true        # Match rank by primary LuckPerms group
    group-permission-prefix: "group."  # Prefix for node-based group resolution
```

Then in `ranks.yml`, set either (or both) of:

```yaml
ranks:
  vip:
    permission: ezmine.rank.vip    # Standard permission check
    luckperms-group: "vip"         # Match by LuckPerms primary group name
    luckperms-permission: ""
```

{: .note }
LuckPerms integration complements the standard permission system — it does not replace it.
If `luckperms.enabled` is `false` or LuckPerms is not installed, all rank resolution falls
back to standard Bukkit permissions.
