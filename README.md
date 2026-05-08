# EzMine

[![GitHub Release](https://img.shields.io/github/v/release/ez-plugins/EzMine?style=flat-square)](https://github.com/ez-plugins/EzMine/releases)
[![Modrinth](https://img.shields.io/modrinth/dt/ezmine?style=flat-square&logo=modrinth)](https://modrinth.com/plugin/ezmine)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)
[![Java 21+](https://img.shields.io/badge/Java-21%2B-brightgreen?style=flat-square)](https://adoptium.net/)

**EzMine** gives players smarter ore drops, instant smelting, and configurable XP bonuses
driven entirely by rank permissions. No custom block mechanics — just clean progression on
top of vanilla mining.

> Full documentation: <https://ez-plugins.github.io/EzMine/>

---

## Features

- **Permission-driven ranks** — Define any number of mining ranks with drop multipliers,
  XP boosts, and auto-smelt toggles. Players automatically receive the first rank they
  have permission for (`settings.rank-order`).
- **Block overrides** — Fine-tune individual materials per rank without touching global
  defaults.
- **World & region profiles** — Map different rank ladders to worlds or WorldGuard regions.
- **Tracked blocks** — Restrict EzMine to specific ores so regular blocks stay vanilla.
- **Auto-smelt** — Instantly convert ore drops to ingots (or custom outputs) per rank.
- **Custom tools** — Distribute scripted pickaxes with 3×3 harvesting, ore-searcher
  particle hints, and forced auto-smelt — all from YAML.
- **Pickaxe shop** — Players browse and purchase tools through a GUI backed by Vault
  economy (optional).
- **EzSkills & mcMMO** — Gate ranks by external skill levels; award skill XP per block.
- **WorldGuard** — Restrict perks to specific regions.
- **LuckPerms** — Resolve ranks by primary group.

---

## Requirements

| Requirement | Version |
|-------------|---------|
| Java | 21 or newer |
| Spigot / Paper | 1.21 or newer |
| [EzSkills](https://github.com/ez-plugins/EzSkills) | 2.0+ *(optional)* |
| [mcMMO](https://www.spigotmc.org/resources/mcmmo.2445/) | any *(optional)* |
| [Vault](https://www.spigotmc.org/resources/vault.34315/) | any *(optional)* |
| [WorldGuard](https://enginehub.org/worldguard/) | any *(optional)* |
| [LuckPerms](https://luckperms.net/) | any *(optional)* |

---

## Installation

1. Download the latest jar from [GitHub Releases](https://github.com/ez-plugins/EzMine/releases)
   or [Modrinth](https://modrinth.com/plugin/ezmine).
2. Place it in your server's `plugins/` directory.
3. Start (or restart) the server.
4. Assign rank permissions (e.g. `ezmine.rank.vip`) through LuckPerms or your preferred
   permissions manager.
5. Reload with `/ezmine reload` after editing configuration.

---

## Quick start

```yaml
# settings.yml
settings:
  rank-order:
    - default
    - vip
  tracked-blocks:
    - DIAMOND_ORE
    - DEEPSLATE_DIAMOND_ORE
```

```yaml
# ranks.yml
ranks:
  default:
    permission: ""
    drop-multiplier: 1.0
    auto-smelt: false
    fortune: true
  vip:
    permission: ezmine.rank.vip
    drop-multiplier: 2.0
    auto-smelt: true
    fortune: true
```

---

## Commands & permissions

| Command | Permission | Default |
|---------|------------|---------|
| `/ezmine` | `ezmine.command` | `true` |
| `/ezmine reload` | `ezmine.reload` | `op` |
| `/ezmine tool <id> [player]` | `ezmine.custom-tool` | `op` |
| `/pickaxe` | `ezmine.pickaxe` | `true` |

Rank permission nodes follow the pattern `ezmine.rank.<name>` (e.g. `ezmine.rank.elite`).

---

## EzSkills API (developers)

EzMine integrates with EzSkills via JitPack. Add the API to your project:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>com.github.ez-plugins.EzSkills</groupId>
        <artifactId>ezskills-api</artifactId>
        <version>2.0.3</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

---

## Contributing

Pull requests are welcome. Please run `mvn checkstyle:check` before submitting to ensure
your code meets the project's style rules.

```bash
git clone https://github.com/ez-plugins/EzMine.git
cd EzMine
mvn package -DskipTests
```

---

## License

[MIT](LICENSE) — free to use, fork, and modify.
