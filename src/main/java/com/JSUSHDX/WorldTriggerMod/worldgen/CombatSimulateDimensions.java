package com.JSUSHDX.WorldTriggerMod.worldgen;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

import java.util.List;
import java.util.Optional;

public class CombatSimulateDimensions {
    public record DayClear() {
        public static final ResourceKey<Level> COMBAT_SIMULATE_LEVEL = ResourceKey.create(
                Registries.DIMENSION, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_day_clear"));

        public static final ResourceKey<LevelStem> COMBAT_SIMULATE_STEM = ResourceKey.create(
                Registries.LEVEL_STEM, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_day_clear"));

        public static final ResourceKey<DimensionType> COMBAT_SIMULATE_TYPE = ResourceKey.create(
                Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_day_clear"));

        public static final ResourceKey<Biome> COMBAT_SIMULATE_BIOME = ResourceKey.create(
                Registries.BIOME, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_day_clear"));

        public static void registerBiome(BootstrapContext<Biome> context) {
            context.register(
                    DayClear.COMBAT_SIMULATE_BIOME,
                    new Biome.BiomeBuilder()
                            .hasPrecipitation(false)
                            .temperature(0.5F)
                            .downfall(0.5F)
                            .specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).build())
                            .mobSpawnSettings(MobSpawnSettings.EMPTY)
                            .generationSettings(BiomeGenerationSettings.EMPTY)
                            .setAttribute(EnvironmentAttributes.FOG_COLOR, -4138753)
                            .setAttribute(EnvironmentAttributes.SKY_COLOR, 0xFF87CEFA)
                            .setAttribute(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, -16119286)
                            .build()
            );
        }

        public static void registerDimensionType(BootstrapContext<DimensionType> context) {
            context.register(
                    DayClear.COMBAT_SIMULATE_TYPE,
                    new DimensionType(
                            true,
                            true,
                            false,
                            false,
                            1.0,
                            -64,
                            384,
                            384,
                            HolderSet.empty(),
                            0.0F,
                            new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
                            DimensionType.Skybox.OVERWORLD,
                            CardinalLighting.Type.DEFAULT,
                            EnvironmentAttributeMap.builder()
                                    .set(EnvironmentAttributes.FOG_COLOR, -4138753)
                                    .set(EnvironmentAttributes.SKY_COLOR, 0xFF87CEFA)
                                    .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, -16119286)
                                    .build(),
                            HolderSet.empty(),
                            Optional.empty()
                    )
            );
        }

        public static void registerLevelStem(BootstrapContext<LevelStem> context) {
            HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
            HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

            FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(
                    Optional.empty(),
                    biomes.getOrThrow(DayClear.COMBAT_SIMULATE_BIOME),
                    List.of()
            );
            settings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BARRIER));
            settings.updateLayers();

            context.register(
                    DayClear.COMBAT_SIMULATE_STEM,
                    new LevelStem(dimensionTypes.getOrThrow(DayClear.COMBAT_SIMULATE_TYPE), new FlatLevelSource(settings))
            );
        }
    }

    public record DayPrecip() {
        public static final ResourceKey<Level> COMBAT_SIMULATE_LEVEL = ResourceKey.create(
                Registries.DIMENSION, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_day_precip"));

        public static final ResourceKey<LevelStem> COMBAT_SIMULATE_STEM = ResourceKey.create(
                Registries.LEVEL_STEM, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_day_precip"));

        public static final ResourceKey<DimensionType> COMBAT_SIMULATE_TYPE = ResourceKey.create(
                Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_day_precip"));

        public static final ResourceKey<Biome> COMBAT_SIMULATE_BIOME = ResourceKey.create(
                Registries.BIOME, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_day_precip"));

        public static void registerBiome(BootstrapContext<Biome> context) {
            context.register(
                    DayPrecip.COMBAT_SIMULATE_BIOME,
                    new Biome.BiomeBuilder()
                            .hasPrecipitation(true)
                            .temperature(0.5F)
                            .downfall(0.5F)
                            .specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).build())
                            .mobSpawnSettings(MobSpawnSettings.EMPTY)
                            .generationSettings(BiomeGenerationSettings.EMPTY)
                            .setAttribute(EnvironmentAttributes.FOG_COLOR, 0xFF9AA6B2)
                            .setAttribute(EnvironmentAttributes.SKY_COLOR, 0xFF7C8A99)
                            .setAttribute(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFF5C6570)
                            .build()
            );
        }

        public static void registerDimensionType(BootstrapContext<DimensionType> context) {
            context.register(
                    DayPrecip.COMBAT_SIMULATE_TYPE,
                    new DimensionType(
                            true,
                            true,
                            false,
                            false,
                            1.0,
                            -64,
                            384,
                            384,
                            HolderSet.empty(),
                            0.0F,
                            new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
                            DimensionType.Skybox.OVERWORLD,
                            CardinalLighting.Type.DEFAULT,
                            EnvironmentAttributeMap.builder()
                                    .set(EnvironmentAttributes.FOG_COLOR, 0xFF9AA6B2)
                                    .set(EnvironmentAttributes.SKY_COLOR, 0xFF7C8A99)
                                    .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFF5C6570)
                                    .set(EnvironmentAttributes.AMBIENT_PARTICLES, AmbientParticle.of(ParticleTypes.RAIN, 0.1F))
                                    .build(),
                            HolderSet.empty(),
                            Optional.empty()
                    )
            );
        }

        public static void registerLevelStem(BootstrapContext<LevelStem> context) {
            HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
            HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

            FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(
                    Optional.empty(),
                    biomes.getOrThrow(DayPrecip.COMBAT_SIMULATE_BIOME),
                    List.of()
            );
            settings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BARRIER));
            settings.updateLayers();

            context.register(
                    DayPrecip.COMBAT_SIMULATE_STEM,
                    new LevelStem(dimensionTypes.getOrThrow(DayPrecip.COMBAT_SIMULATE_TYPE), new FlatLevelSource(settings))
            );
        }
    }

    public record NightClear() {
        public static final ResourceKey<Level> COMBAT_SIMULATE_LEVEL = ResourceKey.create(
                Registries.DIMENSION, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_night_clear"));

        public static final ResourceKey<LevelStem> COMBAT_SIMULATE_STEM = ResourceKey.create(
                Registries.LEVEL_STEM, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_night_clear"));

        public static final ResourceKey<DimensionType> COMBAT_SIMULATE_TYPE = ResourceKey.create(
                Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_night_clear"));

        public static final ResourceKey<Biome> COMBAT_SIMULATE_BIOME = ResourceKey.create(
                Registries.BIOME, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_night_clear"));

        public static void registerBiome(BootstrapContext<Biome> context) {
            context.register(
                    NightClear.COMBAT_SIMULATE_BIOME,
                    new Biome.BiomeBuilder()
                            .hasPrecipitation(false)
                            .temperature(0.5F)
                            .downfall(0.5F)
                            .specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).build())
                            .mobSpawnSettings(MobSpawnSettings.EMPTY)
                            .generationSettings(BiomeGenerationSettings.EMPTY)
                            .setAttribute(EnvironmentAttributes.FOG_COLOR, 0xFF0C0E1A)
                            .setAttribute(EnvironmentAttributes.SKY_COLOR, 0xFF0B1030)
                            .setAttribute(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFF14141F)
                            .build()
            );
        }

        public static void registerDimensionType(BootstrapContext<DimensionType> context) {
            context.register(
                    NightClear.COMBAT_SIMULATE_TYPE,
                    new DimensionType(
                            true,
                            true,
                            false,
                            false,
                            1.0,
                            -64,
                            384,
                            384,
                            HolderSet.empty(),
                            0.0F,
                            new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
                            DimensionType.Skybox.OVERWORLD,
                            CardinalLighting.Type.DEFAULT,
                            EnvironmentAttributeMap.builder()
                                    .set(EnvironmentAttributes.FOG_COLOR, 0xFF0C0E1A)
                                    .set(EnvironmentAttributes.SKY_COLOR, 0xFF0B1030)
                                    .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFF14141F)
                                    .set(EnvironmentAttributes.SUN_ANGLE, 180.0F)
                                    .set(EnvironmentAttributes.MOON_ANGLE, 0.0F)
                                    .set(EnvironmentAttributes.STAR_ANGLE, 180.0F)
                                    .set(EnvironmentAttributes.STAR_BRIGHTNESS, 0.5F)
                                    .build(),
                            HolderSet.empty(),
                            Optional.empty()
                    )
            );
        }

        public static void registerLevelStem(BootstrapContext<LevelStem> context) {
            HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
            HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

            FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(
                    Optional.empty(),
                    biomes.getOrThrow(NightClear.COMBAT_SIMULATE_BIOME),
                    List.of()
            );
            settings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BARRIER));
            settings.updateLayers();

            context.register(
                    NightClear.COMBAT_SIMULATE_STEM,
                    new LevelStem(dimensionTypes.getOrThrow(NightClear.COMBAT_SIMULATE_TYPE), new FlatLevelSource(settings))
            );
        }
    }

    public record NightPrecip() {
        public static final ResourceKey<Level> COMBAT_SIMULATE_LEVEL = ResourceKey.create(
                Registries.DIMENSION, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_night_precip"));

        public static final ResourceKey<LevelStem> COMBAT_SIMULATE_STEM = ResourceKey.create(
                Registries.LEVEL_STEM, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_night_precip"));

        public static final ResourceKey<DimensionType> COMBAT_SIMULATE_TYPE = ResourceKey.create(
                Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_night_precip"));

        public static final ResourceKey<Biome> COMBAT_SIMULATE_BIOME = ResourceKey.create(
                Registries.BIOME, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "combat_simulate_night_precip"));

        public static void registerBiome(BootstrapContext<Biome> context) {
            context.register(
                    NightPrecip.COMBAT_SIMULATE_BIOME,
                    new Biome.BiomeBuilder()
                            .hasPrecipitation(true)
                            .temperature(0.5F)
                            .downfall(0.5F)
                            .specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).build())
                            .mobSpawnSettings(MobSpawnSettings.EMPTY)
                            .generationSettings(BiomeGenerationSettings.EMPTY)
                            .setAttribute(EnvironmentAttributes.FOG_COLOR, 0xFF14161C)
                            .setAttribute(EnvironmentAttributes.SKY_COLOR, 0xFF0E1014)
                            .setAttribute(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFF0C0C10)
                            .build()
            );
        }

        public static void registerDimensionType(BootstrapContext<DimensionType> context) {
            context.register(
                    NightPrecip.COMBAT_SIMULATE_TYPE,
                    new DimensionType(
                            true,
                            true,
                            false,
                            false,
                            1.0,
                            -64,
                            384,
                            384,
                            HolderSet.empty(),
                            0.0F,
                            new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
                            DimensionType.Skybox.OVERWORLD,
                            CardinalLighting.Type.DEFAULT,
                            EnvironmentAttributeMap.builder()
                                    .set(EnvironmentAttributes.FOG_COLOR, 0xFF14161C)
                                    .set(EnvironmentAttributes.SKY_COLOR, 0xFF0E1014)
                                    .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFF0C0C10)
                                    .set(EnvironmentAttributes.AMBIENT_PARTICLES, AmbientParticle.of(ParticleTypes.RAIN, 0.1F))
                                    .set(EnvironmentAttributes.SUN_ANGLE, 180.0F)
                                    .set(EnvironmentAttributes.MOON_ANGLE, 0.0F)
                                    .set(EnvironmentAttributes.STAR_ANGLE, 180.0F)
                                    .set(EnvironmentAttributes.STAR_BRIGHTNESS, 0.5F)
                                    .build(),
                            HolderSet.empty(),
                            Optional.empty()
                    )
            );
        }

        public static void registerLevelStem(BootstrapContext<LevelStem> context) {
            HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
            HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

            FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(
                    Optional.empty(),
                    biomes.getOrThrow(NightPrecip.COMBAT_SIMULATE_BIOME),
                    List.of()
            );
            settings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BARRIER));
            settings.updateLayers();

            context.register(
                    NightPrecip.COMBAT_SIMULATE_STEM,
                    new LevelStem(dimensionTypes.getOrThrow(NightPrecip.COMBAT_SIMULATE_TYPE), new FlatLevelSource(settings))
            );
        }
    }
}
