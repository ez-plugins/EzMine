---
layout: home
title: EzMine
nav_order: 1
description: Rank-aware mining enhancements for Spigot and Paper servers.
permalink: /
---

# EzMine

A lightweight, configurable mining enhancement plugin for Spigot and Paper servers.
Permission-driven ranks control drop multipliers, auto-smelt, fortune, and experience rewards
— all without touching core Minecraft mechanics.

## Features

- **Permission-driven ranks** — define any number of mining ranks with distinct drop multipliers,
  auto-smelt behavior, fortune control, and XP rewards. Players automatically receive the highest
  rank they have permission for.
- **World and region profiles** — route different rank perks per world or WorldGuard region.
  Keep vanilla mining in survival while powering a full progression system in your prison mine.
- **Block-level overrides** — fine-tune individual materials (e.g. double drops on ancient debris)
  while keeping global rank defaults in place.
- **Custom tools** — distribute bespoke pickaxes with scripted actions: 3x3 area mining,
  instant auto-smelt, and ore-searcher particle hints.
- **Pickaxe shop** — let players purchase custom tools from a GUI shop backed by Vault economy.
- **EzSkills integration** — gate ranks behind EzSkills mining levels and award EzSkills XP
  per block broken, with optional per-world skill overrides.
- **mcMMO integration** — use mcMMO mining levels for rank gates and award mcMMO XP.
- **LuckPerms integration** — resolve ranks by LuckPerms primary group for seamless sync
  with your permission hierarchy.
- **WorldGuard integration** — restrict EzMine to specific regions or require players to be
  inside a region before perks activate.

## Quick start

1. Drop `EzMine.jar` into your server's `plugins/` folder and restart.
2. Assign rank permissions (`ezmine.rank.vip`, `ezmine.rank.elite`, etc.) via LuckPerms or
   another permissions manager.
3. Edit `plugins/EzMine/settings.yml` to configure rank order, tracked blocks, and worlds.
4. Edit `plugins/EzMine/ranks.yml` to tune multipliers and unlock conditions per rank.
5. Run `/ezmine reload` to apply changes live.

## Requirements

| Requirement | Version |
|-------------|---------|
| Java | 21+ |
| Spigot / Paper | 1.21+ |
| EzSkills *(optional)* | 2.0+ |
| mcMMO *(optional)* | any |
| Vault *(optional, for shop)* | any |
| WorldGuard *(optional)* | 7.x |
| LuckPerms *(optional)* | 5.x |

{: .note }
EzMine is released under the [MIT License](https://github.com/ez-plugins/EzMine/blob/main/LICENSE.md).
