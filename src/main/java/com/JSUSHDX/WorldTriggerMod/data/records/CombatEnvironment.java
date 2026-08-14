package com.JSUSHDX.WorldTriggerMod.data.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

/**
 * A single combat-simulation scenario stored in the combat pool matrix: a snapshot of
 * time of day and weather that the Combat Simulate Console can generate.
 */
public record CombatEnvironment(TimeOfDay time, Weather weather) {
    public static final Codec<CombatEnvironment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TimeOfDay.CODEC.fieldOf("time").forGetter(CombatEnvironment::time),
            Weather.CODEC.fieldOf("weather").forGetter(CombatEnvironment::weather)
    ).apply(instance, CombatEnvironment::new));

    public Component describe() {
        return Component.translatable("combat_environment.wtmod.description", time.displayName(), weather.displayName());
    }

    public enum TimeOfDay implements StringRepresentable {
        MORNING("morning"),
        EVENING("evening");

        public static final Codec<TimeOfDay> CODEC = Codec.STRING.xmap(TimeOfDay::byName, TimeOfDay::getSerializedName);

        private final String serializedName;

        TimeOfDay(String serializedName) {
            this.serializedName = serializedName;
        }

        private static TimeOfDay byName(String serializedName) {
            for (TimeOfDay value : values()) {
                if (value.serializedName.equals(serializedName)) {
                    return value;
                }
            }
            return MORNING;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        public Component displayName() {
            return Component.translatable("combat_environment.wtmod.time." + serializedName);
        }
    }

    public enum Weather implements StringRepresentable {
        SUNNY("sunny"),
        RAINY("rainy"),
        SNOWY("snowy");

        public static final Codec<Weather> CODEC = Codec.STRING.xmap(Weather::byName, Weather::getSerializedName);

        private final String serializedName;

        Weather(String serializedName) {
            this.serializedName = serializedName;
        }

        private static Weather byName(String serializedName) {
            for (Weather value : values()) {
                if (value.serializedName.equals(serializedName)) {
                    return value;
                }
            }
            return SUNNY;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        public Component displayName() {
            return Component.translatable("combat_environment.wtmod.weather." + serializedName);
        }
    }
}
