package com.JSUSHDX.WorldTriggerMod.blocks.menu;

import com.JSUSHDX.WorldTriggerMod.blocks.ModBlocks;
import com.JSUSHDX.WorldTriggerMod.blocks.entity.OperatorsTerminalBlockEntity;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.base.BaseMachineMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;

public class OperatorsTerminalMenu extends BaseMachineMenu {
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 128;
    private static final int PLAYER_HOTBAR_Y = 186;

    private final OperatorsTerminalBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public OperatorsTerminalMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data));
    }

    public OperatorsTerminalMenu(int containerId, Inventory playerInventory, OperatorsTerminalBlockEntity blockEntity) {
        super(ModMenuTypes.OPERATORS_TERMINAL_MENU.get(), containerId, blockEntity, OperatorsTerminalBlockEntity.INVENTORY_SIZE);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        addPlayerInventory(playerInventory, PLAYER_INV_X, PLAYER_INV_Y, PLAYER_HOTBAR_Y);
    }

    private static OperatorsTerminalBlockEntity getBlockEntity(Inventory playerInventory, RegistryFriendlyByteBuf data) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (be instanceof OperatorsTerminalBlockEntity operatorsTerminal) {
            return operatorsTerminal;
        }
        throw new IllegalStateException("BlockEntity at position is not OperatorsTerminalBlockEntity");
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.OPERATORS_TERMINAL.get());
    }

    public OperatorsTerminalBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
