package com.JSUSHDX.WorldTriggerMod.datagen;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.item.ModItems;
import com.JSUSHDX.WorldTriggerMod.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, WorldTriggerMod.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(ItemTags.SWORDS).add(ModItems.KOGETSU_TRIGGER.getKey());
        
        tag(ModTags.Items.WEAPON_TRIGGERS)
                .add(ModItems.KOGETSU_TRIGGER.getKey())
                .add(ModItems.ASTEROID_TRIGGER.getKey());
                
        tag(ModTags.Items.DEFENSE_TRIGGERS)
                .add(ModItems.SHIELD_TRIGGER.getKey());
                
        // Optional triggers will be empty for now, or you can add items here in the future
    }
}
