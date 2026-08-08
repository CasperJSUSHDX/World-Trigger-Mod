package com.JSUSHDX.WorldTriggerMod.event;

import com.JSUSHDX.WorldTriggerMod.blocks.menu.ModMenuTypes;
import com.JSUSHDX.WorldTriggerMod.client.renderer.entity.ShieldEntityRenderer;
import com.JSUSHDX.WorldTriggerMod.client.renderer.entity.TrionBulletRenderer;
import com.JSUSHDX.WorldTriggerMod.client.screen.custom.AssemblyBenchScreen;
import com.JSUSHDX.WorldTriggerMod.entity.ModEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientModEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TRION_BULLET.get(), TrionBulletRenderer::new);
        event.registerEntityRenderer(ModEntities.SHIELD_ENTITY.get(), ShieldEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ASSEMBLY_BENCH_MENU.get(), AssemblyBenchScreen::new);
    }
}
