package com.JSUSHDX.WorldTriggerMod.blocks.entity;

import com.JSUSHDX.WorldTriggerMod.blocks.menu.AssemblyBenchMenu;
import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.data.records.TriggerConfigureData;
import com.JSUSHDX.WorldTriggerMod.item.custom.AsteroidTriggerItem;
import com.JSUSHDX.WorldTriggerMod.item.custom.KogetsuTriggerItem;
import com.JSUSHDX.WorldTriggerMod.item.custom.ShieldTriggerItem;
import com.JSUSHDX.WorldTriggerMod.item.custom.TriggerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("removal")
public class AssemblyBenchBlockEntity extends BlockEntity implements MenuProvider {
    // Slot layout constants
    public static final int TRIGGER_SLOT = 0;
    public static final int MAIN_SLOT_START = 1;  // Main slots: 1, 2, 3, 4
    public static final int MAIN_SLOT_END = 4;    // inclusive
    public static final int SUB_SLOT_START = 5;    // Sub slots: 5, 6, 7, 8
    public static final int SUB_SLOT_END = 8;      // inclusive
    public static final int TOTAL_SLOTS = 9;

    private boolean isUpdating = false;

    private final ItemStackHandler inventory = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            AssemblyBenchBlockEntity.this.setChanged();
            
            if (isUpdating) return;
            
            if (slot == TRIGGER_SLOT) {
                ItemStack triggerStack = getStackInSlot(TRIGGER_SLOT);
                if (triggerStack.isEmpty()) {
                    isUpdating = true;
                    AssemblyBenchBlockEntity.this.clearConfigSlots();
                    isUpdating = false;
                } else if (triggerStack.getItem() instanceof TriggerItem) {
                    isUpdating = true;
                    AssemblyBenchBlockEntity.this.loadTriggerConfig(triggerStack);
                    isUpdating = false;
                }
            } else if (slot >= MAIN_SLOT_START && slot <= SUB_SLOT_END) {
                ItemStack triggerStack = getStackInSlot(TRIGGER_SLOT);
                if (!triggerStack.isEmpty() && triggerStack.getItem() instanceof TriggerItem) {
                    isUpdating = true;
                    AssemblyBenchBlockEntity.this.saveTriggerConfig(triggerStack);
                    isUpdating = false;
                }
            }
        }
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == TRIGGER_SLOT) {
                return stack.getItem() instanceof TriggerItem;
            }
            // Main and Sub slots: only accept trigger-type items
            return isTriggerTypeItem(stack);
        }
    };

    public AssemblyBenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASSEMBLY_BENCH_BE.get(), pos, state);
    }

    /**
     * Check if an ItemStack is a trigger-type item (can be placed in Main/Sub slots).
     */
    public static boolean isTriggerTypeItem(ItemStack stack) {
        return stack.getItem() instanceof AsteroidTriggerItem
                || stack.getItem() instanceof KogetsuTriggerItem
                || stack.getItem() instanceof ShieldTriggerItem;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    /**
     * Load TriggerConfigureData from a TRIGGER item into the 8 config slots.
     * Called when a TRIGGER is placed in the center slot.
     */
    public void loadTriggerConfig(ItemStack triggerStack) {
        TriggerConfigureData config = triggerStack.get(ModDataComponents.TRIGGER_CONFIGURE);
        if (config != null && config.triggers() != null) {
            List<Item> triggers = config.triggers();
            // Main slots: config indices 0-3 → inventory slots 1-4
            for (int i = 0; i < 4 && i < triggers.size(); i++) {
                Item item = triggers.get(i);
                inventory.setStackInSlot(MAIN_SLOT_START + i,
                        (item != null && item != Items.AIR) ? new ItemStack(item) : ItemStack.EMPTY);
            }
            // Sub slots: config indices 4-7 → inventory slots 5-8
            for (int i = 4; i < 8 && i < triggers.size(); i++) {
                Item item = triggers.get(i);
                inventory.setStackInSlot(SUB_SLOT_START + (i - 4),
                        (item != null && item != Items.AIR) ? new ItemStack(item) : ItemStack.EMPTY);
            }
        }
        setChanged();
    }

    /**
     * Save the 8 config slots back into a TRIGGER item's TriggerConfigureData.
     * Called when a TRIGGER is removed from the center slot or when the GUI closes.
     */
    public void saveTriggerConfig(ItemStack triggerStack) {
        List<Item> triggers = new ArrayList<>(Collections.nCopies(8, Items.AIR));

        // Main slots: inventory slots 1-4 → config indices 0-3
        for (int i = 0; i < 4; i++) {
            ItemStack stack = inventory.getStackInSlot(MAIN_SLOT_START + i);
            if (!stack.isEmpty()) {
                triggers.set(i, stack.getItem());
            }
        }
        // Sub slots: inventory slots 5-8 → config indices 4-7
        for (int i = 0; i < 4; i++) {
            ItemStack stack = inventory.getStackInSlot(SUB_SLOT_START + i);
            if (!stack.isEmpty()) {
                triggers.set(i + 4, stack.getItem());
            }
        }

        triggerStack.set(ModDataComponents.TRIGGER_CONFIGURE, new TriggerConfigureData(triggers));
    }

    /**
     * Clear the 8 config slots (called after saving config when trigger is removed).
     */
    public void clearConfigSlots() {
        for (int i = MAIN_SLOT_START; i <= SUB_SLOT_END; i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    /**
     * Drop all contents when the block is broken.
     * Saves config to trigger before dropping.
     */
    public void dropContents(Level level, BlockPos pos) {
        // If there's a trigger, save config into it first
        ItemStack triggerStack = inventory.getStackInSlot(TRIGGER_SLOT);
        if (!triggerStack.isEmpty() && triggerStack.getItem() instanceof TriggerItem) {
            saveTriggerConfig(triggerStack);
        }

        // Drop the trigger (with saved config)
        if (!triggerStack.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), triggerStack);
            inventory.setStackInSlot(TRIGGER_SLOT, ItemStack.EMPTY);
        }

        // Clear config slots (items are saved in the trigger's data, not dropped separately)
        clearConfigSlots();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putChild("inventory", inventory);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.readChild("inventory", inventory);
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
