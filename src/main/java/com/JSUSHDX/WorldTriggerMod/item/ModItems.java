package com.JSUSHDX.WorldTriggerMod.item;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WorldTriggerMod.MODID);

    public static final DeferredItem<Item> TRIGGER = ITEMS.registerSimpleItem("trigger");

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
