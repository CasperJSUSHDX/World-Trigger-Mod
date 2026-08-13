package com.JSUSHDX.WorldTriggerMod.blocks.entity;

import com.JSUSHDX.WorldTriggerMod.blocks.entity.base.BaseMachineBlockEntity;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.MotherTriggerMenu;
import com.JSUSHDX.WorldTriggerMod.data.custom.MotherTriggerNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class MotherTriggerBlockEntity extends BaseMachineBlockEntity implements MenuProvider {
    public static final int INVENTORY_SIZE = 0;

    public MotherTriggerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOTHER_TRIGGER_BE.get(), pos, state, INVENTORY_SIZE);
    }

    public double getRange() {
        return MotherTriggerNetwork.RANGE;
    }

    @NonNull
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.wtmod.mother_trigger");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MotherTriggerMenu(containerId, playerInventory, this);
    }
}
