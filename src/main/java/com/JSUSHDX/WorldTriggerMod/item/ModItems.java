package com.JSUSHDX.WorldTriggerMod.item;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.item.custom.AsteroidTriggerItem;
import com.JSUSHDX.WorldTriggerMod.item.custom.ShieldTriggerItem;
import com.JSUSHDX.WorldTriggerMod.item.custom.TriggerItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WorldTriggerMod.MODID);

    public static final DeferredItem<Item> TRIGGER = ITEMS.registerItem("trigger",
            properties -> new TriggerItem(properties.component(ModDataComponents.IS_ON, false)));

    public static final DeferredItem<Item> SHIELD_TRIGGER = ITEMS.registerItem("shield_trigger",
            properties -> new ShieldTriggerItem(properties.useCooldown(1.0f).component(ModDataComponents.IS_ON, false)));

    public static final DeferredItem<Item> ASTEROID_TRIGGER = ITEMS.registerItem("asteroid_trigger",
            properties -> new AsteroidTriggerItem(properties.useCooldown(1.0f).component(ModDataComponents.MODE, 0)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
