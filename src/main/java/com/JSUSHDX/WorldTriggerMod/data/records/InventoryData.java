package com.JSUSHDX.WorldTriggerMod.data.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.component.ItemContainerContents;

public record InventoryData(ItemContainerContents contents) {
    // Original Codec & Stream codec
    public static final Codec<InventoryData> CODEC =
            ItemContainerContents.CODEC.xmap(InventoryData::new, InventoryData::contents);

    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryData> STREAM_CODEC =
            ItemContainerContents.STREAM_CODEC.map(InventoryData::new, InventoryData::contents);
}
