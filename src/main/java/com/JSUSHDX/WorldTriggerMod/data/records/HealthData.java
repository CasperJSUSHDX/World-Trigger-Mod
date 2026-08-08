package com.JSUSHDX.WorldTriggerMod.data.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record HealthData(float current, float max) {
    public static final Codec<HealthData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("current").forGetter(HealthData::current),
            Codec.FLOAT.fieldOf("max").forGetter(HealthData::max)
    ).apply(instance, HealthData::new));

    public static final StreamCodec<ByteBuf, HealthData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, HealthData::current,
            ByteBufCodecs.FLOAT, HealthData::max,
            HealthData::new
    );
}
