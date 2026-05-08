---
layout: default
title: Commands
parent: Server Owners
nav_order: 6
---

# Commands
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Player commands

### `/pickaxe`

Opens the pickaxe shop GUI. Players can browse and purchase custom tools if
`custom-tools.shop.enabled` is `true`.

Permission: `ezmine.pickaxe` (default: `true`)

---

## Admin command — `/ezmine`

All subcommands use `/ezmine`. The base command with no arguments shows the plugin version
and the caller's active rank.

Permission: `ezmine.command` (default: `true`)

### Rank inspection

| Command | Description |
|---------|-------------|
| `/ezmine` | Show EzMine version and your currently active rank. |

### Configuration

| Command | Permission | Description |
|---------|------------|-------------|
| `/ezmine reload` | `ezmine.reload` (op) | Reload all configuration files from disk. Equivalent to restarting the plugin without a server restart. |

### Tool distribution

| Command | Permission | Description |
|---------|------------|-------------|
| `/ezmine tool <id>` | `ezmine.custom-tool` (op) | Give yourself the specified custom tool. |
| `/ezmine tool <id> <player>` | `ezmine.custom-tool` (op) | Give the specified player the custom tool. |

`<id>` is the tool key as defined in `tools.yml` (case-insensitive).

---

## Permissions reference

| Node | Default | Description |
|------|---------|-------------|
| `ezmine.command` | `true` | View EzMine version and active rank. |
| `ezmine.reload` | `op` | Reload EzMine configuration. |
| `ezmine.custom-tool` | `op` | Give custom tools with `/ezmine tool`. |
| `ezmine.pickaxe` | `true` | Open the pickaxe shop with `/pickaxe`. |
| `ezmine.rank.<name>` | `false` | Grants the named rank. Replace `<name>` with the rank key (e.g. `ezmine.rank.vip`). |

{: .note }
Rank permissions default to `false` — assign them explicitly via LuckPerms or another
permissions manager.

---

## Tab completion

All `/ezmine` subcommands support tab completion. The `tool` subcommand completes tool IDs
from `tools.yml` and online player names.
