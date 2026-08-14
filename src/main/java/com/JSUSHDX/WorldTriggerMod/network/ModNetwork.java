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
        registrar.playToClient(CommonPayload.SetPlayerSlot.TYPE, CommonPayload.SetPlayerSlot.STREAM_CODEC, CommonPayload.SetPlayerSlot::handler);
        registrar.playToServer(CommonPayload.RemoveTerminalEntry.TYPE, CommonPayload.RemoveTerminalEntry.STREAM_CODEC, CommonPayload.RemoveTerminalEntry::handler);
        registrar.playToServer(CommonPayload.AddCombatPoolEntry.TYPE, CommonPayload.AddCombatPoolEntry.STREAM_CODEC, CommonPayload.AddCombatPoolEntry::handler);
        registrar.playToServer(CommonPayload.RemoveCombatPoolEntry.TYPE, CommonPayload.RemoveCombatPoolEntry.STREAM_CODEC, CommonPayload.RemoveCombatPoolEntry::handler);
        registrar.playToServer(CommonPayload.UpdateCombatPoolEntry.TYPE, CommonPayload.UpdateCombatPoolEntry.STREAM_CODEC, CommonPayload.UpdateCombatPoolEntry::handler);
        registrar.playToServer(CommonPayload.StartCombatSimulation.TYPE, CommonPayload.StartCombatSimulation.STREAM_CODEC, CommonPayload.StartCombatSimulation::handler);
        registrar.playToServer(CommonPayload.RecallCombatSimulation.TYPE, CommonPayload.RecallCombatSimulation.STREAM_CODEC, CommonPayload.RecallCombatSimulation::handler);
    }
}
