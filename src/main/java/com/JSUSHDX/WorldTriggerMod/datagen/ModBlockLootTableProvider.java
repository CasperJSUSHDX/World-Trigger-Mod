package com.JSUSHDX.WorldTriggerMod.datagen;

import com.JSUSHDX.WorldTriggerMod.blocks.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {

    public ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.ASSEMBLY_BENCH.get());
        dropSelf(ModBlocks.RECALL_BED.get());
        dropSelf(ModBlocks.MOTHER_TRIGGER.get());
        dropSelf(ModBlocks.OPERATORS_TERMINAL.get());
        dropSelf(ModBlocks.COMBAT_SIMULATE_CONSOLE.get());
    }

    @Override
    protected @NonNull Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}