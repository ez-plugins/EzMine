---
layout: default
title: Installation
parent: Server Owners
nav_order: 1
---

# Installation
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## As a Spigot / Paper plugin

1. Download the latest `EzMine-<version>.jar` from the
   [GitHub Releases](https://github.com/ez-plugins/EzMine/releases) page or
   [Modrinth](https://modrinth.com/plugin/ezmine).
2. Place the jar in your server's `plugins/` directory.
3. Start (or restart) the server. EzMine generates its default configuration files automatically
   under `plugins/EzMine/`.
4. Assign rank permissions to your players (see [Ranks](ranks.md)).
5. Reload with `/ezmine reload` or restart to apply any configuration changes.

## Building from source

```bash
git clone https://github.com/ez-plugins/EzMine.git
cd EzMine
mvn package -DskipTests
```

The shaded jar is produced at `target/EzMine-<version>.jar`.

## Optional dependencies

EzMine works standalone. Install any of the following for additional features:

| Plugin | Purpose |
|--------|---------|
| [EzSkills](https://modrinth.com/plugin/ezskills) | Gate ranks by mining skill level; award EzSkills XP per block |
| [mcMMO](https://www.spigotmc.org/resources/mcmmo.2445/) | Gate ranks by mcMMO mining level; award mcMMO XP |
| [Vault](https://www.spigotmc.org/resources/vault.34315/) | Power the pickaxe shop with an economy balance |
| [WorldGuard](https://modrinth.com/plugin/worldguard) | Restrict EzMine perks to specific regions |
| [LuckPerms](https://modrinth.com/plugin/luckperms) | Resolve ranks by primary group |

{: .note }
None of the optional dependencies are required. EzMine gracefully skips any integration whose
plugin is not loaded.
