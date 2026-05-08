---
layout: default
title: Configuration
parent: Server Owners
nav_order: 2
---

# Configuration
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## config.yml

The top-level `config.yml` acts as a file registry that tells EzMine where to find each
configuration section. You can point any entry to a custom file path relative to the
EzMine data folder.

```yaml
files:
  settings: settings.yml   # Block tracking, auto-smelt, world toggles, WorldGuard, LuckPerms
  ranks:    ranks.yml       # Rank definitions and per-block overrides
  tools:    tools.yml       # Custom tool definitions and the pickaxe shop
  ezskills: ezskills.yml    # EzSkills integration and XP settings
  mcmmo:    mcmmo.yml       # mcMMO integration and XP settings
```

---

## settings.yml

### Custom textures

Enables custom model data on EzMine items when the server uses a resource pack.

```yaml
settings:
  custom-textures:
    enabled: false
    resource-pack-url: ""
    item-textures: {}       # Material -> custom-model-data (integer or string key)
```

### Rank order

The order in which ranks are evaluated. The **first** rank a player has permission for
is used. Place more restrictive ranks above less restrictive ones.

```yaml
settings:
  rank-order:
    - default
    - vip
    - elite
```

{: .important }
`default` should always be last (or have an empty permission string) so every player
receives at least a baseline rank.

### World toggles

Enable or disable EzMine on a per-world basis.

```yaml
settings:
  worlds:
    default-enabled: true   # Used for worlds not in the lists below
    enabled: []             # If set, only these worlds use EzMine
    disabled: []            # These worlds are always excluded
```

### Profiles

Map worlds or WorldGuard regions to named rank profiles (defined in `ranks.yml`).
This allows different rank tiers in different areas — for example, a harder prison mine
profile or a creative-world profile with no perks.

```yaml
settings:
  profiles:
    default: default        # Profile used when no override matches
    worlds:
      world: default
    regions:
      global: {}            # region-name -> profile-name (all worlds)
      world: {}             # region-name -> profile-name (specific world)
```

### WorldGuard

```yaml
settings:
  worldguard:
    enabled: true
    require-region: false         # Require players to be inside a region to mine
    minimum-priority: 0           # Ignore regions below this priority
    allowed-regions:
      global: []
      world: []
    blocked-regions:
      global: []
      world: []
```

### LuckPerms

When enabled, EzMine resolves a player's rank by their LuckPerms primary group or
a configurable group permission prefix, in addition to the standard permission check.

```yaml
settings:
  luckperms:
    enabled: false
    use-primary-group: true         # Use LuckPerms primary group name
    group-permission-prefix: "group."  # Prefix for group-based permission nodes
```

### Ore searcher action

Configures the built-in ore searcher that highlights nearby ores when a player mines
a tracked block.

```yaml
settings:
  actions:
    ore-searcher:
      enabled: true
      range: 8              # Search radius in blocks
      max-results: 6        # Maximum ore hints shown at once
      particle: VILLAGER_HAPPY
      particle-count: 3
      particle-distance: 1.5  # Distance from the player's eyes
      targets:              # Materials that trigger the hint
        - DIAMOND_ORE
        - DEEPSLATE_DIAMOND_ORE
        # … add any Material enum name
```

### Tracked blocks

Restrict EzMine to a specific set of materials. Only blocks in this list will have
drop multipliers, auto-smelt, and XP rewards applied.

```yaml
settings:
  tracked-blocks:
    - "*"         # Wildcard — track every block
    # Or list specific materials:
    # - DIAMOND_ORE
    # - DEEPSLATE_DIAMOND_ORE
```

### Auto-smelt

```yaml
settings:
  auto-smelt-use-defaults: true   # Merge EzMine's built-in smelt conversions
  auto-smelt-results:             # Additional or override conversions
    IRON_ORE: IRON_INGOT
    GOLD_ORE: GOLD_INGOT
```

{: .note }
Set `auto-smelt-use-defaults: false` to manage the full list yourself and disable all
built-in conversions.

---

## ranks.yml

See the [Ranks](ranks.md) page for the full reference.

---

## tools.yml

See the [Custom Tools](custom-tools.md) page for the full reference.

---

## ezskills.yml / mcmmo.yml

See the [Integrations](integrations.md) page for the full reference.

---

## Applying changes

Run `/ezmine reload` in-game to reload all configuration files without restarting the server.
