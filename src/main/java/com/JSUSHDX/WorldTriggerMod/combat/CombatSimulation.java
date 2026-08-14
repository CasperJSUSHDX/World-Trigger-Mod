package com.JSUSHDX.WorldTriggerMod.combat;

import com.JSUSHDX.WorldTriggerMod.data.records.CombatEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Runtime state for one running combat simulation, owned by {@link CombatSimulateManager}.
 * Purely in-memory - never persisted (see [[combat_simulate_console]] design notes): a server
 * restart is treated as every running simulation ending on its own, since there is no sane way
 * to resume a mid-fight arena/opponent/return-teleport state across a restart.
 */
public class CombatSimulation {
    public enum EndReason {
        TIME_UP,
        TEAM_ELIMINATED,
        MANUAL_RECALL
    }

    public enum EliminationCause {
        TRION_DEPLETED,
        OUT_OF_BOUNDS,
        BAILOUT
    }

    /** Where a participant should be sent back to once they leave the simulation. */
    public record ReturnPoint(ServerLevel level, Vec3 pos, float yaw, float pitch) {
    }

    private final UUID id = UUID.randomUUID();
    private final CombatEnvironment environment;
    private final ServerLevel arenaLevel;
    private final BlockPos arenaOrigin;
    private final int slotIndex;
    private final BlockPos consolePos;
    private final ServerLevel consoleLevel;
    private final long startedAtTick;
    private final long timeLimitTicks;

    // Boss-bar style countdown shown above every participant for the duration of the fight -
    // membership is kept in sync with alivePlayers in addPlayer()/CombatSimulateManager's
    // eliminatePlayer(), and cleared entirely once the result is announced (see markEnding()).
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            UUID.randomUUID(), Component.translatable("gui.wtmod.combat_simulation_bossbar"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    private final Set<UUID> alivePlayers = new LinkedHashSet<>();
    private final Map<UUID, ReturnPoint> returnPoints = new LinkedHashMap<>();
    private final Map<UUID, LivingEntity> aliveOpponents = new LinkedHashMap<>();

    // Set once the fight has concluded (time up / a team wiped) but before the winning side is
    // actually teleported home - CombatSimulateManager holds the simulation in this state for
    // RESULT_DISPLAY_TICKS so the result title has time to be seen. Null while still ongoing.
    private EndReason endReason;
    private long endedAtTick = -1;

    public CombatSimulation(CombatEnvironment environment, ServerLevel arenaLevel, BlockPos arenaOrigin, int slotIndex,
                             BlockPos consolePos, ServerLevel consoleLevel, long startedAtTick, long timeLimitTicks) {
        this.environment = environment;
        this.arenaLevel = arenaLevel;
        this.arenaOrigin = arenaOrigin;
        this.slotIndex = slotIndex;
        this.consolePos = consolePos;
        this.consoleLevel = consoleLevel;
        this.startedAtTick = startedAtTick;
        this.timeLimitTicks = timeLimitTicks;
    }

    public UUID getId() {
        return id;
    }

    public CombatEnvironment getEnvironment() {
        return environment;
    }

    public ServerLevel getArenaLevel() {
        return arenaLevel;
    }

    public BlockPos getArenaOrigin() {
        return arenaOrigin;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public BlockPos getConsolePos() {
        return consolePos;
    }

    public ServerLevel getConsoleLevel() {
        return consoleLevel;
    }

    public long getStartedAtTick() {
        return startedAtTick;
    }

    public long getTimeLimitTicks() {
        return timeLimitTicks;
    }

    public ServerBossEvent getBossEvent() {
        return bossEvent;
    }

    public void addPlayer(ServerPlayer player, ReturnPoint returnPoint) {
        alivePlayers.add(player.getUUID());
        returnPoints.put(player.getUUID(), returnPoint);
        bossEvent.addPlayer(player);
    }

    public void addOpponent(LivingEntity entity) {
        aliveOpponents.put(entity.getUUID(), entity);
    }

    public Set<UUID> getAlivePlayers() {
        return Set.copyOf(alivePlayers);
    }

    public Map<UUID, LivingEntity> getAliveOpponents() {
        return Map.copyOf(aliveOpponents);
    }

    public ReturnPoint getReturnPoint(UUID playerId) {
        return returnPoints.get(playerId);
    }

    public void eliminatePlayer(UUID playerId) {
        alivePlayers.remove(playerId);
    }

    public void eliminateOpponent(UUID entityId) {
        aliveOpponents.remove(entityId);
    }

    public boolean isPlayersWiped() {
        return alivePlayers.isEmpty();
    }

    public boolean isOpponentsWiped() {
        return aliveOpponents.isEmpty();
    }

    public long remainingTicks(long currentTick) {
        return Math.max(0, (startedAtTick + timeLimitTicks) - currentTick);
    }

    /** Marks the fight as concluded - {@code reason} and {@code tick} it happened at. */
    public void markEnding(EndReason reason, long tick) {
        this.endReason = reason;
        this.endedAtTick = tick;
    }

    /** True once the fight has a result (time up / team wiped) and is waiting to be finalized. */
    public boolean isEnding() {
        return endReason != null;
    }

    public EndReason getEndReason() {
        return endReason;
    }

    public long getEndedAtTick() {
        return endedAtTick;
    }
}
