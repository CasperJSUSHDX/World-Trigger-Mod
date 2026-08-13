package com.JSUSHDX.WorldTriggerMod.data.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record TerminalEntry(UUID ownerId, String ownerName, float trion, float maxTrion) {
    public static final Codec<TerminalEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("owner_id").forGetter(TerminalEntry::ownerId),
            Codec.STRING.fieldOf("owner_name").forGetter(TerminalEntry::ownerName),
            Codec.FLOAT.fieldOf("trion").forGetter(TerminalEntry::trion),
            Codec.FLOAT.fieldOf("max_trion").forGetter(TerminalEntry::maxTrion)
    ).apply(instance, TerminalEntry::new));
}
