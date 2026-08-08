package com.JSUSHDX.WorldTriggerMod.blocks.menu;

import com.JSUSHDX.WorldTriggerMod.blocks.ModBlocks;
import com.JSUSHDX.WorldTriggerMod.blocks.entity.AssemblyBenchBlockEntity;
import com.JSUSHDX.WorldTriggerMod.item.custom.TriggerItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.base.BaseMachineMenu;

public class AssemblyBenchMenu extends BaseMachineMenu {
    private final AssemblyBenchBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    // GUI layout constants
    private static final int TRIGGER_SLOT_X = 80;
    private static final int TRIGGER_SLOT_Y = 65;

    private static final int MAIN_SLOT_X = 26;
    private static final int SUB_SLOT_X = 134;
    private static final int CONFIG_SLOT_START_Y = 38;
    private static final int CONFIG_SLOT_SPACING = 18;

    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 128;
    private static final int PLAYER_HOTBAR_Y = 186;

    public AssemblyBenchMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data));
    }

    public AssemblyBenchMenu(int containerId, Inventory playerInventory, AssemblyBenchBlockEntity blockEntity) {
        super(ModMenuTypes.ASSEMBLY_BENCH_MENU.get(), containerId, blockEntity, AssemblyBenchBlockEntity.INVENTORY_SIZE);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        ItemStacksResourceHandler handler = blockEntity.getInventory();

        // Slot 0: Trigger Slot
        addSlot(new TriggerSlot(handler, AssemblyBenchBlockEntity.TRIGGER_SLOT, TRIGGER_SLOT_X, TRIGGER_SLOT_Y));

        // Slots 1-4: Main Slots
        for (int i = 0; i < 4; i++) {
            addSlot(new ConfigSlot(handler, AssemblyBenchBlockEntity.MAIN_SLOT_START + i,
                    MAIN_SLOT_X, CONFIG_SLOT_START_Y + i * CONFIG_SLOT_SPACING));
        }

        // Slots 5-8: Sub Slots
        for (int i = 0; i < 4; i++) {
            addSlot(new ConfigSlot(handler, AssemblyBenchBlockEntity.SUB_SLOT_START + i,
                    SUB_SLOT_X, CONFIG_SLOT_START_Y + i * CONFIG_SLOT_SPACING));
        }

        // Player Inventory and Hotbar (Standardized by BaseMachineMenu)
        addPlayerInventory(playerInventory, PLAYER_INV_X, PLAYER_INV_Y, PLAYER_HOTBAR_Y);
    }

    private static AssemblyBenchBlockEntity getBlockEntity(Inventory playerInventory, RegistryFriendlyByteBuf data) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (be instanceof AssemblyBenchBlockEntity assemblyBench) {
            return assemblyBench;
        }
        throw new IllegalStateException("BlockEntity at position is not AssemblyBenchBlockEntity");
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.ASSEMBLY_BENCH.get());
    }

    public AssemblyBenchBlockEntity getBlockEntity() {
        return blockEntity;
    }

    // ==================== Custom Slot Classes ====================

    private class TriggerSlot extends ResourceHandlerSlot {
        public TriggerSlot(ItemStacksResourceHandler handler, int index, int x, int y) {
            super(handler, handler::set, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof TriggerItem;
        }
    }

    private class ConfigSlot extends ResourceHandlerSlot {
        public ConfigSlot(ItemStacksResourceHandler handler, int index, int x, int y) {
            super(handler, handler::set, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            ItemResource res = blockEntity.getInventory().getResource(AssemblyBenchBlockEntity.TRIGGER_SLOT);
            if (res == null || res.isEmpty()) {
                return false;
            }
            return AssemblyBenchBlockEntity.isTriggerTypeItem(stack.getItem());
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
