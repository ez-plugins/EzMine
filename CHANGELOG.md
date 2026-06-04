# Changelog

All notable changes to EzMine are documented here.

---

## [2.0.1] - 2026-06-04

### Fixed

- **Null pointer exception in vein mining** - `VeinMineUtil.findVein()` now gracefully handles null origin block
- **Null pointer exception in area mining** - `AreaMineUtil.mine3x3()` now validates center block and world before use
- **Locale-sensitive string comparison** - `AllowedActions.isAllowed()` now uses `toLowerCase(Locale.ROOT)` for consistent behavior
- **Redundant validation** - Removed redundant `shopCost < 0` check in `CustomToolDefinition.validate()` (constructor already clamps)
- **Inconsistent action matching** - `CustomToolListener.onItemHeld()` now uses case-insensitive action matching consistent with `onPlayerInteract()`

### Added

- **Unit tests** - Added test coverage for bug fixes in `AllowedActions`, `VeinMineUtil`, and `CustomToolDefinition`

---

## [2.0.0] - 2025-05-08

### Breaking changes

- **Java 25 required.** The minimum supported Java version has been raised from 8 to 25.
- **Parent POM removed.** EzMine is now fully standalone - the `com.skyblockexp:EzTree-parent`
  parent POM dependency has been eliminated. All repositories and build properties are defined
  directly in `pom.xml`.
- **EzSkills 2.0+ required.** The reflection-based EzSkills adapter has been removed. EzMine
  now depends on the `ezskills-api` artifact from JitPack and calls `EzSkillsAPI` directly.
  Servers running EzSkills 1.x must upgrade to EzSkills 2.0+ before upgrading EzMine.

### Added

- **Vein miner action** - New `vein-miner` custom tool action. When active, mining any
  tracked block automatically mines the entire connected vein of the same material in one
  swing. Uses a BFS flood-fill across the six face-connected neighbours.
  - Configurable via `settings.actions.vein-miner`: `enabled` flag and `max-blocks`
    (default `64`, hard cap `512`).
  - Fully compatible with area mining, auto-smelt, ore-searcher, all rank drop/XP
    multipliers, and WorldGuard region checks.
  - Built-in `vein-mining` action group and `vein-pickaxe` example tool added to
    `tools.yml`.
  - `prison-master` combo action group now includes `vein-mining` by default.
- **GitHub Actions CI** - build verification on every push and pull request to `main`.
- **Code quality workflow** - Checkstyle enforced on every push and pull request.
- **Documentation site** - new Jekyll/GitHub Pages docs at `docs/`.
- **Docs CI workflow** - markdownlint and Javadoc verification on pull requests.
- **Publish workflow** - automated release to GitHub Packages and Modrinth on version tags.
- **`checkstyle.xml`** - Checkstyle configuration aligned with project code style.
- **`.markdownlint.yml`** - Markdown lint configuration for all documentation files.
- **`.gitignore`** - comprehensive ignore rules for Maven and IDE artifacts.

### Changed

- `EzSkillsIntegration` replaced reflection with direct `EzSkillsAPI` calls.
- `pom.xml` updated to version `2.0.0`; `maven.compiler.release` set to `25`.
- Spigot API version updated to `1.21.4-R0.1-SNAPSHOT`.

---

## [1.3.1] and earlier

See the [GitHub commit history](https://github.com/ez-plugins/EzMine/commits/main) for
changes prior to 2.0.0.
