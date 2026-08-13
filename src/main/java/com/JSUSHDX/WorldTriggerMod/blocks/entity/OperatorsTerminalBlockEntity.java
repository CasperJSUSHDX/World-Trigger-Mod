package com.JSUSHDX.WorldTriggerMod.blocks.entity;

import com.JSUSHDX.WorldTriggerMod.blocks.entity.base.BaseMachineBlockEntity;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.OperatorsTerminalMenu;
import com.JSUSHDX.WorldTriggerMod.data.records.TerminalEntry;
import com.JSUSHDX.WorldTriggerMod.util.TrionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OperatorsTerminalBlockEntity extends BaseMachineBlockEntity implements MenuProvider {
    public static final int INVENTORY_SIZE = 0;

    private final Map<UUID, TerminalEntry> entries = new LinkedHashMap<>();

    public OperatorsTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OPERATORS_TERMINAL_BE.get(), pos, state, INVENTORY_SIZE);
    }

    public void registerEntry(Player player) {
        UUID id = player.getUUID();
        entries.put(id, new TerminalEntry(id, player.getGameProfile().name(),
                TrionUtils.getTrion(player), TrionUtils.getMaxTrion(player)));
        setChanged();
        syncToClients();
    }

    public void removeEntry(UUID ownerId) {
        if (entries.remove(ownerId) != null) {
            setChanged();
            syncToClients();
        }
    }

    /**
     * Refreshes trion for currently-online owners in place, then pushes an updated sync packet.
     * Offline owners keep their last-registered snapshot.
     */
    public void refreshOnlineEntries() {
        if (level instanceof ServerLevel serverLevel) {
            for (TerminalEntry entry : List.copyOf(entries.values())) {
                ServerPlayer online = serverLevel.getServer().getPlayerList().getPlayer(entry.ownerId());
                if (online != null) {
                    entries.put(entry.ownerId(), new TerminalEntry(entry.ownerId(), entry.ownerName(),
                            TrionUtils.getTrion(online), TrionUtils.getMaxTrion(online)));
                }
            }
        }
        syncToClients();
    }

    public List<TerminalEntry> getEntries() {
        return List.copyOf(entries.values());
    }

    private void syncToClients() {
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("entries", TerminalEntry.CODEC.listOf(), List.copyOf(entries.values()));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        entries.clear();
        input.read("entries", TerminalEntry.CODEC.listOf())
                .ifPresent(list -> list.forEach(e -> entries.put(e.ownerId(), e)));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NonNull
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.wtmod.operators_terminal");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new OperatorsTerminalMenu(containerId, playerInventory, this);
    }
}
