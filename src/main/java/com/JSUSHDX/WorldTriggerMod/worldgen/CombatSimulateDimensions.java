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

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Registers the four combat_simulate dimensions (day/night x clear/precip).
 * <p>
 * Each variant is described by an {@link EnvironmentVariant} entry in {@link #VARIANTS} - adding
 * a new time/weather combination only means adding a new entry there. The
 * registerBiome/registerDimensionType/registerLevelStem logic below is shared and data-driven,
 * not duplicated per variant (previously each of the four variants had its own copy-pasted
 * registration methods).
 */
public class CombatSimulateDimensions {

    /**
     * All the per-variant data that used to be hand-duplicated across DayClear/DayPrecip/
     * NightClear/NightPrecip. The four angle/particle fields are nullable because "day" variants
     * don't override sun/moon/star attributes and "clear" variants have no ambient particles.
     */
    public record EnvironmentVariant(
            String id,
            ResourceKey<Level> level,
            ResourceKey<LevelStem> stem,
            ResourceKey<DimensionType> type,
            ResourceKey<Biome> biome,
            boolean hasPrecipitation,
            int fogColor,
            int skyColor,
            int ambientLightColor,
            @Nullable List<AmbientParticle> ambientParticles,
            @Nullable Float sunAngle,
            @Nullable Float moonAngle,
            @Nullable Float starAngle,
            @Nullable Float starBrightness
    ) {
    }

    private static EnvironmentVariant variant(
            String id, boolean hasPrecipitation, int fogColor, int skyColor, int ambientLightColor,
            @Nullable List<AmbientParticle> ambientParticles,
            @Nullable Float sunAngle, @Nullable Float moonAngle, @Nullable Float starAngle, @Nullable Float starBrightness
    ) {
        return new EnvironmentVariant(
                id,
                ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, id)),
                ResourceKey.create(Registries.LEVEL_STEM, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, id)),
                ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, id)),
                ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, id)),
                hasPrecipitation, fogColor, skyColor, ambientLightColor, ambientParticles,
                sunAngle, moonAngle, starAngle, starBrightness
        );
    }

    public static final EnvironmentVariant DAY_CLEAR = variant(
            "combat_simulate_day_clear", false,
            -4138753, 0xFF87CEFA, -16119286,
            null,
            null, null, null, null
    );

    public static final EnvironmentVariant DAY_PRECIP = variant(
            "combat_simulate_day_precip", true,
            0xFF9AA6B2, 0xFF7C8A99, 0xFF5C6570,
            AmbientParticle.of(ParticleTypes.RAIN, 0.1F),
            null, null, null, null
    );

    public static final EnvironmentVariant NIGHT_CLEAR = variant(
            "combat_simulate_night_clear", false,
            0xFF0C0E1A, 0xFF0B1030, 0xFF14141F,
            null,
            180.0F, 0.0F, 180.0F, 0.5F
    );

    public static final EnvironmentVariant NIGHT_PRECIP = variant(
            "combat_simulate_night_precip", true,
            0xFF14161C, 0xFF0E1014, 0xFF0C0C10,
            AmbientParticle.of(ParticleTypes.RAIN, 0.1F),
            180.0F, 0.0F, 180.0F, 0.5F
    );

    /** All registered combat_simulate environment variants, in registration order. */
    public static final List<EnvironmentVariant> VARIANTS = List.of(DAY_CLEAR, DAY_PRECIP, NIGHT_CLEAR, NIGHT_PRECIP);

    public static void registerBiomes(BootstrapContext<Biome> context) {
        for (EnvironmentVariant v : VARIANTS) registerBiome(context, v);
    }

    public static void registerDimensionTypes(BootstrapContext<DimensionType> context) {
        for (EnvironmentVariant v : VARIANTS) registerDimensionType(context, v);
    }

    public static void registerLevelStems(BootstrapContext<LevelStem> context) {
        for (EnvironmentVariant v : VARIANTS) registerLevelStem(context, v);
    }

    private static void registerBiome(BootstrapContext<Biome> context, EnvironmentVariant v) {
        context.register(
                v.biome(),
                new Biome.BiomeBuilder()
                        .hasPrecipitation(v.hasPrecipitation())
                        .temperature(0.5F)
                        .downfall(0.5F)
                        .specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).build())
                        .mobSpawnSettings(MobSpawnSettings.EMPTY)
                        .generationSettings(BiomeGenerationSettings.EMPTY)
                        .setAttribute(EnvironmentAttributes.FOG_COLOR, v.fogColor())
                        .setAttribute(EnvironmentAttributes.SKY_COLOR, v.skyColor())
                        .setAttribute(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, v.ambientLightColor())
                        .build()
        );
    }

    private static void registerDimensionType(BootstrapContext<DimensionType> context, EnvironmentVariant v) {
        var attributes = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.FOG_COLOR, v.fogColor())
                .set(EnvironmentAttributes.SKY_COLOR, v.skyColor())
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, v.ambientLightColor());

        if (v.ambientParticles() != null) attributes.set(EnvironmentAttributes.AMBIENT_PARTICLES, v.ambientParticles());
        if (v.sunAngle() != null) attributes.set(EnvironmentAttributes.SUN_ANGLE, v.sunAngle());
        if (v.moonAngle() != null) attributes.set(EnvironmentAttributes.MOON_ANGLE, v.moonAngle());
        if (v.starAngle() != null) attributes.set(EnvironmentAttributes.STAR_ANGLE, v.starAngle());
        if (v.starBrightness() != null) attributes.set(EnvironmentAttributes.STAR_BRIGHTNESS, v.starBrightness());

        context.register(
                v.type(),
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
                        attributes.build(),
                        HolderSet.empty(),
                        Optional.empty()
                )
        );
    }

    private static void registerLevelStem(BootstrapContext<LevelStem> context, EnvironmentVariant v) {
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(
                Optional.empty(),
                biomes.getOrThrow(v.biome()),
                List.of()
        );
        settings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BARRIER));
        settings.updateLayers();

        context.register(
                v.stem(),
                new LevelStem(dimensionTypes.getOrThrow(v.type()), new FlatLevelSource(settings))
        );
    }
}
