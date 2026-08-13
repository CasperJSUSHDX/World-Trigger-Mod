# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

A NeoForge mod for Minecraft (targets Minecraft `26.2`, NeoForge `26.2.0.48-beta`, Java 25) themed around
*World Trigger*. Mod id is `wtmod`, base package `com.JSUSHDX.WorldTriggerMod`. Built with Gradle via the
`net.neoforged.moddev` plugin.

## Common commands

All commands use the Gradle wrapper (`gradlew.bat` on this Windows machine; PowerShell tool is primary).

- Build the mod jar: `.\gradlew.bat build`
- Run the client (opens a dev Minecraft client with the mod loaded): `.\gradlew.bat runClient`
- Run a dedicated server: `.\gradlew.bat runServer`
- Run registered gametests headlessly: `.\gradlew.bat runGameTestServer`
- Regenerate datagen output (models, tags, loot tables, recipes) into `src/generated/resources`: `.\gradlew.bat runData`
- Refresh dependencies after a `gradle.properties` version bump: `.\gradlew.bat --refresh-dependencies`
- Clean build outputs (does not touch source): `.\gradlew.bat clean`

There is no separate lint/test-runner command beyond Gradle's `build` (compile + `runGameTestServer`-style
in-game tests registered via NeoForge's gametest framework); there is no standalone unit test suite.

Client-side testing (screens, HUD, renderers) requires actually launching `runClient` and interacting in-game —
this can't be verified by compiling alone.

## Architecture

### Registration pattern

Every registrable game object (items, blocks, block entities, menus, entities, data components, data
attachments) follows the same `DeferredRegister` pattern: a `Mod*` class (e.g. `ModItems`, `ModBlocks`,
`ModDataComponents`, `ModDataAttachment`, `ModBlockEntities`, `ModEntities`, `ModMenuTypes`) owns a
`DeferredRegister`, exposes `public static final DeferredHolder/-Item/-Block` fields, and a
`register(IEventBus)` method. All of these `register(...)` calls are invoked from the mod constructor in
`WorldTriggerMod.java` — that constructor is the single place that wires up the mod's subsystems, so start
there when tracing how something gets registered.

`ModBlocks.registerBlock` auto-registers a matching `BlockItem` for every block through `ModItems.ITEMS`,
so adding a block does not require a separate item registration.

### Persistent/synced data: components vs. attachments

Two different NeoForge mechanisms are used for state, and picking the right one matters:

- **Data components** (`data/ModDataComponents.java`) attach state to an `ItemStack` (e.g. `IS_ON`, `MODE`,
  `HEALTH_DATA`, `INVENTORY_DATA`, `TRIGGER_CONFIGURE`, `TRIGGER_RECALL_POS`). Used for anything that should
  travel with the trigger item itself (it's dropped, given away, etc.). Each has a `Codec` for persistence and
  a `StreamCodec` for network sync, usually defined alongside a record type in `data/records/`.
- **Data attachments** (`data/ModDataAttachment.java`) attach state to an entity, e.g. `TRION_DATA` and
  `TRIGGER_STATE_DATA` on the player. Used for state that belongs to the player, survives across trigger
  items, and is configured with `.copyOnDeath()`. Backing types live in `data/custom/`.

When adding new persistent trigger/player state, decide item-scoped (component) vs. player-scoped
(attachment) first — this determines which registry and which package it belongs in.

### Trigger items

`item/custom/TriggerItem.java` is the base for all "Trigger" items (`ShieldTriggerItem`,
`AsteroidTriggerItem`, `KogetsuTriggerItem` extend or parallel it). The core mechanic is toggling a player
in/out of "trigger mode" on right-click (`use()`):
- Turning on: saves the player's inventory into the item's `INVENTORY_DATA` component, moves the trigger to
  hotbar slot 9, records current health, and populates the hotbar with the player's configured sub-triggers
  (`TRIGGER_CONFIGURE`).
- Turning off (`bailOut`, also called externally e.g. from block interactions): restores the saved inventory
  and slot position, restores health via an `AttributeModifier` keyed by a fixed `Identifier`, clears
  negative effects, and teleports the player to `TRIGGER_RECALL_POS` if bound (see `RecallBedBlock`).

`TriggerStateUtils` centralizes the on/off state toggle bookkeping shared across trigger items — reuse it
rather than duplicating state changes.

### Networking

`network/ModNetwork.java` registers all custom payloads in one place via `RegisterPayloadHandlersEvent`.
Payload record types live in `network/CommonPayload.java` (nested static classes), each with a `TYPE` and
`STREAM_CODEC`. Follow the existing pattern (`ChangeMode`, `TriggerPlacedBullet`, `SetPlayerSlot`) — a payload
per action, dispatched with `PacketDistributor.sendToPlayer`/`sendToServer`.

### Block entities / machine screens

Multi-slot machine blocks (e.g. `AssemblyBenchBlock`) share a small framework:
- `blocks/entity/base/BaseMachineBlockEntity` wraps an `ItemStacksResourceHandler` inventory, handles
  save/load via `ValueInput`/`ValueOutput`, and exposes `isItemValidForSlot`/`onInventoryChanged` hooks for
  subclasses.
- `blocks/menu/base/BaseMachineMenu` and `client/screen/base/BaseMachineScreen` are the corresponding
  menu/screen base classes.

New multi-slot machines should extend these bases rather than reimplementing inventory/menu/screen
boilerplate.

### Data generation

`WorldTriggerModDataGen` (triggered by `runData`, subscribed via `@EventBusSubscriber`) registers all
providers under `datagen/`: `ModModelProvider`, `ModBlockTagsProvider`, `ModItemTagsProvider`,
`ModBlockLootTableProvider`, and `ModRecipeProvider`. Output goes to `src/generated/resources`; hand-authored
resources in `src/main/resources` win over generated ones when both exist (`DuplicatesStrategy.EXCLUDE` in
`build.gradle`). After adding a new block/item/recipe, extend the relevant provider and run `runData` rather
than hand-writing JSON.

There's also a standalone Python/exe tool, `tools/gen_gui.py` / `tools/gen_gui.exe`, kept outside the Gradle
build — used for generating GUI-related assets separately from datagen.

### Assets/resources layout

Standard NeoForge layout under `src/main/resources`: `assets/wtmod/{blockstates,models,textures,lang}` and
`data/wtmod/tags`. Language keys follow `message.wtmod.*` (player-facing chat messages) and `tooltip.wtmod.*`
(item tooltips) conventions — check `assets/wtmod/lang` when adding new user-facing text.

## Notes

- `run/` is a local dev environment (saves, logs, configs, crash reports) created by `runClient`/`runServer` —
  not part of the mod source, safe to ignore.
- This is cloned from NeoForge's MDK template; template boilerplate (`Config.java`'s example values, the
  `reflectScreen` Gradle task) may still be present alongside real mod code.

## AI Assistant Guidelines & Constraints

To ensure smooth development and avoid common modding pitfalls with NeoForge 26.2, the AI assistant must strictly adhere to the following rules:

### Core Behaviors
- **No Hallucinations:** Minecraft/NeoForge APIs change drastically between versions. If you are unsure about a class, method signature, or rendering pipeline in version 26.2, **you must search the local codebase, decompiled sources, or NeoForge documentation first**. Do not guess or hallucinate API methods.
- **Precise Modifications:** When providing code fixes, output only the relevant changed blocks or snippets along with clear instructions on where to place them. Avoid re-outputting entire files unless explicitly requested.

### API Blacklist & Anti-Patterns
- 🚫 **No legacy Forge event buses:** Never use `MinecraftForge.EVENT_BUS.register()`. Always use NeoForge's `@EventBusSubscriber` or `NeoForge.EVENT_BUS`, and clearly distinguish between the Mod Bus and the Game Bus.
- 🚫 **No legacy registry events:** Never use 1.12/1.16 style `RegistryEvent`. Stick strictly to the `DeferredRegister` pattern already established in this project.
- 🚫 **No Fabric API:** Never import or suggest anything from `net.fabricmc.*`.
- 🚫 **No Hardcoding IDs:** Do not hardcode resource strings (e.g., `"wtmod:machine"`). Always use `ResourceLocation` (e.g., `ResourceLocation.fromNamespaceAndPath(...)`) or established constant classes.
- 🚫 **No legacy rendering:** Avoid outdated OpenGL calls or `RenderSystem.bindTexture()`. Use the modern rendering pipeline, `RenderType`, and modern GUI drawing methods.

### Debugging & Crash Resolution Workflow
When presented with a Gradle compilation error or a Minecraft crash log, follow this exact diagnostic process:
1. **Extract Core Info:** Identify the faulting class, exact line number, and the Exception type (e.g., `NullPointerException`, `AbstractMethodError`, `NoSuchMethodError`).
2. **Root Cause Analysis:** Check for common NeoForge pitfalls:
  - Was a `@OnlyIn(Dist.CLIENT)` / Client-only method called on the logical server?
  - Is it caused by a breaking API change in NeoForge 26.2 (e.g., registry changes, component/attachment changes)?
  - Is a BlockEntity or Menu attempting to render before being properly registered?
3. **Verify Signatures:** Before providing a fix, verify the correct method signature or class structure by reading the local decompiled classes.
4. **Actionable Fix:** Provide a concise code snippet that fixes the issue and briefly explain the underlying cause.