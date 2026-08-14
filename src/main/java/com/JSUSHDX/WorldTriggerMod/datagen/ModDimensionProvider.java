package com.JSUSHDX.WorldTriggerMod.datagen;

import com.JSUSHDX.WorldTriggerMod.worldgen.CombatSimulateDimensions;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class ModDimensionProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.BIOME, ModDimensionProvider::bootstrapBiome)
            .add(Registries.DIMENSION_TYPE, ModDimensionProvider::bootstrapDimensionType)
            .add(Registries.LEVEL_STEM, ModDimensionProvider::bootstrapLevelStem);

    private static void bootstrapBiome(BootstrapContext<Biome> context) {
        CombatSimulateDimensions.registerBiomes(context);
    }

    private static void bootstrapDimensionType(BootstrapContext<DimensionType> context) {
        CombatSimulateDimensions.registerDimensionTypes(context);
    }

    private static void bootstrapLevelStem(BootstrapContext<LevelStem> context) {
        CombatSimulateDimensions.registerLevelStems(context);
    }
}
