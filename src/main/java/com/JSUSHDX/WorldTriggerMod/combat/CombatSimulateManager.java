package com.JSUSHDX.WorldTriggerMod.combat;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.blocks.entity.CombatSimulateConsoleEntity;
import com.JSUSHDX.WorldTriggerMod.data.records.CombatEnvironment;
import com.JSUSHDX.WorldTriggerMod.worldgen.CombatSimulateDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Server-side singleton owning every currently-running combat simulation: allocating a
 * coordinate slot inside the four shared combat_simulate dimensions, building/tearing down the
 * arena, spawning the opponent placeholder, and driving the win/timeout/out-of-bounds/bailout
 * lifecycle. Purely in-memory (see [[combat_simulate_console]]) - a server restart is treated as
 * every running simulation ending, so nothing here is written to disk.
 */
@EventBusSubscriber(modid = WorldTriggerMod.MODID)
public class CombatSimulateManager {
    public static final int ARENA_RADIUS = 24; // 48x48 footprint
    public static final int ARENA_SPACING = 64; // distance between slot origins, > arena diameter so slots never touch
    public static final int WALL_HEIGHT = 6;
    public static final long TIME_LIMIT_TICKS = 6000; // 5 minutes
    // How long the winning/surviving side lingers in the arena - seeing the result title - before
    // actually being teleported home once a fight concludes naturally (time up / team wiped).
    // Does NOT apply to individual eliminations (bailout/out-of-bounds/trion depleted, which still
    // return the player immediately) or to an admin's manual recall from the console.
    public static final long RESULT_DISPLAY_TICKS = 100; // 15 seconds
    private static final int TITLE_FADE_IN_TICKS = 10;
    private static final int TITLE_FADE_OUT_TICKS = 20;
    private static final int SLOTS_PER_ROW = 16;
    private static final int OUT_OF_BOUNDS_MARGIN = 4;

    private static final Map<UUID, CombatSimulation> ACTIVE_BY_ID = new HashMap<>();
    private static final Map<UUID, CombatSimulation> ACTIVE_BY_PLAYER = new HashMap<>();
    private static final Map<ResourceKey<Level>, Deque<Integer>> FREE_SLOTS = new HashMap<>();
    private static final Map<ResourceKey<Level>, Integer> NEXT_SLOT = new HashMap<>();

    private static long tickCounter = 0;

    private CombatSimulateManager() {
    }

    private static ResourceKey<Level> levelKeyFor(CombatEnvironment environment) {
        boolean night = environment.time() == CombatEnvironment.TimeOfDay.EVENING;
        boolean precip = environment.weather() != CombatEnvironment.Weather.SUNNY;

        if (night && precip) return CombatSimulateDimensions.NightPrecip.COMBAT_SIMULATE_LEVEL;
        if (night) return CombatSimulateDimensions.NightClear.COMBAT_SIMULATE_LEVEL;
        if (precip) return CombatSimulateDimensions.DayPrecip.COMBAT_SIMULATE_LEVEL;
        return CombatSimulateDimensions.DayClear.COMBAT_SIMULATE_LEVEL;
    }

    /**
     * Starts a new simulation for {@code player} using {@code environment}, teleporting them
     * into a freshly-built arena slot inside the matching combat_simulate dimension and spawning
     * the (currently fixed) opponent placeholder. No-ops with a chat message if the player is
     * already in a running simulation.
     */
    public static boolean start(ServerPlayer player, CombatSimulateConsoleEntity console, CombatEnvironment environment, int poolIndex) {
        if (ACTIVE_BY_PLAYER.containsKey(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("message.wtmod.combat_simulation_already_running"));
            return false;
        }

        Level rawConsoleLevel = console.getLevel();
        if (!(rawConsoleLevel instanceof ServerLevel consoleLevel)) {
            return false;
        }

        MinecraftServer server = consoleLevel.getServer();
        ResourceKey<Level> arenaKey = levelKeyFor(environment);
        ServerLevel arenaLevel = server.getLevel(arenaKey);
        if (arenaLevel == null) {
            WorldTriggerMod.LOGGER.error("Combat simulate dimension {} is not loaded", arenaKey.identifier());
            player.sendSystemMessage(Component.translatable("message.wtmod.combat_simulation_dimension_unavailable"));
            return false;
        }

        int slot = allocateSlot(arenaKey);
        BlockPos origin = slotOrigin(slot);
        // Force-load the arena's chunks for the simulation's lifetime - a freshly generated
        // dimension has nothing else keeping these chunks (loaded only in passing to place
        // blocks/spawn the opponent) ticking, and an entity added to a chunk that isn't kept
        // loaded can get silently discarded again almost immediately (observed 2026-08-14: golem
        // logged alive=true right after spawning, then isAlive()=false with full health on the
        // very next tick - see [[combat_simulate_console]]).
        setArenaChunksForced(arenaLevel, origin, true);
        buildArena(arenaLevel, origin);

        CombatSimulation simulation = new CombatSimulation(environment, arenaLevel, origin, slot,
                console.getBlockPos(), consoleLevel, tickCounter, TIME_LIMIT_TICKS);

        CombatSimulation.ReturnPoint returnPoint = new CombatSimulation.ReturnPoint(
                (ServerLevel) player.level(), player.position(), player.getYRot(), player.getXRot());

        BlockPos playerSpawn = origin.offset(0, 0, -(ARENA_RADIUS - 6));
        boolean teleported = player.teleportTo(arenaLevel, playerSpawn.getX() + 0.5, playerSpawn.getY(), playerSpawn.getZ() + 0.5,
                Set.of(), 180.0F, 0.0F, false);
        if (!teleported) {
            WorldTriggerMod.LOGGER.error("CombatSimulateManager: teleportTo() returned false moving {} into {} at slot {} (origin {})",
                    player.getGameProfile().name(), arenaKey.identifier(), slot, origin);
            freeSlot(arenaKey, slot);
            player.sendSystemMessage(Component.translatable("message.wtmod.combat_simulation_teleport_failed"));
            return false;
        }
        WorldTriggerMod.LOGGER.info("CombatSimulateManager: teleported {} to {} slot {} origin {}, now in level {}",
                player.getGameProfile().name(), arenaKey.identifier(), slot, origin, player.level().dimension().identifier());
        simulation.addPlayer(player, returnPoint);

        BlockPos opponentSpawn = origin.offset(0, 0, ARENA_RADIUS - 6);
        LivingEntity opponent = spawnOpponent(arenaLevel, opponentSpawn);
        simulation.addOpponent(opponent);
        WorldTriggerMod.LOGGER.info("CombatSimulateManager: spawned opponent {} (uuid {}) at {}, alive={}, valid={}",
                opponent.getName().getString(), opponent.getUUID(), opponentSpawn, opponent.isAlive(), opponent.isAddedToLevel());

        ACTIVE_BY_ID.put(simulation.getId(), simulation);
        ACTIVE_BY_PLAYER.put(player.getUUID(), simulation);
        updateBossBar(simulation);

        // A started scenario is a running instance now, not a repeatable menu option - remove it
        // from the console's pool immediately so it can't be selected again while this instance
        // is running (see [[combat_simulate_console]] for the earlier lock-based approach this
        // replaced).
        console.removeCombatEnvironment(poolIndex);
        console.setActiveSimulations(summariesForConsole(console));
        player.sendSystemMessage(Component.translatable("message.wtmod.combat_simulation_started"));
        return true;
    }

    /**
     * Called from {@code TriggerItem#bailOut} whenever a player turns their trigger off, whether
     * voluntarily (shift-click) or because their trion hit zero. If they're in an active
     * simulation, this handles the elimination/return-teleport instead of the trigger's own
     * recall-bed logic and returns true so the caller skips that.
     */
    public static boolean onPlayerBailout(ServerPlayer player, boolean trionDepleted) {
        CombatSimulation simulation = ACTIVE_BY_PLAYER.get(player.getUUID());
        if (simulation == null) {
            return false;
        }

        if (simulation.isEnding()) {
            // The fight is already over and they're just watching the result title - let them
            // skip the wait instead of trying to eliminate them from a fight that's already
            // decided.
            simulation.eliminatePlayer(player.getUUID());
            ACTIVE_BY_PLAYER.remove(player.getUUID());
            simulation.getBossEvent().removePlayer(player);
            player.connection.send(new ClientboundClearTitlesPacket(false));
            returnPlayer(simulation, player);
            return true;
        }

        CombatSimulation.EliminationCause cause = trionDepleted
                ? CombatSimulation.EliminationCause.TRION_DEPLETED
                : CombatSimulation.EliminationCause.BAILOUT;
        eliminatePlayer(simulation, player, cause);
        checkForWipe(simulation);
        return true;
    }

    /** Manual "force recall" from the console's ongoing-simulations panel. */
    public static void forceRecall(UUID simulationId) {
        CombatSimulation simulation = ACTIVE_BY_ID.get(simulationId);
        if (simulation != null) {
            end(simulation, CombatSimulation.EndReason.MANUAL_RECALL);
        }
    }



    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (ACTIVE_BY_ID.isEmpty()) {
            return;
        }

        for (CombatSimulation simulation : List.copyOf(ACTIVE_BY_ID.values())) {
            if (simulation.isEnding()) {
                if (tickCounter - simulation.getEndedAtTick() >= RESULT_DISPLAY_TICKS) {
                    finalizeEnd(simulation, simulation.getEndReason());
                }
                continue;
            }

            pruneDeadOpponents(simulation);
            updateBossBar(simulation);
            checkOutOfBounds(simulation);

            if (!ACTIVE_BY_ID.containsKey(simulation.getId())) {
                continue; // already ended by checkForWipe below during this pass
            }

            if (checkForWipe(simulation)) {
                continue;
            }

            if (tickCounter - simulation.getStartedAtTick() >= simulation.getTimeLimitTicks()) {
                end(simulation, CombatSimulation.EndReason.TIME_UP);
            }
        }

        if (tickCounter % 20 == 0) {
            refreshConsoles();
        }
    }

    /** Refreshes the boss-bar countdown (name + progress) shown above every alive participant. */
    private static void updateBossBar(CombatSimulation simulation) {
        long remaining = simulation.remainingTicks(tickCounter);
        float progress = simulation.getTimeLimitTicks() <= 0 ? 0.0F
                : Math.max(0.0F, Math.min(1.0F, (float) remaining / (float) simulation.getTimeLimitTicks()));
        simulation.getBossEvent().setProgress(progress);
        long seconds = remaining / 20;
        simulation.getBossEvent().setName(Component.translatable("gui.wtmod.combat_simulation_bossbar", seconds));
    }

    private static void pruneDeadOpponents(CombatSimulation simulation) {
        for (UUID opponentId : Set.copyOf(simulation.getAliveOpponents().keySet())) {
            LivingEntity opponent = simulation.getAliveOpponents().get(opponentId);
            if (opponent == null || !opponent.isAlive()) {
                WorldTriggerMod.LOGGER.info("CombatSimulateManager: pruning opponent {} from simulation {} (present={}, alive={}, health={})",
                        opponentId, simulation.getId(), opponent != null, opponent != null && opponent.isAlive(),
                        opponent != null ? opponent.getHealth() : -1);
                simulation.eliminateOpponent(opponentId);
            }
        }
    }

    private static void checkOutOfBounds(CombatSimulation simulation) {
        for (UUID playerId : Set.copyOf(simulation.getAlivePlayers())) {
            ServerPlayer player = findPlayer(simulation, playerId);
            if (player == null) {
                continue; // offline - leave them counted, they'll be swept up when the sim ends
            }
            if (player.level() != simulation.getArenaLevel() || !isWithinArena(simulation, player.blockPosition())) {
                WorldTriggerMod.LOGGER.info("CombatSimulateManager: {} out of bounds for simulation {} (playerLevel={}, arenaLevel={}, pos={})",
                        player.getGameProfile().name(), simulation.getId(), player.level().dimension().identifier(),
                        simulation.getArenaLevel().dimension().identifier(), player.blockPosition());
                eliminatePlayer(simulation, player, CombatSimulation.EliminationCause.OUT_OF_BOUNDS);
            }
        }
    }

    private static boolean isWithinArena(CombatSimulation simulation, BlockPos pos) {
        BlockPos origin = simulation.getArenaOrigin();
        return Math.abs(pos.getX() - origin.getX()) <= ARENA_RADIUS + OUT_OF_BOUNDS_MARGIN
                && Math.abs(pos.getZ() - origin.getZ()) <= ARENA_RADIUS + OUT_OF_BOUNDS_MARGIN;
    }

    /** @return true if the simulation was ended as a result of this wipe check. */
    private static boolean checkForWipe(CombatSimulation simulation) {
        if (simulation.isPlayersWiped() || simulation.isOpponentsWiped()) {
            end(simulation, CombatSimulation.EndReason.TEAM_ELIMINATED);
            return true;
        }
        return false;
    }

    private static void eliminatePlayer(CombatSimulation simulation, ServerPlayer player, CombatSimulation.EliminationCause cause) {
        simulation.eliminatePlayer(player.getUUID());
        simulation.getBossEvent().removePlayer(player);
        ACTIVE_BY_PLAYER.remove(player.getUUID());
        returnPlayer(simulation, player);

        Component msg = switch (cause) {
            case OUT_OF_BOUNDS -> Component.translatable("message.wtmod.combat_simulation_out_of_bounds");
            case TRION_DEPLETED -> Component.translatable("message.wtmod.combat_simulation_trion_depleted");
            case BAILOUT -> Component.translatable("message.wtmod.combat_simulation_bailout");
        };
        player.sendSystemMessage(msg);
    }

    private static void returnPlayer(CombatSimulation simulation, ServerPlayer player) {
        CombatSimulation.ReturnPoint point = simulation.getReturnPoint(player.getUUID());
        if (point == null) {
            return;
        }
        player.teleportTo(point.level(), point.pos().x, point.pos().y, point.pos().z,
                Set.of(), point.yaw(), point.pitch(), false);
    }

    /**
     * Entry point for a fight concluding. A manual recall is finalized immediately (no result to
     * show); a natural conclusion (time up / team wiped) instead announces the result to whoever
     * is still standing and only actually tears the arena down/teleports them home after
     * {@link #RESULT_DISPLAY_TICKS} have passed - see the {@code isEnding()} branch in
     * {@link #onServerTick}.
     */
    private static void end(CombatSimulation simulation, CombatSimulation.EndReason reason) {
        if (reason == CombatSimulation.EndReason.MANUAL_RECALL) {
            finalizeEnd(simulation, reason);
            return;
        }

        simulation.markEnding(reason, tickCounter);
        simulation.getBossEvent().removeAllPlayers();

        Component resultTitle = resultTitleFor(simulation, reason);
        int stayTicks = (int) Math.max(0, RESULT_DISPLAY_TICKS - TITLE_FADE_IN_TICKS - TITLE_FADE_OUT_TICKS);
        for (UUID playerId : simulation.getAlivePlayers()) {
            ServerPlayer player = findPlayer(simulation, playerId);
            if (player != null) {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(TITLE_FADE_IN_TICKS, stayTicks, TITLE_FADE_OUT_TICKS));
                player.connection.send(new ClientboundSetTitleTextPacket(resultTitle));
            }
        }

        CombatSimulateConsoleEntity console = findConsole(simulation);
        if (console != null) {
            console.setActiveSimulations(summariesForConsole(console));
        }
    }

    /** Which result title to show the side(s) still standing when a fight concludes naturally. */
    private static Component resultTitleFor(CombatSimulation simulation, CombatSimulation.EndReason reason) {
        if (reason == CombatSimulation.EndReason.TEAM_ELIMINATED) {
            return simulation.isOpponentsWiped()
                    ? Component.translatable("message.wtmod.combat_simulation_result_win")
                    : Component.translatable("message.wtmod.combat_simulation_result_lose");
        }
        return Component.translatable("message.wtmod.combat_simulation_result_draw"); // TIME_UP
    }

    /** Actually tears the arena down and teleports whoever is left back home. */
    private static void finalizeEnd(CombatSimulation simulation, CombatSimulation.EndReason reason) {
        ACTIVE_BY_ID.remove(simulation.getId());
        simulation.getBossEvent().removeAllPlayers();

        // Anyone still alive when the simulation itself ends (timeout, their team won, manual
        // recall) also needs to go home - individual eliminations already sent people back.
        for (UUID playerId : simulation.getAlivePlayers()) {
            ServerPlayer player = findPlayer(simulation, playerId);
            ACTIVE_BY_PLAYER.remove(playerId);
            if (player != null) {
                player.connection.send(new ClientboundClearTitlesPacket(false));
                returnPlayer(simulation, player);
                Component msg = switch (reason) {
                    case TIME_UP -> Component.translatable("message.wtmod.combat_simulation_time_up");
                    case MANUAL_RECALL -> Component.translatable("message.wtmod.combat_simulation_recalled");
                    case TEAM_ELIMINATED -> Component.translatable("message.wtmod.combat_simulation_victory");
                };
                player.sendSystemMessage(msg);
            }
        }

        // Sweep the whole arena footprint, not just the entities this simulation was tracking -
        // catches item drops (e.g. from a killed opponent), stray projectiles, and any opponent
        // that somehow fell out of tracking, so the slot is actually clean before it gets reused.
        clearArenaEntities(simulation.getArenaLevel(), simulation.getArenaOrigin());

        setArenaChunksForced(simulation.getArenaLevel(), simulation.getArenaOrigin(), false);
        freeSlot(simulation.getArenaLevel().dimension(), simulation.getSlotIndex());

        CombatSimulateConsoleEntity console = findConsole(simulation);
        if (console != null) {
            console.setActiveSimulations(summariesForConsole(console));
        }
    }

    private static void refreshConsoles() {
        Set<CombatSimulateConsoleEntity> consoles = new HashSet<>();
        for (CombatSimulation simulation : ACTIVE_BY_ID.values()) {
            CombatSimulateConsoleEntity console = findConsole(simulation);
            if (console != null) {
                consoles.add(console);
            }
        }
        for (CombatSimulateConsoleEntity console : consoles) {
            console.setActiveSimulations(summariesForConsole(console));
        }
    }

    private static List<CombatSimulationSummary> summariesForConsole(CombatSimulateConsoleEntity console) {
        List<CombatSimulationSummary> summaries = new ArrayList<>();
        for (CombatSimulation simulation : ACTIVE_BY_ID.values()) {
            if (!simulation.getConsolePos().equals(console.getBlockPos()) || simulation.getConsoleLevel() != console.getLevel()) {
                continue;
            }

            String participants = simulation.getAlivePlayers().stream()
                    .map(id -> findPlayer(simulation, id))
                    .filter(Objects::nonNull)
                    .map(p -> p.getGameProfile().name())
                    .collect(Collectors.joining(", "));

            String opponents = simulation.getAliveOpponents().values().stream()
                    .map(entity -> entity.getName().getString())
                    .collect(Collectors.joining(", "));

            int remainingSeconds = (int) (simulation.remainingTicks(tickCounter) / 20);
            summaries.add(new CombatSimulationSummary(simulation.getId(), participants, opponents, remainingSeconds));
        }
        return summaries;
    }

    private static CombatSimulateConsoleEntity findConsole(CombatSimulation simulation) {
        if (simulation.getConsoleLevel().getBlockEntity(simulation.getConsolePos()) instanceof CombatSimulateConsoleEntity console) {
            return console;
        }
        return null;
    }

    private static ServerPlayer findPlayer(CombatSimulation simulation, UUID playerId) {
        return simulation.getConsoleLevel().getServer().getPlayerList().getPlayer(playerId);
    }

    private static int allocateSlot(ResourceKey<Level> levelKey) {
        Deque<Integer> free = FREE_SLOTS.computeIfAbsent(levelKey, key -> new ArrayDeque<>());
        if (!free.isEmpty()) {
            return free.pop();
        }
        return NEXT_SLOT.merge(levelKey, 1, Integer::sum) - 1;
    }

    private static void freeSlot(ResourceKey<Level> levelKey, int slot) {
        FREE_SLOTS.computeIfAbsent(levelKey, key -> new ArrayDeque<>()).push(slot);
    }

    private static BlockPos slotOrigin(int slot) {
        int row = slot / SLOTS_PER_ROW;
        int col = slot % SLOTS_PER_ROW;
        return new BlockPos(col * ARENA_SPACING, 4, row * ARENA_SPACING);
    }

    /**
     * Removes every non-player entity still inside the arena footprint (item drops, arrows,
     * the opponent if it's still alive, anything else that ended up there) so a reused slot
     * starts clean for the next simulation.
     */
    private static void clearArenaEntities(ServerLevel level, BlockPos origin) {
        AABB bounds = new AABB(
                origin.getX() - ARENA_RADIUS, origin.getY() - 4, origin.getZ() - ARENA_RADIUS,
                origin.getX() + ARENA_RADIUS + 1, origin.getY() + WALL_HEIGHT + 4, origin.getZ() + ARENA_RADIUS + 1
        );
        for (Entity entity : level.getEntities((Entity) null, bounds, e -> !(e instanceof ServerPlayer))) {
            entity.discard();
        }
    }

    /** Force-loads (or releases) every chunk the arena footprint overlaps, plus a small margin. */
    private static void setArenaChunksForced(ServerLevel level, BlockPos origin, boolean forced) {
        int minChunkX = (origin.getX() - ARENA_RADIUS - 4) >> 4;
        int maxChunkX = (origin.getX() + ARENA_RADIUS + 4) >> 4;
        int minChunkZ = (origin.getZ() - ARENA_RADIUS - 4) >> 4;
        int maxChunkZ = (origin.getZ() + ARENA_RADIUS + 4) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                level.setChunkForced(chunkX, chunkZ, forced);
            }
        }
    }

    /**
     * Floor: red concrete. Walls: blue concrete around the perimeter, {@link #WALL_HEIGHT} tall.
     * No ceiling - deliberately left open per the agreed arena look.
     */
    private static void buildArena(ServerLevel level, BlockPos origin) {
        BlockState floor = Blocks.CONCRETE.pick(DyeColor.RED).defaultBlockState();
        BlockState wall = Blocks.CONCRETE.pick(DyeColor.BLUE).defaultBlockState();

        for (int x = -ARENA_RADIUS; x <= ARENA_RADIUS; x++) {
            for (int z = -ARENA_RADIUS; z <= ARENA_RADIUS; z++) {
                level.setBlock(origin.offset(x, -1, z), floor, Block.UPDATE_CLIENTS);

                boolean edge = x == -ARENA_RADIUS || x == ARENA_RADIUS || z == -ARENA_RADIUS || z == ARENA_RADIUS;
                if (edge) {
                    for (int y = 0; y < WALL_HEIGHT; y++) {
                        level.setBlock(origin.offset(x, y, z), wall, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    /**
     * Spawns the (currently fixed) opponent placeholder - a vanilla Villager with unmodified
     * AI, per the agreed design. Swap this method's source entity to support other mobs/players
     * as opponents later; nothing else in the manager needs to change.
     */
    private static LivingEntity spawnOpponent(ServerLevel level, BlockPos pos) {
        Villager golem = new Villager(EntityTypes.VILLAGER, level);
        golem.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 180.0F, 0.0F);
        golem.setPersistenceRequired();
        level.addFreshEntity(golem);
        return golem;
    }
}
