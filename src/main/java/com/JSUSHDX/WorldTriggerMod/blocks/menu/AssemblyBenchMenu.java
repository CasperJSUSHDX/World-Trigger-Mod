package com.JSUSHDX.WorldTriggerMod.blocks.menu;

import com.JSUSHDX.WorldTriggerMod.blocks.ModBlocks;
import com.JSUSHDX.WorldTriggerMod.blocks.entity.AssemblyBenchBlockEntity;
import com.JSUSHDX.WorldTriggerMod.item.custom.TriggerItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import com.JSUSHDX.WorldTriggerMod.tags.ModTags;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.base.BaseMachineMenu;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    
    private static final int PALETTE_START_X = 179;
    private static final int PALETTE_START_Y = 10;
    
    public static class CategoryLayout {
        public final Component name;
        public final int textX;
        public final int textY;
        
        public CategoryLayout(Component name, int textX, int textY) {
            this.name = name;
            this.textX = textX;
            this.textY = textY;
        }
    }

    private final ItemStacksResourceHandler paletteHandler;
    private final List<CategoryLayout> categoryLayouts = new ArrayList<>();

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

        // Initialize Palette
        Map<TagKey<Item>, Component> categories = new LinkedHashMap<>();
        categories.put(ModTags.Items.WEAPON_TRIGGERS, Component.translatable("gui.wtmod.category.weapon"));
        categories.put(ModTags.Items.DEFENSE_TRIGGERS, Component.translatable("gui.wtmod.category.defense"));
        categories.put(ModTags.Items.OPTIONAL_TRIGGERS, Component.translatable("gui.wtmod.category.optional"));

        List<ItemStack> paletteItems = new ArrayList<>();
        List<PaletteSlotInfo> slotInfos = new ArrayList<>();
        
        int currentY = PALETTE_START_Y;
        
        for (Map.Entry<TagKey<Item>, Component> entry : categories.entrySet()) {
            List<Item> itemsInTag = new ArrayList<>();
            for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(entry.getKey())) {
                Item item = holder.value();
                if (AssemblyBenchBlockEntity.isTriggerTypeItem(item)) {
                    itemsInTag.add(item);
                }
            }
            
            if (!itemsInTag.isEmpty()) {
                // Add category layout (Text position)
                categoryLayouts.add(new CategoryLayout(entry.getValue(), PALETTE_START_X, currentY));
                currentY += 12; // Move down for slots
                
                int indexInCat = 0;
                for (Item item : itemsInTag) {
                    paletteItems.add(new ItemStack(item));
                    int slotX = PALETTE_START_X + (indexInCat % 3) * 18;
                    int slotY = currentY + (indexInCat / 3) * 18;
                    slotInfos.add(new PaletteSlotInfo(slotX, slotY));
                    indexInCat++;
                }
                
                // Move down for next category
                int rows = (itemsInTag.size() + 2) / 3;
                currentY += rows * 18 + 8; // padding
            }
        }
        
        paletteHandler = new ItemStacksResourceHandler(paletteItems.size());
        for (int i = 0; i < paletteItems.size(); i++) {
            paletteHandler.set(i, ItemResource.of(paletteItems.get(i)), 1);
            PaletteSlotInfo info = slotInfos.get(i);
            addSlot(new PaletteSlot(paletteHandler, i, info.x, info.y));
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
    
    public List<CategoryLayout> getCategoryLayouts() {
        return categoryLayouts;
    }
    
    public boolean isPaletteSlot(Slot slot) {
        return slot instanceof PaletteSlot;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        ItemStack cursor = this.getCarried();

        // 1. Prevent Triggers from entering player inventory
        if (!cursor.isEmpty() && AssemblyBenchBlockEntity.isTriggerTypeItem(cursor.getItem())) {
            if (slotId >= 0 && slotId < this.slots.size()) {
                Slot slot = this.slots.get(slotId);
                if (!(slot instanceof ConfigSlot) && !(slot instanceof PaletteSlot)) {
                    this.setCarried(ItemStack.EMPTY); // Destroy
                    return;
                }
            } else if (slotId == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE) {
                this.setCarried(ItemStack.EMPTY); // Destroy
                return;
            }
        }

        // 2. Prevent Hotbar swapping for Config and Palette slots
        if (clickType == ContainerInput.SWAP) {
            if (slotId >= 0 && slotId < this.slots.size()) {
                Slot slot = this.slots.get(slotId);
                if (slot instanceof PaletteSlot || slot instanceof ConfigSlot) {
                    return; // Cancel swap
                }
            }
        }

        // 3. Palette Slot logic
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot instanceof PaletteSlot) {
                if (clickType == ContainerInput.PICKUP) {
                    ItemStack stackInSlot = slot.getItem();
                    if (button == 0) {
                        this.setCarried(stackInSlot.copy());
                    } else if (button == 1) {
                        this.setCarried(ItemStack.EMPTY);
                    }
                }
                return; // Cancel default behavior
            }
        }

        // 4. Config Slot middle click (clone)
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot instanceof ConfigSlot && clickType == ContainerInput.CLONE) {
                this.setCarried(slot.getItem().copy());
                return;
            }
        }

        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        Slot slot = this.slots.get(quickMovedSlotIndex);
        if (slot != null && slot.hasItem()) {
            if (slot instanceof PaletteSlot) {
                return ItemStack.EMPTY; // Do nothing
            }
            if (slot instanceof ConfigSlot) {
                slot.set(ItemStack.EMPTY); // Delete the item
                return ItemStack.EMPTY;
            }
        }
        return super.quickMoveStack(player, quickMovedSlotIndex);
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

    private class PaletteSlot extends ResourceHandlerSlot {
        public PaletteSlot(ItemStacksResourceHandler handler, int index, int x, int y) {
            super(handler, handler::set, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
    
    private static class PaletteSlotInfo {
        public final int x, y;
        public PaletteSlotInfo(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
