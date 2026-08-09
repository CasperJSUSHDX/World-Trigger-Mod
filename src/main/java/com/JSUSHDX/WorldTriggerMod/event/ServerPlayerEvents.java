package com.JSUSHDX.WorldTriggerMod.event;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.item.custom.TriggerItem;
import com.JSUSHDX.WorldTriggerMod.util.TriggerStateUtils;
import com.JSUSHDX.WorldTriggerMod.util.TrionUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = WorldTriggerMod.MODID)
public class ServerPlayerEvents {

    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Check if player is transformed (Trigger On)
            if (TriggerStateUtils.getState(player)) {
                // Get damage amount before canceling it
                float damageAmount = event.getNewDamage();
                
                // Cancel normal HP damage
                event.setNewDamage(0);

                // Deplete Trion instead
                TrionUtils.addTrion(player, -damageAmount);

                // Check for Bail Out
                if (TrionUtils.getTrion(player) <= 0) {
                    ItemStack activeTrigger = getActiveTrigger(player);
                    if (!activeTrigger.isEmpty()) {
                        TriggerItem.bailOut(player, activeTrigger);
                    } else {
                        // Fallback if we somehow can't find the trigger item
                        TriggerStateUtils.toggleState(player);
                    }
                }
            }
        }
    }

    private static ItemStack getActiveTrigger(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof TriggerItem) {
                Boolean isOn = stack.get(ModDataComponents.IS_ON);
                if (isOn != null && isOn) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
