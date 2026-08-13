package com.JSUSHDX.WorldTriggerMod.datagen;

import com.JSUSHDX.WorldTriggerMod.blocks.ModBlocks;
import com.JSUSHDX.WorldTriggerMod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    public static class Runner extends RecipeProvider.Runner {

        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
            return new ModRecipeProvider(provider, recipeOutput);
        }

        @Override
        public String getName() {
            return "World Trigger Recipes";
        }
    }

    @Override
    protected void buildRecipes() {
        shaped(RecipeCategory.COMBAT, ModItems.TRIGGER.get())
                .pattern("A  ")
                .pattern(" B ")
                .pattern("  A")
                .define('A', Items.DIAMOND)
                .define('B', Items.REDSTONE)
                .unlockedBy(getHasName(ModItems.TRIGGER.get()), has(ModItems.TRIGGER))
                .group("worldtrigger")
                .save(output);

        shaped(RecipeCategory.MISC, ModBlocks.ASSEMBLY_BENCH.get())
                .pattern("III")
                .pattern("ICI")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('C', Items.CRAFTING_TABLE)
                .unlockedBy("has_trigger", has(ModItems.TRIGGER))
                .save(output);

        shaped(RecipeCategory.MISC, ModBlocks.RECALL_BED.get())
                .pattern("IBI")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('B', ItemTags.BEDS)
                .unlockedBy("has_trigger", has(ModItems.TRIGGER))
                .save(output);

        shaped(RecipeCategory.MISC, ModBlocks.OPERATORS_TERMINAL.get())
                .pattern(" I ")
                .pattern("IGI")
                .pattern(" R ")
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GLASS_PANE)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_trigger", has(ModItems.TRIGGER))
                .save(output);
    }
}
