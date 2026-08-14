package com.JSUSHDX.WorldTriggerMod.combat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

/**
 * Client-facing, display-only snapshot of a {@link CombatSimulation} - what the
 * Combat Simulate Console's "Current simulations" panel actually renders. Kept separate from
 * {@link CombatSimulation} because that class holds live server objects (levels, entities) that
 * can't be serialized to the client.
 */
public record CombatSimulationSummary(UUID id, String participantNames, String opponentName, int remainingSeconds) {
    public static final Codec<CombatSimulationSummary> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(CombatSimulationSummary::id),
            Codec.STRING.fieldOf("participants").forGetter(CombatSimulationSummary::participantNames),
            Codec.STRING.fieldOf("opponent").forGetter(CombatSimulationSummary::opponentName),
            Codec.INT.fieldOf("remaining_seconds").forGetter(CombatSimulationSummary::remainingSeconds)
    ).apply(instance, CombatSimulationSummary::new));
}
