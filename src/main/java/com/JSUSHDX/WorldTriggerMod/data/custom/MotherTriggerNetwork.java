package com.JSUSHDX.WorldTriggerMod.data.custom;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tracks Mother Trigger block positions per dimension so trigger-network machines
 * (assembly bench, recall bed, ...) can check whether they're within range of one.
 */
public class MotherTriggerNetwork extends SavedData {
    public static final double RANGE = 32.0;

    public static final Codec<MotherTriggerNetwork> CODEC = BlockPos.CODEC.listOf()
            .xmap(MotherTriggerNetwork::new, network -> List.copyOf(network.positions));

    public static final SavedDataType<MotherTriggerNetwork> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "mother_trigger_network"),
            MotherTriggerNetwork::new,
            CODEC);

    private final Set<BlockPos> positions = new HashSet<>();

    public MotherTriggerNetwork() {
    }

    private MotherTriggerNetwork(List<BlockPos> positions) {
        this.positions.addAll(positions);
    }

    public static MotherTriggerNetwork get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public static boolean isNearMotherTrigger(Level level, BlockPos pos) {
        return level instanceof ServerLevel serverLevel && get(serverLevel).isWithinRange(pos);
    }

    public void register(BlockPos pos) {
        if (positions.add(pos.immutable())) {
            setDirty();
        }
    }

    public void unregister(BlockPos pos) {
        if (positions.remove(pos)) {
            setDirty();
        }
    }

    public boolean isWithinRange(BlockPos pos) {
        for (BlockPos motherTriggerPos : positions) {
            if (motherTriggerPos.distSqr(pos) <= RANGE * RANGE) {
                return true;
            }
        }
        return false;
    }
}
