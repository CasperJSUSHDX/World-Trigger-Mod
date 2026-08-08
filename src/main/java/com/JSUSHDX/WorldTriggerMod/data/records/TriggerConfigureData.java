package com.JSUSHDX.WorldTriggerMod.data.records;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record TriggerConfigureData(List<Item> triggers) {
    public TriggerConfigureData() {
        this(new ArrayList<>(Collections.nCopies(8, null)));
    }

    // Original Codec & Stream codec
    public static final Codec<TriggerConfigureData> CODEC =
            BuiltInRegistries.ITEM.byNameCodec().listOf(0, 8)
                    .xmap(TriggerConfigureData::new, TriggerConfigureData::triggers);

    public static final StreamCodec<RegistryFriendlyByteBuf, TriggerConfigureData> STREAM_CODEC =
            ByteBufCodecs.registry(Registries.ITEM).apply(ByteBufCodecs.list())
                    .map(TriggerConfigureData::new, TriggerConfigureData::triggers);
}
