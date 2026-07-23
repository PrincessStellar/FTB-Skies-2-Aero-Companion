# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [21.1.38]

### Fixed

- Squat Grow now works while standing on an airship. It scanned for crops around the player's world position, but a ship's blocks are stored at its sub-level plot coordinates, so the scan origin is now transformed into the ship's block space when the player is on one. FTBTesting/Testing-Issues#4066
- Further hardening against item frame duplication on ship assembly/disassembly: the vanilla move/push kill-and-drop paths are now suppressed for frames on ships, and frames placed while the ship was already assembled now get the same drop protection during disassembly that assembled frames already had. FTBTesting/Testing-Issues#4076
- Taking items out of Functional Storage drawers now works on assembled airships.

## [21.1.37]

### Changed

- Floating islands now spawn across a wider range of heights so fewer sit high enough to turn snowy. The height band is configurable under `island_spawning` in the config.

### Fixed

- Bits N' Bobs cogwheel chains no longer duplicate when an airship carrying them is assembled and disassembled. FTBTeam/FTB-Modpack-Issues#12709
- Placing a composter (or other non full cube block) on an assembled airship no longer visually breaks crouch. FTBTeam/FTB-Modpack-Issues#12710
- Extended Industrialization Solar Boilers now output Mekanism steam instead of MI steam. FTBTeam/FTB-Modpack-Issues#12723
- Extended Industrialization's Bronze and Steel steam machines, such as the Bending Machine, now run on the pack's steam instead of requiring the unavailable MI steam. FTBTeam/FTB-Modpack-Issues#12795
- Fixed server log flooding caused by a ridden flying Happy Ghast. FTBTeam/FTB-Modpack-Issues#12739
- Crafting Stations, including the slab variant, no longer duplicate their items when a station holding items is assembled into an airship. FTBTeam/FTB-Modpack-Issues#12714
- Item frames on an airship no longer drop and duplicate their held item when the ship is assembled or disassembled. FTBTeam/FTB-Modpack-Issues#12763
- A base whose ship assembly corrupted a block entity no longer crashes the server when its chunk loads. FTBTeam/FTB-Modpack-Issues#12763
- Floating islands no longer generate with flat cut-off edges, and large islands now paint their full intended size instead of clipping past ~128 blocks.
- Sky villages and other custom floating structures no longer generate overlapping a Sky Archipelago island. A configurable exclusion zone under `structure_exclusion` keeps them clear.
- Item frames and paintings on an airship no longer log an "invalid position" error and lose their attachment when their chunk reloads.
- Ars Nouveau spell effects no longer resolve on airship structural entities, so spells like Blink and Bubble can't teleport a ship to its far-away plot coordinates or lag the server trying. FTBTeam/FTB-Modpack-Issues#12745 FTBTeam/FTB-Modpack-Issues#12785
- Tiered Void Fishing Rods can now be enchanted: all six tiers were missing from the enchantable item tags, and the unbreakable Supremium tiers additionally lacked the max damage component vanilla's enchantability check requires. FTBTeam/FTB-Modpack-Issues#12769

## [21.1.36]

### Fixed

- Functional Storage drawers on an assembled airship once again accept items instead of opening their configuration GUI. FS's `Drawer.useItemOn` discards the `BlockHitResult` it was handed and re-derives the clicked slot via `getHit`, which re-raytraces from the player (`RayTraceUtils.rayTraceSimple(level, player, 32, 0)`); that misses on a sub-level and returns `-1`, and `onSlotActivated(..., -1)` opens the config screen. `DrawerSlotSubLevelMixin` already recomputed the slot from the real hit, but gated it on `Sable.HELPER.getContaining(player)`, which resolves through `SubLevelContainer.getPlot(chunkX, chunkZ)` and therefore only answers "is the player physically inside a sub-level's plot region" — not "is the player standing on a ship". The guard now also accepts `getTrackingOrVehicleSubLevel(player)`, covering players standing on a deck or riding a seat/console.
- Create Redstone Links (and the Aeroworks console/steering wheel that transmits over them) no longer silently fail to register on airship sub-levels, which left a steering wheel turning with the ship unresponsive until a reload. Create keys its link networks by `Level` instance in `RedstoneLinkNetworkHandler.connections`, populated from the `LevelEvent.Load` hook; for any level not yet in that map, `networksIn` logged "Tried to Access unprepared network space" and returned a **throwaway `HashMap`**, so every `addToNetwork` on it was discarded. Sable sub-levels are created during assembly and their block entities begin ticking immediately, so a console whose first tick beat the sub-level's Load event registered into that discarded map — and because `ConsoleBlockEntity.initialize()` is first-tick-only and latches `networkRegistered = true` regardless of outcome, it never retried. `RedstoneLinkSubLevelNetworkMixin` makes `networksIn` lazily create and *store* the map for an unprepared level instead of discarding it, and makes `onLoadWorld` skip levels that already have live networks rather than clobbering them. Both sides (the console transmitter and Create's `LinkBehaviour` receivers) share the same first-tick-only registration, so fixing the handler heals both.

## [21.1.35]

### Added
- Overcharged item tags for automation, now shipped natively instead of via pack KubeJS overrides: `c:plates` (adds the Create: New Age iron/gold overcharged sheets alongside the companion sheets), `c:gems/overcharged_diamond`, `create_new_age:overcharged_items`, and the `ftb:overcharged_sheets` / `ftb:overcharged_ingots` / `ftb:overcharged_wires` groupings. FTBTesting/Testing-Issues#4033

### Fixed

- Iron's Spellbooks Chain Lightning no longer crashes the server after a dimension transition. `ChainLightning.tick()` damages its `initialVictim` on the first tick, but that field is transient (never written to NBT), so when the entity is rebuilt by a dimension transition (Sable sub-level/ship crossing, portals) it is null while `tickCount` resets to 0. The mod's `doHurt(initialVictim)` call has no null check and throws `NullPointerException` in `DamageSources.applyDamage`, killing the server thread. Guarding only `doHurt` is insufficient: with a non-null owner (owner UUID *is* in save-data and survives the rebuild) the same tick then dereferences `initialVictim` again in the particle block (`initialVictim.position()`), so `ChainLightningNullVictimMixin` instead cancels the whole tick at HEAD and discards the entity when a server-side chain lightning has a null `initialVictim` — the rebuilt entity has lost its victim state and is inert anyway. The guard is server-gated because client-side chain lightnings always have a null `initialVictim` (built from the spawn packet) and must keep rendering. Remove once fixed upstream.
- The Quantum Energiser is now mineable. It was missing from the `minecraft:mineable/pickaxe` and `minecraft:mineable/axe` block tags that its netherite/platinum/titanium siblings already carried, so it broke slowly with any tool. FTBTesting/Testing-Issues#4033
- Right-clicking certain entities (e.g. a Draconic Evolution Guardian Crystal in The End) no longer crashes the server. Cognition's `EventHandler.onPlayerRightClickEntity` unconditionally calls `ProtectionSalveItem.handleEntity` on every `EntityInteractSpecific`, and that method is leftover debug code that force-serializes the target with `entity.save()` and dumps the NBT to `System.out` — no held-item or entity-type guard. The Guardian Crystal's `addAdditionalSaveData` writes a null owner UUID via `CompoundTag.putUUID`, so the forced save threw `NullPointerException` mid-interaction. `ProtectionSalveItemMixin` now cancels `handleEntity` at HEAD, which also stops the per-interaction entity serialization and console NBT spam it caused on every entity right-click.

## [21.1.26]

### Fixed
- The GeOre "Conjure Island" ritual tablets now actually spawn their islands. Ars Nouveau's `RitualRegistry.getRitual()` reinstantiates a registered ritual per cast by reflecting its **no-arg** constructor (`getClass().getDeclaredConstructor().newInstance()`); `GeoreIslandRitual` is a single parameterized class with only a 4-arg constructor, so that reflection threw `NoSuchMethodException` and `getRitual` returned null — the brazier consumed the tablet but had no ritual to run (no casting state, no island). A `@Inject` mixin on `getRitual` now returns a freshly-copied `GeoreIslandRitual` for the pack's `ars_caelum:ritual_conjure_island_*` IDs (via `GeoreRituals.createFresh`), leaving every other ritual to the vanilla reflection path. FTBTesting/Testing-Issues#3917

## [21.1.25]

### Fixed
- Create machines (Crushing Wheels and any other `SmartBlockEntity`) no longer void their contents on Sable sub-levels (airships/islands). The `SmartBlockEntityVoidGuardMixin` orphan check used `worldState.isAir()`, which was dropping live block entities whose world block was still a valid Create block — the spam was `Dropping orphaned Create block entity ... (world block is create:crushing_wheel_controller[valid=true])` every tick, killing the in-progress crush. The guard now drops a block entity only when its type is genuinely invalid for the world block (`!getType().isValid(worldState)`), which still catches truly orphaned BEs left by a sub-level teardown (`void_air`) while leaving valid blocks alone. FTBTesting/Testing-Issues#3920

## [21.1.24]

### Fixed
- Chance Cubes giant fluid sphere rewards no longer place invisible "invalid" blocks. `RewardsUtil.getRandomFluid` picks any fluid from the registry, and the `FluidSphereReward` / `MixedFluidSphereReward` then place `fluid.defaultFluidState().createLegacyBlock()`; for the many modded/tank-only fluids that have no `LiquidBlock` (Mekanism, Create, IE, Oritech, and the pack's own KubeJS fluids) that call returns `AIR`, so the sphere was full of invisible air blocks. A `@ModifyReturnValue` mixin now re-rolls any picked fluid whose `createLegacyBlock()` is air to a random placeable source fluid (falling back to water), so spheres are always made of real, visible fluid. FTBTesting/Testing-Issues#3900

## [21.1.23]

### Fixed
- The 25 GeOre conjuring ritual tablets now have proper names. `RitualTablet`'s name override isn't used in every display context (JEI, search, etc.), so the raw translation keys (`item.ars_caelum.ritual_conjure_island_*`) were showing through. The companion now ships `assets/ars_caelum/lang/en_us.json` with a name for each tablet matching the ritual's own `langName` (e.g. "Conjure Island: Uranium", "Conjure Island: Diamond (Nether)").

## [21.1.22]

### Fixed
- Connecting to a server no longer crashes / disconnects when the world's saved item registry has an orphaned or renamed id (a null hole in `MappedRegistry.byId`, left behind by mod churn over the world's life). The three ways code reaches registry entries now all tolerate null slots: `byId(int)` returns null (so defaulted registries like items fall back to `air`), `iterator()` skips holes, and `holders()` filters them out — instead of throwing `NullPointerException` on `Holder.value()`/`Holder.Reference.value()`. One fix covers every symptom that hit the same hole: Malum's spirit-repair recipe decode, Irregular Implements' lubricate-boot static init, the registry data-map sync, and JEI's grindstone/disenchant recipe builder during `onRecipesUpdated`.

## [21.1.20]

### Fixed
- Connecting to a server no longer crashes via SG Economy. Its `ServerConfig.bakeConfig()` unconditionally broadcasts a `SyncServerConfigS2C` packet with `PacketDistributor.sendToAllPlayers`; when a client receives the synced server config on connect, the `ModConfigEvent.Reloading` fires `bakeConfig` on the client (where no server exists), throwing `Cannot send clientbound payloads on the client`. A `@WrapOperation` now performs the broadcast only when a server is actually running (`ServerLifecycleHooks.getCurrentServer() != null`); the client still bakes the values and just skips the broadcast. Replaces the previous mixin, which targeted an `onLoad(ModConfigEvent)` signature SG Economy 1.0.5 no longer has (so it had silently stopped applying).

## [21.1.19]

### Fixed
- Connecting to a server no longer crashes when Sable's UDP side-channel fails to establish. `ClientboundSableUDPActivationPacket.handle` called `channel.eventLoop()` on the UDP channel with no null check, so when the channel was null (UDP couldn't bind, e.g. the server's UDP port isn't reachable) it threw an NPE that cascaded into a fatal render crash. Two `@WrapOperation` mixins now no-op the UDP activation when the channel is null, so UDP networking stays enabled and degrades gracefully to TCP instead of crashing. No config change needed; `attempt_udp_networking` can remain on.

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
- Entering a Compact Machine from a moving airship no longer strands you. The companion captures a ship binding when you enter a room from a sub-level (`RoomHelper.teleportPlayerIntoRoom`) and, when you leave the compact dimension back to the ship's dimension, re-ejects you onto the ship's current position instead of the stale entry coordinates — the same ship-aware teleport used for homes/warps/respawn. Persisted across logout, and falls back to the original coordinates if the ship is gone. FTBTesting/Testing-Issues#3878

## [21.1.12]

### Fixed
- The Integrated Dynamics Squeezer now works on Create: Aeronautics / Sable airships. When a player jumps on a squeezer that sits on a sub-level, its `updateEntityAfterFallOn` was flooring the player's world coordinates and never finding the block in the sub-level plot region. The companion now redirects those coordinate lookups into the sub-level frame (mirroring Sable's own per-block fall-on compat for Create's Basin/Saw/Seat), so jumping squeezes again. FTBTesting/Testing-Issues#3877

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
