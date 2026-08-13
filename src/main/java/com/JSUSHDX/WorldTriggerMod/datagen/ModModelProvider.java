package com.JSUSHDX.WorldTriggerMod.datagen;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.blocks.ModBlocks;
import com.JSUSHDX.WorldTriggerMod.item.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;

import static com.JSUSHDX.WorldTriggerMod.datagen.custom.HybridItemModel.generateHybridItem;

public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput output) {
        super(output, WorldTriggerMod.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // Items
        itemModels.generateFlatItem(ModItems.TRIGGER.get(), ModelTemplates.FLAT_ITEM);
        generateHybridItem(itemModels, ModItems.SHIELD_TRIGGER.get());
        generateHybridItem(itemModels, ModItems.ASTEROID_TRIGGER.get());
        generateHybridItem(itemModels, ModItems.KOGETSU_TRIGGER.get());

        // BLOCKS
        // We use createTrivialCube here to satisfy the ModelProvider validation.
        // The actual complex models for assembly_bench and recall_bed are provided manually
        // in src/main/resources, which overrides these generated dummy files.
        blockModels.createTrivialCube(ModBlocks.ASSEMBLY_BENCH.get());
        blockModels.createTrivialCube(ModBlocks.RECALL_BED.get());
        blockModels.createTrivialCube(ModBlocks.MOTHER_TRIGGER.get());
        blockModels.createTrivialCube(ModBlocks.OPERATORS_TERMINAL.get());
    }
}
