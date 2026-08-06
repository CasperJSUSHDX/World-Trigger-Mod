package com.JSUSHDX.WorldTriggerMod.event;


import com.JSUSHDX.WorldTriggerMod.network.CommonPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientKeyEvents {
    public static final Lazy<KeyMapping> TRIGGER_ITEM = Lazy.of(()-> new KeyMapping(
            "key.wtmod.trigger_item",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KeyMapping.Category.GAMEPLAY
    ));

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TRIGGER_ITEM.get());
    }

    @SubscribeEvent
    public static void onClientClick(ClientTickEvent.Post event) {
        while (TRIGGER_ITEM.get().consumeClick()) {
            ClientPacketDistributor.sendToServer(CommonPayload.TriggerPlacedBullet.INSTANCE);
        }
    }
}
