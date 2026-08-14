package com.JSUSHDX.WorldTriggerMod.blocks.menu;

import com.JSUSHDX.WorldTriggerMod.blocks.ModBlocks;
import com.JSUSHDX.WorldTriggerMod.blocks.entity.CombatSimulateConsoleEntity;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.base.BaseMachineMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CombatSimulateConsoleMenu extends BaseMachineMenu {
    private static final int PLAYER_INV_X = 9;
    private static final int PLAYER_INV_Y = 129;
    private static final int PLAYER_HOTBAR_Y = 187;

    private final CombatSimulateConsoleEntity blockEntity;
    private final ContainerLevelAccess access;

    private static CombatSimulateConsoleEntity getBlockEntity(Inventory playerInventory, RegistryFriendlyByteBuf data) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (be instanceof CombatSimulateConsoleEntity combatSimulateConsole) {
            return combatSimulateConsole;
        }
        throw new IllegalStateException("BlockEntity at position is not CombatSimulateConsoleEntity");
    }

    public CombatSimulateConsoleMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data));
    }

    public CombatSimulateConsoleMenu(int containerId, Inventory playerInventory, CombatSimulateConsoleEntity blockEntity) {
        super(ModMenuTypes.COMBAT_SIMULATE_CONSOLE_MENU.get(), containerId, blockEntity, CombatSimulateConsoleEntity.INVENTORY_SIZE);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        addPlayerInventory(playerInventory, PLAYER_INV_X, PLAYER_INV_Y, PLAYER_HOTBAR_Y);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.COMBAT_SIMULATE_CONSOLE.get());
    }

    public CombatSimulateConsoleEntity getBlockEntity() {
        return blockEntity;
    }
}
