package com.JSUSHDX.WorldTriggerMod.blocks.entity;

import com.JSUSHDX.WorldTriggerMod.blocks.entity.base.BaseMachineBlockEntity;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.CombatSimulateConsoleMenu;
import com.JSUSHDX.WorldTriggerMod.data.records.CombatEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
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
     * combat_pool: the matrix of generated combat scenarios (time of day + weather)
     * accumulated by pressing the "generate" button on the console's screen.
     */
    private final List<CombatEnvironment> combatPool = new ArrayList<>();

    public CombatSimulateConsoleEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMBAT_SIMULATE_CONSOLE_BE.get(), pos, state, INVENTORY_SIZE);
    }

    /**
     * Rolls a random time-of-day/weather combination and appends it to the combat pool.
     */
    public void addRandomCombatEnvironment() {
        RandomSource random = level != null ? level.getRandom() : RandomSource.create();

        CombatEnvironment.TimeOfDay[] times = CombatEnvironment.TimeOfDay.values();
        CombatEnvironment.Weather[] weathers = CombatEnvironment.Weather.values();

        CombatEnvironment environment = new CombatEnvironment(
                times[random.nextInt(times.length)],
                weathers[random.nextInt(weathers.length)]
        );

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

    public List<CombatEnvironment> getCombatPool() {
        return List.copyOf(combatPool);
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
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        combatPool.clear();
        input.read("combat_pool", CombatEnvironment.CODEC.listOf()).ifPresent(combatPool::addAll);
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
