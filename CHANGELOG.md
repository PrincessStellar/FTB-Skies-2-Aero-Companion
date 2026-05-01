# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [21.1.1]

### Added
- Initial scaffold for the FTB Skies 2: Aero Companion mod (Minecraft 1.21.1, NeoForge).
- Villager trade adjustments (`VillagerTradesHandler`) ported from the pack's previous KubeJS `villager_trades.js`: cartographer crash-prone map trades replaced with a hang-glider trade, Roots `silver_ingot` trades stripped, MI `industrialist` levels 1-4 swapped to FTB Materials equivalents, AE2 `fluix_researcher` level 2 emerald trade rebalanced. Lets the pack drop its MoreJS dependency.

### Fixed
- `LootrDirectoryFix`: create `<world>/data/lootr/` on `ServerStartingEvent` so Lootr 1.21.1's TickingData saves don't throw `NoSuchFileException` every tick (Lootr forgets to `createDirectories` before its atomic-write).

### Removed
### Changed
