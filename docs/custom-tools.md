---
layout: default
title: Custom Tools
parent: Server Owners
nav_order: 5
---

# Custom Tools
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Overview

Custom tools are special pickaxes (or any item) that have scripted actions attached.
When a player right-clicks with a custom tool, its actions activate. Actions include:

| Action | Effect |
|--------|--------|
| `3x3` | Mine a 3x3 area centered on the broken block |
| `auto-smelt` | Instantly smelt ore drops to ingots |
| `ore-searcher` | Spawn particle hints toward nearby ores |

Multiple actions can be combined on a single tool. Action groups let you reuse combinations
across multiple tools without repeating configuration.

---

## tools.yml structure

```yaml
custom-tools:
  enabled: true

  shop:
    enabled: true
    title: "&8Pickaxe Shop"
    rows: 3
    currency-name: "coins"
    vault:
      enabled: false     # Set true to charge players via Vault economy

  actions:               # Reusable action groups (prefixed with @ when referencing)
    area-mining:
      actions:
        - "3x3"
    smelting:
      actions:
        - "auto-smelt"
    ore-hunter:
      actions:
        - "ore-searcher"
    prison-master:       # Combine multiple groups
      actions:
        - "@area-mining"
        - "@smelting"
        - "@ore-hunter"

  tools:
    quarry-hammer:
      material: NETHERITE_PICKAXE
      name: "&bQuarry Hammer"
      lore:
        - "&7Area mining tool."
        - "&7Right-click to activate."
      actions:
        - "@area-mining"
      shop:
        visible: true
        slot: 11
        cost: 0

    smelter-pickaxe:
      material: DIAMOND_PICKAXE
      name: "&6Smelter Pickaxe"
      lore:
        - "&7Auto-smelt mining tool."
      actions:
        - "@smelting"
      shop:
        visible: true
        slot: 13
        cost: 500

    prison-master:
      material: NETHERITE_PICKAXE
      name: "&dPrison Master"
      lore:
        - "&7All EzMine actions in one tool."
      actions:
        - "@prison-master"
      shop:
        visible: false   # Hidden from shop, staff-only via /ezmine tool
```

---

## Tool fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `material` | Material | Yes | Bukkit `Material` enum name (e.g. `DIAMOND_PICKAXE`). |
| `name` | string | Yes | Display name. Supports `&` color codes. |
| `lore` | list | No | List of lore lines. Supports `&` color codes. |
| `actions` | list | Yes | List of action names or `@group` references to execute on right-click. |
| `custom-model-data` | int | No | Custom model data for resource pack textures. Must be `>= 0`. |
| `shop.visible` | boolean | No | Whether the tool appears in the pickaxe shop GUI. |
| `shop.slot` | int | No | GUI slot index (0-based). Must be within `shop.rows * 9`. |
| `shop.cost` | double | No | Price in the Vault economy currency. `0` = free. |

---

## Giving a custom tool

Operators can distribute tools directly with:

```
/ezmine tool <id> [player]
```

- `<id>` — the tool key from `tools.yml` (e.g. `quarry-hammer`)
- `[player]` — optional target player; defaults to the command sender

Permission: `ezmine.custom-tool` (default: `op`)

---

## Shop GUI

Players open the shop with `/pickaxe` (permission: `ezmine.pickaxe`, default: `true`).

Tools marked `shop.visible: true` appear in the GUI. When Vault is enabled and a tool
has `cost > 0`, the player's balance is charged on purchase.

---

## Validation

EzMine validates `tools.yml` on load. Fatal errors disable custom tools until corrected.
Common messages and their fixes:

| Message | Fix |
|---------|-----|
| `Missing material for custom tool <id>` | Set a valid Bukkit `Material` name. |
| `Unknown material <name>` | Correct the material name — it must match a `Material` enum value exactly. |
| `Unknown action '<action>'` | Use a built-in action (`3x3`, `auto-smelt`, `ore-searcher`) or a defined `@group`. |
| `Duplicate custom tool id after normalization` | Tool IDs are case-insensitive; ensure each key is unique. |
| `custom-model-data must be zero or positive` | Use a non-negative integer. |
| `shop.slot out of range` | Choose a slot within `shop.rows * 9 - 1`. |
