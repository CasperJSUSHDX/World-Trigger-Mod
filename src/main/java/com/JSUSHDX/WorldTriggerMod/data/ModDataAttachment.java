package com.JSUSHDX.WorldTriggerMod.data;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.data.custom.TrionData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModDataAttachment {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, WorldTriggerMod.MODID);

    public static final Supplier<AttachmentType<TrionData>> TRION_DATA =
            ATTACHMENT_TYPES.register(
                    "trion_data",
                    () -> AttachmentType.builder(TrionData::new)
                            .serialize(TrionData.CODEC)
                            .sync(TrionData.STREAM_CODEC)
                            .copyOnDeath()
                            .build()
            );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
