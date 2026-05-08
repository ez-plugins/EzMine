---
layout: default
title: Ranks
parent: Server Owners
nav_order: 3
---

# Ranks
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Overview

Ranks are the core of EzMine's progression system. Each rank defines:

- A **permission node** that grants the rank
- An optional **minimum skill level** (EzSkills or mcMMO) required to unlock the rank
- **Drop, experience, and auto-smelt** behavior for that rank tier
- Optional **block-level overrides** for fine-grained control per material

When a player breaks a tracked block, EzMine walks `settings.rank-order` from top to bottom
and awards the first rank the player qualifies for.

---

## Flat format (default)

Ranks defined directly in `ranks.yml` are used as a single shared profile.

```yaml
ranks:
  default:
    permission: ""              # Empty = every player qualifies
    minimum-skill-level: 0
    drop-multiplier: 1.0
    experience-multiplier: 1.0
    auto-smelt: false
    fortune: true
    luckperms-group: ""
    luckperms-permission: ""

  vip:
    permission: ezmine.rank.vip
    minimum-skill-level: 15     # Requires EzSkills/mcMMO mining level 15
    drop-multiplier: 1.5
    experience-multiplier: 1.25
    auto-smelt: true
    fortune: true
    luckperms-group: ""
    luckperms-permission: ""
    block-overrides:
      DIAMOND_ORE:
        drop-multiplier: 2.0
      DEEPSLATE_DIAMOND_ORE:
        drop-multiplier: 2.0

  elite:
    permission: ezmine.rank.elite
    minimum-skill-level: 30
    drop-multiplier: 2.0
    experience-multiplier: 1.5
    auto-smelt: true
    fortune: true
    block-overrides:
      ANCIENT_DEBRIS:
        drop-multiplier: 2.0
        experience-multiplier: 2.0
```

---

## Profile format (multi-world)

Group ranks into named profiles so different worlds or WorldGuard regions can use
different rank ladders. Map profiles to worlds and regions in `settings.yml`.

```yaml
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

---

## Rank fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `permission` | string | `""` | Permission node required (`ezmine.rank.<name>`). Empty string matches all players. |
| `minimum-skill-level` | int | `0` | Minimum EzSkills or mcMMO mining level. Players below this level are skipped. |
| `drop-multiplier` | double | `1.0` | Multiplies the quantity of each drop. `2.0` doubles drops. |
| `experience-multiplier` | double | `1.0` | Adjusts Minecraft XP and skill XP rewards. |
| `auto-smelt` | boolean | `false` | Instantly converts ores to ingots (or custom outputs from `auto-smelt-results`). |
| `fortune` | boolean | `true` | Allow or deny fortune enchantment when calculating drops. |
| `luckperms-group` | string | `""` | LuckPerms group name (requires `settings.luckperms.enabled: true`). |
| `luckperms-permission` | string | `""` | LuckPerms permission node for group-based resolution. |

---

## Block overrides

Use `block-overrides` within any rank to override specific materials. Only the fields
you specify are overridden; all other fields fall back to the rank default.

```yaml
ranks:
  elite:
    drop-multiplier: 2.0
    auto-smelt: true
    block-overrides:
      ANCIENT_DEBRIS:
        drop-multiplier: 3.0          # Triple drops on ancient debris only
        experience-multiplier: 2.0
        auto-smelt: false             # Keep ancient debris as-is (no smelt conversion)
```

---

## Prison server example

A five-tier prison rank ladder where each rank is gated by both a permission node
and an increasing EzSkills mining level:

```yaml
settings:
  rank-order:
    - a
    - b
    - c
    - d
    - z

ranks:
  a:
    permission: ezmine.rank.a
    minimum-skill-level: 0
    drop-multiplier: 1.0
    experience-multiplier: 1.0
    auto-smelt: false
    fortune: true

  b:
    permission: ezmine.rank.b
    minimum-skill-level: 10
    drop-multiplier: 1.2
    experience-multiplier: 1.1
    auto-smelt: false
    fortune: true

  c:
    permission: ezmine.rank.c
    minimum-skill-level: 25
    drop-multiplier: 1.5
    experience-multiplier: 1.25
    auto-smelt: true
    fortune: true

  d:
    permission: ezmine.rank.d
    minimum-skill-level: 50
    drop-multiplier: 1.75
    experience-multiplier: 1.4
    auto-smelt: true
    fortune: true

  z:
    permission: ezmine.rank.z
    minimum-skill-level: 100
    drop-multiplier: 2.0
    experience-multiplier: 1.5
    auto-smelt: true
    fortune: true
    block-overrides:
      ANCIENT_DEBRIS:
        drop-multiplier: 3.0
```
