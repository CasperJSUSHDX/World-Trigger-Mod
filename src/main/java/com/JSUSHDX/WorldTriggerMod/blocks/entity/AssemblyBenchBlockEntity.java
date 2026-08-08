package com.JSUSHDX.WorldTriggerMod.blocks.entity;

import com.JSUSHDX.WorldTriggerMod.blocks.menu.AssemblyBenchMenu;
import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.data.records.TriggerConfigureData;
import com.JSUSHDX.WorldTriggerMod.item.custom.AsteroidTriggerItem;
import com.JSUSHDX.WorldTriggerMod.item.custom.KogetsuTriggerItem;
import com.JSUSHDX.WorldTriggerMod.item.custom.ShieldTriggerItem;
import com.JSUSHDX.WorldTriggerMod.item.custom.TriggerItem;
import com.JSUSHDX.WorldTriggerMod.blocks.entity.base.BaseMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssemblyBenchBlockEntity extends BaseMachineBlockEntity implements MenuProvider {
    public static final int INVENTORY_SIZE = 9;
    
    // Slot Constants
    public static final int TRIGGER_SLOT = 0;
    public static final int MAIN_SLOT_START = 1;  // Main slots: 1, 2, 3, 4
    public static final int MAIN_SLOT_END = 4;    // inclusive
    public static final int SUB_SLOT_START = 5;    // Sub slots: 5, 6, 7, 8
    public static final int SUB_SLOT_END = 8;      // inclusive

    private boolean isUpdating = false;

    public AssemblyBenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASSEMBLY_BENCH_BE.get(), pos, state, INVENTORY_SIZE);
    }

    private ItemStack getSlotStack(int slot) {
        ItemResource res = inventory.getResource(slot);
        long amount = inventory.getAmountAsLong(slot);
        return (res != null && amount > 0) ? res.toStack((int) amount) : ItemStack.EMPTY;
    }

    private void setSlotStack(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            inventory.set(slot, ItemResource.EMPTY, 0);
        } else {
            inventory.set(slot, ItemResource.of(stack), stack.getCount());
        }
    }

    @Override
    protected void onInventoryChanged(int slot) {
        if (isUpdating) return;
        
        if (slot == TRIGGER_SLOT) {
            ItemStack triggerStack = getSlotStack(TRIGGER_SLOT);
            if (triggerStack.isEmpty()) {
                isUpdating = true;
                clearConfigSlots();
                isUpdating = false;
            } else if (triggerStack.getItem() instanceof TriggerItem) {
                isUpdating = true;
                loadTriggerConfig(triggerStack);
                isUpdating = false;
            }
        } else if (slot >= MAIN_SLOT_START && slot <= SUB_SLOT_END) {
            ItemStack triggerStack = getSlotStack(TRIGGER_SLOT);
            if (!triggerStack.isEmpty() && triggerStack.getItem() instanceof TriggerItem) {
                isUpdating = true;
                saveTriggerConfig(triggerStack);
                isUpdating = false;
            }
        }
    }

    @Override
    protected boolean isItemValidForSlot(int slot, ItemResource resource) {
        if (resource == null || resource.isEmpty()) return true;
        if (slot == TRIGGER_SLOT) {
            return resource.getItem() instanceof TriggerItem;
        }
        return isTriggerTypeItem(resource.getItem());
    }

    public static boolean isTriggerTypeItem(Item item) {
        return item instanceof AsteroidTriggerItem
                || item instanceof KogetsuTriggerItem
                || item instanceof ShieldTriggerItem;
    }

    public void loadTriggerConfig(ItemStack triggerStack) {
        TriggerConfigureData config = triggerStack.get(ModDataComponents.TRIGGER_CONFIGURE);
        if (config != null && config.triggers() != null) {
            List<Item> triggers = config.triggers();
            // Main slots
            for (int i = 0; i < 4 && i < triggers.size(); i++) {
                Item item = triggers.get(i);
                setSlotStack(MAIN_SLOT_START + i,
                        (item != null && item != Items.AIR) ? new ItemStack(item) : ItemStack.EMPTY);
            }
            // Sub slots
            for (int i = 4; i < 8 && i < triggers.size(); i++) {
                Item item = triggers.get(i);
                setSlotStack(SUB_SLOT_START + (i - 4),
                        (item != null && item != Items.AIR) ? new ItemStack(item) : ItemStack.EMPTY);
            }
        }
        setChanged();
    }

    public void saveTriggerConfig(ItemStack triggerStack) {
        List<Item> triggers = new ArrayList<>(Collections.nCopies(8, Items.AIR));

        // Main slots
        for (int i = 0; i < 4; i++) {
            ItemStack stack = getSlotStack(MAIN_SLOT_START + i);
            if (!stack.isEmpty()) {
                triggers.set(i, stack.getItem());
            }
        }
        // Sub slots
        for (int i = 0; i < 4; i++) {
            ItemStack stack = getSlotStack(SUB_SLOT_START + i);
            if (!stack.isEmpty()) {
                triggers.set(i + 4, stack.getItem());
            }
        }

        triggerStack.set(ModDataComponents.TRIGGER_CONFIGURE, new TriggerConfigureData(triggers));
        setChanged();
    }

    public void clearConfigSlots() {
        for (int i = MAIN_SLOT_START; i <= SUB_SLOT_END; i++) {
            setSlotStack(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    @NonNull
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.wtmod.assembly_bench");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AssemblyBenchMenu(containerId, playerInventory, this);
    }
}
