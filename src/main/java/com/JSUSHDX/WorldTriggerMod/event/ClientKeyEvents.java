package com.JSUSHDX.WorldTriggerMod.event;


import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.client.screen.BaseTriggerMenuScreen;
import com.JSUSHDX.WorldTriggerMod.client.screen.custom.AsteroidMenuScreen;
import com.JSUSHDX.WorldTriggerMod.item.ModItems;
import com.JSUSHDX.WorldTriggerMod.network.CommonPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientKeyEvents {
    public static final KeyMapping.Category WORLD_TRIGGER_CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "wtmod_keymap"));

    public static final Lazy<KeyMapping> TRIGGER_ITEM = Lazy.of(()-> new KeyMapping(
            "key.wtmod.trigger_item",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            WORLD_TRIGGER_CATEGORY
    ));

    public static final Lazy<KeyMapping> OPEN_MENU = Lazy.of(()-> new KeyMapping(
            "key.wtmod.open_trigger_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            WORLD_TRIGGER_CATEGORY
    ));

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TRIGGER_ITEM.get());
        event.register(OPEN_MENU.get());
    }

    private static Map<Item, java.util.function.Supplier<BaseTriggerMenuScreen>> menuMap = null;

    private static BaseTriggerMenuScreen getMenuFor(Item item) {
        if (menuMap == null) {
            menuMap = new HashMap<>(Map.of(
                    ModItems.ASTEROID_TRIGGER.get(), AsteroidMenuScreen::new
            ));
        }
        java.util.function.Supplier<BaseTriggerMenuScreen> supplier = menuMap.get(item);
        return supplier != null ? supplier.get() : null;
    }

    @SubscribeEvent
    public static void onClientClick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        while (TRIGGER_ITEM.get().consumeClick()) {
            ClientPacketDistributor.sendToServer(CommonPayload.TriggerPlacedBullet.INSTANCE);
        }

        while (OPEN_MENU.get().consumeClick()) {
            if (mc.player == null) break;

            Item mainHandItem = mc.player.getMainHandItem().getItem();
            Item offHandItem = mc.player.getOffhandItem().getItem();

            // Lazy load
            BaseTriggerMenuScreen menu = getMenuFor(mainHandItem);
            if (menu == null) {
                menu = getMenuFor(offHandItem);
            }

            if (menu != null) {
                mc.setScreenAndShow(menu);
            }
        }
    }
}
