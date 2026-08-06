package com.JSUSHDX.WorldTriggerMod.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class ModNetwork {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0");

        // Common Payload
        registrar.playToServer(CommonPayload.ChangeMode.TYPE, CommonPayload.ChangeMode.STREAM_CODEC, CommonPayload.ChangeMode::handler);
        registrar.playToServer(CommonPayload.TriggerPlacedBullet.TYPE, CommonPayload.TriggerPlacedBullet.STREAM_CODEC, CommonPayload.TriggerPlacedBullet::handler);
    }
}
