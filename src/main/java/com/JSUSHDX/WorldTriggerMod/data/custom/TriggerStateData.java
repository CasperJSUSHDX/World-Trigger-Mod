package com.JSUSHDX.WorldTriggerMod.data.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TriggerStateData(boolean isMorph) {
    public TriggerStateData() { this(false); }

    public TriggerStateData setMorph(boolean bool) {
        return new TriggerStateData(bool);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, TriggerStateData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, TriggerStateData::isMorph,
    TriggerStateData::new
    );

    public static final MapCodec<TriggerStateData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("is_morph").forGetter(TriggerStateData::isMorph)
    ).apply(instance, TriggerStateData::new));
}
