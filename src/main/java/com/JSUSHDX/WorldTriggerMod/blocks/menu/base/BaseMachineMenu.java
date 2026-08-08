package com.JSUSHDX.WorldTriggerMod.blocks.menu.base;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public abstract class BaseMachineMenu extends AbstractContainerMenu {
    protected final BlockEntity blockEntity;
    protected final int customSlotCount;

    protected BaseMachineMenu(MenuType<?> menuType, int containerId, BlockEntity blockEntity, int customSlotCount) {
        super(menuType, containerId);
        this.blockEntity = blockEntity;
        this.customSlotCount = customSlotCount;
    }

    /**
     * Helper to automatically lay out the player's 36 inventory slots.
     */
    protected void addPlayerInventory(Inventory playerInventory, int startX, int startY, int hotbarY) {
        // Player Inventory (27 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, 9 + (row * 9) + col, startX + (col * 18), startY + (row * 18)));
            }
        }
        // Player Hotbar (9 slots)
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, startX + (col * 18), hotbarY));
        }
    }

    /**
     * Generalized shift-click routing between custom slots and player inventory.
     */
    @NotNull
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            returnStack = slotStack.copy();

            // Shift-clicked from Custom Slots -> Move to Player Inventory
            if (slotIndex < customSlotCount) {
                if (!this.moveItemStackTo(slotStack, customSlotCount, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Shift-clicked from Player Inventory -> Move to Custom Slots
            else {
                if (!this.moveItemStackTo(slotStack, 0, customSlotCount, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == returnStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return returnStack;
    }
}
