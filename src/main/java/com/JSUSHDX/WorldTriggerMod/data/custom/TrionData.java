package com.JSUSHDX.WorldTriggerMod.data.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TrionData(float trion, float maxTrion) {
    public TrionData() {
        this(100.0f, 100.0f);
    }

    public TrionData updateTrion(float newTrion) {
        float clamped = Math.clamp(newTrion, 0.0f, this.maxTrion);
        return new TrionData(clamped, this.maxTrion);
    }

    public TrionData updateMaxTrion(float newMaxTrion) {
        float newTrion = Math.min(this.trion, newMaxTrion);
        return new TrionData(newTrion, newMaxTrion);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, TrionData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, TrionData::trion,
            ByteBufCodecs.FLOAT, TrionData::maxTrion,
            TrionData::new
    );

    public static final MapCodec<TrionData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("trion").forGetter(TrionData::trion),
            Codec.FLOAT.fieldOf("max_trion").forGetter(TrionData::maxTrion)
    ).apply(instance, TrionData::new));
}
