# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [21.1.18]

### Added
- GeOre island conjuring rituals, in the style of Ars Caelum's Geode island ritual. 25 new ritual tablets (registered under the `ars_caelum` namespace as `ritual_conjure_island_<ore>` and `ritual_conjure_island_nether_<ore>`) each conjure a floating island built around that ore's GeOre, using the pack's existing `ftb:geore_island/<ore>` and `ftb:geore_island_nether/<ore>` structure templates. Each `GeoreIslandRitual` extends Ars Nouveau's `StructureRitual` (10000 source, same `-8/-3/-7` offset as the vanilla geode), is registered with `RitualRegistry` at common setup, and names itself from the ritual (no lang files needed). Tablets craft from a purple archwood log, three source blocks, and the matching `geore:<ore>_cluster`, and appear in the Ars Nouveau creative tab alongside the other ritual tablets (registered into the shared `RitualRegistry` item map, the same way Ars Nouveau registers its own tablets). Overworld: aluminum, black quartz, coal, copper, gold, iron, lapis, lead, monazite, nickel, osmium, redstone, ruby, sapphire, silver, tin, topaz, uranium, zinc. Nether: ancient debris, diamond, emerald, platinum, quartz, tungsten. (Amethyst is omitted — it has no distinct artwork and duplicates the vanilla Geode ritual.)

## [21.1.17]

### Added
- The Oritech Enchantment Catalyst's overenchanting is now capped. Vanilla allows enchantment levels up to 255, and the Catalyst would happily push an item that high one level at a time. A mixin on `EnchantmentCatalystBlockEntity.canProceed()` stops the process once the target item reaches a configurable ceiling (`max_overenchant_level`, default **15**, in `ftbskies2aerocompanion-oritech.toml`). Souls are not consumed past the cap. The Enchanter itself already respected each enchantment's normal max level, so only the Catalyst's overenchant path needed limiting.

## [21.1.16]

### Changed
- No ores generate in the overworld anymore (Sky Archipelago islands included). A custom NeoForge biome modifier (`remove_overworld_ores`) strips every ore-type placed feature (`minecraft:ore`/`scattered_ore` whose target block is an ore) from all `#minecraft:is_overworld` biomes, so islands no longer carry ore veins. Stone/dirt/gravel blobs are kept; only ore features are removed. Future-proof across mods (matches by feature type + target name, not a hand-maintained list).

## [21.1.15]

### Changed
- Random Bone Meal Flowers now only generates vanilla (`minecraft`) flowers. The mod detects flowers by the `FlowerBlock` class and only supports a blacklist (no whitelist), so modded flowers (e.g. `irregular_implements:pitcher_plant`, tropicraft/flourish/ars_elemental flowers) kept slipping through. A mixin now filters the mod's flower list to the `minecraft` namespace after it's built — complete and future-proof regardless of which flower mods are installed. The mod's existing `blacklist.txt` still applies first, so excluded vanilla flowers (wither rose, torchflower) stay excluded.

## [21.1.14]

### Added
- JEI support for the Mekanism Lasers Ore Generator. A new "Ore Generator" category lists every ore the generator can produce (the `#c:ores` pool minus the mod's config blacklist and `mekanism_lasers:ore_blacklist` tag), with the roll chance shown on hover. Looking up uses of the Ore Generator block shows the full list, and looking up any generated ore shows the Ore Generator as a source. Client-side only; gracefully does nothing if Mekanism Lasers isn't installed.

## [21.1.13]

### Fixed
- Entering a Compact Machine from a moving airship no longer strands you. The companion captures a ship binding when you enter a room from a sub-level (`RoomHelper.teleportPlayerIntoRoom`) and, when you leave the compact dimension back to the ship's dimension, re-ejects you onto the ship's current position instead of the stale entry coordinates — the same ship-aware teleport used for homes/warps/respawn. Persisted across logout, and falls back to the original coordinates if the ship is gone. {#3878}

## [21.1.12]

### Fixed
- The Integrated Dynamics Squeezer now works on Create: Aeronautics / Sable airships. When a player jumps on a squeezer that sits on a sub-level, its `updateEntityAfterFallOn` was flooring the player's world coordinates and never finding the block in the sub-level plot region. The companion now redirects those coordinate lookups into the sub-level frame (mirroring Sable's own per-block fall-on compat for Create's Basin/Saw/Seat), so jumping squeezes again. {#3877}

## [21.1.11]

### Fixed
- Guarded against a client crash where a Create block entity is ticked at a position whose block is `void_air` (or air) — an orphaned block entity left behind by a Sable sub-level (airship) teardown. `SmartBlockEntity.tick` now drops the orphaned block entity instead of letting `validateBlockState` throw, and logs the position so the underlying Sable desync can still be reported. Independent of Sable version.

## [21.1.10]

### Fixed
- Functional Storage drawers now work on Create: Aeronautics / Sable airship sub-levels. Two sub-level-aware mixins: depositing/extracting recomputes the clicked slot from the (already sub-level-correct) hit result instead of FS's world-space re-raytrace that misses ship geometry, and Titanium's container locator (`TileEntityLocatorInstance`) resolves the tile through the player's sub-level so the configuration GUI opens. The Titanium fix also covers other Titanium-based block GUIs on ships.

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
