package com.JSUSHDX.WorldTriggerMod.blocks.menu;

import com.JSUSHDX.WorldTriggerMod.blocks.ModBlocks;
import com.JSUSHDX.WorldTriggerMod.blocks.entity.MotherTriggerBlockEntity;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.base.BaseMachineMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MotherTriggerMenu extends BaseMachineMenu {
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 128;
    private static final int PLAYER_HOTBAR_Y = 186;

    private final MotherTriggerBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public MotherTriggerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data));
    }

    public MotherTriggerMenu(int containerId, Inventory playerInventory, MotherTriggerBlockEntity blockEntity) {
        super(ModMenuTypes.MOTHER_TRIGGER_MENU.get(), containerId, blockEntity, MotherTriggerBlockEntity.INVENTORY_SIZE);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        addPlayerInventory(playerInventory, PLAYER_INV_X, PLAYER_INV_Y, PLAYER_HOTBAR_Y);
    }

    private static MotherTriggerBlockEntity getBlockEntity(Inventory playerInventory, RegistryFriendlyByteBuf data) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (be instanceof MotherTriggerBlockEntity motherTrigger) {
            return motherTrigger;
        }
        throw new IllegalStateException("BlockEntity at position is not MotherTriggerBlockEntity");
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.MOTHER_TRIGGER.get());
    }

    public MotherTriggerBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
