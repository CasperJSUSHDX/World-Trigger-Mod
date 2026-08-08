package com.JSUSHDX.WorldTriggerMod.client;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.util.TriggerStateUtils;
import com.JSUSHDX.WorldTriggerMod.util.TrionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = WorldTriggerMod.MODID, value = Dist.CLIENT)
public class ModHUDs {
    /**
     * Intercept player's health HUD drawing event
     * */
    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
            Player player = Minecraft.getInstance().player;

            if (player != null) {
                if (TriggerStateUtils.getState(player)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    /**
     * Write trion amount at the original position of player's health
     * */
    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.PLAYER_HEALTH,
                Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "trion_hud"),
                (guiGraphics, deltaTracker) -> {
                    Minecraft mc = Minecraft.getInstance();
                    Player player = mc.player;

                    if (player == null) return;

                    if (TriggerStateUtils.getState(player)) {
                        float currentTrion = TrionUtils.getTrion(player);
                        float maxTrion = TrionUtils.getMaxTrion(player);

                        int screenWidth = guiGraphics.guiWidth();
                        int screenHeight = guiGraphics.guiHeight();

                        int x = screenWidth / 2 - 91;
                        int y = screenHeight - 39;

                        String text = "Trion: " + currentTrion + " / " + maxTrion;
                        guiGraphics.text(mc.font, text, x, y, 0xFF00FFFF, true);
                    }
        });
    }
}
