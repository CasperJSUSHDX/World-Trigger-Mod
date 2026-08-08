package com.JSUSHDX.WorldTriggerMod.blocks.entity.base;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.Nullable;

public abstract class BaseMachineBlockEntity extends BlockEntity {
    protected final ItemStacksResourceHandler inventory;

    public BaseMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state);
        this.inventory = new ItemStacksResourceHandler(inventorySize) {
            @Override
            protected void onContentsChanged(int slot, ItemStack stack) {
                BaseMachineBlockEntity.this.setChanged();
                BaseMachineBlockEntity.this.onInventoryChanged(slot);
            }

            @Override
            public boolean isValid(int slot, ItemResource resource) {
                return BaseMachineBlockEntity.this.isItemValidForSlot(slot, resource);
            }
        };
    }

    protected void onInventoryChanged(int slot) {
        // To be overridden by subclasses
    }

    protected boolean isItemValidForSlot(int slot, ItemResource resource) {
        return true; // Default behavior
    }

    public ItemStacksResourceHandler getInventory() {
        return this.inventory;
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(inventory.size());
        for (int i = 0; i < inventory.size(); i++) {
            ItemResource res = inventory.getResource(i);
            long amount = inventory.getAmountAsLong(i);
            if (amount > 0) {
                container.setItem(i, res.toStack((int) amount));
            }
        }
        Containers.dropContents(level, pos, container);
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
}
