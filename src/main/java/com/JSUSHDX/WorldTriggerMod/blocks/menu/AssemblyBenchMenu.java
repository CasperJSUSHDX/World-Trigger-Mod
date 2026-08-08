package com.JSUSHDX.WorldTriggerMod.blocks.menu;

import com.JSUSHDX.WorldTriggerMod.blocks.ModBlocks;
import com.JSUSHDX.WorldTriggerMod.blocks.entity.AssemblyBenchBlockEntity;
import com.JSUSHDX.WorldTriggerMod.item.custom.TriggerItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

@SuppressWarnings("removal")
public class AssemblyBenchMenu extends AbstractContainerMenu {
    private final AssemblyBenchBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    // GUI layout constants (pixel positions within the GUI texture)
    private static final int TRIGGER_SLOT_X = 80;
    private static final int TRIGGER_SLOT_Y = 65;

    private static final int MAIN_SLOT_X = 26;
    private static final int SUB_SLOT_X = 134;
    private static final int CONFIG_SLOT_START_Y = 38;
    private static final int CONFIG_SLOT_SPACING = 18;

    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 128;
    private static final int PLAYER_HOTBAR_Y = 186;

    /**
     * Client-side constructor — called from MenuType factory.
     */
    public AssemblyBenchMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data));
    }

    /**
     * Server-side constructor — called from BlockEntity.createMenu().
     */
    public AssemblyBenchMenu(int containerId, Inventory playerInventory, AssemblyBenchBlockEntity blockEntity) {
        super(ModMenuTypes.ASSEMBLY_BENCH_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        ItemStackHandler handler = blockEntity.getInventory();

        // === Custom Slots (0-8) ===

        // Slot 0: Trigger Slot (center) — only accepts TriggerItem
        addSlot(new TriggerSlot(handler, AssemblyBenchBlockEntity.TRIGGER_SLOT,
                TRIGGER_SLOT_X, TRIGGER_SLOT_Y));

        // Slots 1-4: Main Slots (left column)
        for (int i = 0; i < 4; i++) {
            addSlot(new ConfigSlot(handler, AssemblyBenchBlockEntity.MAIN_SLOT_START + i,
                    MAIN_SLOT_X, CONFIG_SLOT_START_Y + i * CONFIG_SLOT_SPACING));
        }

        // Slots 5-8: Sub Slots (right column)
        for (int i = 0; i < 4; i++) {
            addSlot(new ConfigSlot(handler, AssemblyBenchBlockEntity.SUB_SLOT_START + i,
                    SUB_SLOT_X, CONFIG_SLOT_START_Y + i * CONFIG_SLOT_SPACING));
        }

        // === Player Inventory (slots 9-35) ===
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
            }
        }

        // === Player Hotbar (slots 36-44) ===
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col,
                    PLAYER_INV_X + col * 18, PLAYER_HOTBAR_Y));
        }
    }

    private static AssemblyBenchBlockEntity getBlockEntity(Inventory playerInventory, RegistryFriendlyByteBuf data) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (be instanceof AssemblyBenchBlockEntity assemblyBench) {
            return assemblyBench;
        }
        throw new IllegalStateException("BlockEntity at position is not AssemblyBenchBlockEntity");
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack = slot.getItem();
        ItemStack originalStack = slotStack.copy();

        // Custom slots (0-8) → Player inventory (9-44)
        if (slotIndex < 9) {
            if (!this.moveItemStackTo(slotStack, 9, 45, true)) {
                return ItemStack.EMPTY;
            }
        }
        // Player inventory (9-44) → Custom slots (0-8)
        else {
            // Try trigger slot first if it's a TriggerItem
            if (slotStack.getItem() instanceof TriggerItem) {
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Try main/sub slots if it's a trigger-type item
            else if (AssemblyBenchBlockEntity.isTriggerTypeItem(slotStack)) {
                if (!this.moveItemStackTo(slotStack, 1, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Move between inventory and hotbar
            else if (slotIndex < 36) {
                if (!this.moveItemStackTo(slotStack, 36, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(slotStack, 9, 36, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, slotStack);
        return originalStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.ASSEMBLY_BENCH.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }

    public AssemblyBenchBlockEntity getBlockEntity() {
        return blockEntity;
    }

    // ==================== Custom Slot Classes ====================

    /**
     * Trigger Slot — only accepts TriggerItem.
     * Handles config load on placement and config save on removal.
     */
    private class TriggerSlot extends SlotItemHandler {
        public TriggerSlot(ItemStackHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof TriggerItem;
        }

    }

    /**
     * Config Slot (Main/Sub) — only accepts trigger-type items.
     * Only interactable when a TRIGGER is in the center slot.
     */
    private class ConfigSlot extends SlotItemHandler {
        public ConfigSlot(ItemStackHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // Only allow placement if there's a trigger in the center slot
            ItemStack triggerStack = blockEntity.getInventory()
                    .getStackInSlot(AssemblyBenchBlockEntity.TRIGGER_SLOT);
            if (triggerStack.isEmpty()) {
                return false;
            }
            return AssemblyBenchBlockEntity.isTriggerTypeItem(stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            // Allow pickup even without trigger (for edge cases)
            return true;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
