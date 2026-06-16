# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [21.1.9]

### Fixed
- Fixed a `ConcurrentModificationException` during parallel mod loading: Create: New Age registers sounds into Create's shared `AllSoundEvents.ALL` map from its constructor while Create's own constructor iterates that map in `AllSoundEvents.prepare()`. A mixin now makes `prepare()` iterate a snapshot, so the race is harmless and the pack can run full mod-loading parallelism (`fml.toml` `maxThreads = -1`) without crashing.

## [21.1.8]

### Fixed
- IntegratedDynamics networks now survive ship **disassembly**, not just assembly. The disassembly path (`SimAssemblyHelper.disassembleSubLevel`) is now bracketed with `setRemovingCable` and reforms the network in the parent world afterward, so parts like a steering block reader keep working after a disassemble/reassemble cycle.

## [21.1.7]

### Fixed
- Breaking an IntegratedDynamics cable/part on an assembled ship no longer kicks the player. The companion now takes over the removal of ID cables on Sable sub-levels, catching the network-teardown exception that ID throws there (the dropped parts/cards are unaffected), removing the block, and reforming the remaining network.

## [21.1.6]

### Fixed
- `SkyboundAnchorController`: never feed a non-finite (NaN/infinite) torque impulse into the physics engine, which could crash Sable's native Rapier library.

## [21.1.5]

### Added
- `EntityCollisionGuardMixin`: skips an entity's block-collision sweep when its collision box is non-finite or over 512 blocks on any axis, preventing the server-hang crash from oversized Sable sub-level collisions.

## [21.1.4]

### Fixed
- Blaze and Steel now actually lights Nether portals. The pack restricts portal ignition to Irregular Implements' Blaze and Steel (`NetherPortalIgnitionHandler` cancels `PortalSpawnEvent` for any other fire), but `irregular_implements:blaze_fire` was never in the `#minecraft:fire` block tag, so vanilla `PortalShape.findEmptyPortalShape` rejected the frame interior and no portal-spawn was ever attempted — the fire just sat there. Added a `#minecraft:fire` tag entry for `blaze_fire` (`required: false`), so the frame is recognized and the ignition handler allows it through.

## [21.1.3]

### Fixed
- `ShipHomeEvents`: bed/respawn-anchor respawns set on the ground (not on a ship) are now honored again. FTB Team Bases rewrites every respawn to the lobby at default priority; the companion only re-asserted respawns for beds bound to a Sable sub-level, so a normal base bed was lost and the player woke at the lobby (reported as dying in the Nether returning you to spawn). A HIGHEST-priority listener now snapshots the vanilla bed/anchor transition before Team Bases overrides it, and the existing LOWEST-priority handler restores it for players with a real respawn point set. Players with no respawn point still fall through to the lobby.

## [21.1.2]

### Removed
- `LootrDirectoryFix` — Lootr fixed the upstream `NoSuchFileException` save bug, workaround no longer needed.

## [21.1.1]

### Added
- Initial scaffold for the FTB Skies 2: Aero Companion mod (Minecraft 1.21.1, NeoForge).
- Villager trade adjustments (`VillagerTradesHandler`) ported from the pack's previous KubeJS `villager_trades.js`: cartographer crash-prone map trades replaced with a hang-glider trade, Roots `silver_ingot` trades stripped, MI `industrialist` levels 1-4 swapped to FTB Materials equivalents, AE2 `fluix_researcher` level 2 emerald trade rebalanced. Lets the pack drop its MoreJS dependency.

### Fixed
- `LootrDirectoryFix`: create `<world>/data/lootr/` on `ServerStartingEvent` so Lootr 1.21.1's TickingData saves don't throw `NoSuchFileException` every tick (Lootr forgets to `createDirectories` before its atomic-write).

### Removed
### Changed
