package com.JSUSHDX.WorldTriggerMod.blocks.entity;

import com.JSUSHDX.WorldTriggerMod.blocks.entity.base.BaseMachineBlockEntity;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.CombatSimulateConsoleMenu;
import com.JSUSHDX.WorldTriggerMod.combat.CombatSimulationSummary;
import com.JSUSHDX.WorldTriggerMod.data.records.CombatEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CombatSimulateConsoleEntity extends BaseMachineBlockEntity implements MenuProvider {
    public static final int INVENTORY_SIZE = 0;

    /**
     * combat_pool: the matrix of combat scenarios (time of day + weather) the player has
     * chosen via {@code CombatEnvironmentPickerScreen} and added through the console's screen.
     */
    private final List<CombatEnvironment> combatPool = new ArrayList<>();

    /**
     * Display-only snapshot of the simulations {@link com.JSUSHDX.WorldTriggerMod.combat.CombatSimulateManager}
     * currently has running for this console. Pushed by the manager whenever a simulation
     * starts/ends and refreshed roughly once a second for the countdown. Persisted alongside
     * combat_pool for network sync convenience. Note: since CombatSimulateManager itself never
     * survives a restart, a row loaded from disk after a restart can briefly be a stale ghost with
     * a frozen countdown - harmless, and it self-corrects the next time this console starts or ends
     * a simulation (no manager state ever depends on this list).
     */
    private final List<CombatSimulationSummary> activeSimulations = new ArrayList<>();

    public CombatSimulateConsoleEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMBAT_SIMULATE_CONSOLE_BE.get(), pos, state, INVENTORY_SIZE);
    }

    /**
     * Appends a caller-chosen time-of-day/weather combination to the combat pool
     * (picked by the player through {@code CombatEnvironmentPickerScreen}).
     */
    public void addCombatEnvironment(CombatEnvironment environment) {
        combatPool.add(environment);
        setChanged();
        syncToClients();
    }

    /**
     * Removes the entry at the given index from the combat pool, if present.
     */
    public void removeCombatEnvironment(int index) {
        if (index >= 0 && index < combatPool.size()) {
            combatPool.remove(index);
            setChanged();
            syncToClients();
        }
    }

    /**
     * Replaces the entry at the given index in place (used by the pool row's gear/edit button -
     * reopens {@code CombatEnvironmentPickerScreen} pre-filled with the current time/weather and
     * overwrites that same slot on confirm, rather than appending a new entry).
     */
    public void updateCombatEnvironment(int index, CombatEnvironment environment) {
        if (index >= 0 && index < combatPool.size()) {
            combatPool.set(index, environment);
            setChanged();
            syncToClients();
        }
    }

    public List<CombatEnvironment> getCombatPool() {
        return List.copyOf(combatPool);
    }

    /** Called by {@code CombatSimulateManager} to push an updated ongoing-simulations snapshot. */
    public void setActiveSimulations(List<CombatSimulationSummary> summaries) {
        activeSimulations.clear();
        activeSimulations.addAll(summaries);
        setChanged();
        syncToClients();
    }

    public List<CombatSimulationSummary> getActiveSimulations() {
        return List.copyOf(activeSimulations);
    }

    private void syncToClients() {
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("combat_pool", CombatEnvironment.CODEC.listOf(), List.copyOf(combatPool));
        output.store("active_simulations", CombatSimulationSummary.CODEC.listOf(), List.copyOf(activeSimulations));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        combatPool.clear();
        input.read("combat_pool", CombatEnvironment.CODEC.listOf()).ifPresent(combatPool::addAll);
        activeSimulations.clear();
        input.read("active_simulations", CombatSimulationSummary.CODEC.listOf()).ifPresent(activeSimulations::addAll);
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

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.wtmod.combat_simulate_console");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CombatSimulateConsoleMenu(containerId, playerInventory, this);
    }
}
