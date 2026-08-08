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
        // Assembly bench uses custom blockstates/models (facing × half variants)
        // so we skip automatic model generation here.

    }
}
