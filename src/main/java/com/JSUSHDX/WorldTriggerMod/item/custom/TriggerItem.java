package com.JSUSHDX.WorldTriggerMod.item.custom;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.data.ModDataAttachment;
import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.data.records.HealthData;
import com.JSUSHDX.WorldTriggerMod.data.records.InventoryData;
import com.JSUSHDX.WorldTriggerMod.util.TriggerStateUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class TriggerItem extends Item {
    public TriggerItem(Properties properties) {
        super(properties);
    }

    private static void savePlayerInventoryToTrigger(Player player, ItemStack itemStack) {
        int invSize = player.getInventory().getContainerSize();
        NonNullList<ItemStack> invItems = NonNullList.withSize(invSize, ItemStack.EMPTY);

        for (int i = 0; i < invSize; ++i) {
            ItemStack currentItemStack = player.getInventory().getItem(i);

            if (!currentItemStack.isEmpty() && currentItemStack != itemStack) {
                invItems.set(i, currentItemStack.copy());
            }
        }

        // Package to components
        ItemContainerContents containerContents = ItemContainerContents.fromItems(invItems);

        // Write into trigger
        itemStack.set(ModDataComponents.INVENTORY_DATA, new InventoryData(containerContents));

        // Clean player inventory (Keep trigger)
        for (int i = 0; i < invSize; ++i) {
            if (player.getInventory().getItem(i) != itemStack) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private static void restoreTriggerSavedInventoryToPlayer(Player player, ItemStack itemStack) {
        InventoryData data = itemStack.get(ModDataComponents.INVENTORY_DATA);
        ItemContainerContents containerContents = (data != null) ? data.contents() : null;

        // End if there is no data
        if (containerContents == null) return;

        int invSize = player.getInventory().getContainerSize();
        NonNullList<ItemStack> restoredItems = NonNullList.withSize(invSize, ItemStack.EMPTY);
        containerContents.copyInto(restoredItems);

        // Pub back to player inventory
        for (int i = 0; i < invSize; i++) {
            if (i < restoredItems.size()) {
                if (player.getInventory().getItem(i) != itemStack) {
                    player.getInventory().setItem(i, restoredItems.get(i));
                }
            }
        }

        // Clean the data component on trigger
        itemStack.remove(ModDataComponents.INVENTORY_DATA);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack itemStack = player.getItemInHand(hand);
            Boolean isOn = itemStack.get(ModDataComponents.IS_ON);
            
            // default to false if null
            if (isOn == null) isOn = false;

            if (!isOn) {
                savePlayerInventoryToTrigger(player, itemStack);
                itemStack.set(ModDataComponents.IS_ON, true);
                itemStack.set(ModDataComponents.HEALTH_DATA, new HealthData(player.getHealth(), player.getMaxHealth()));
                player.setHealth(player.getMaxHealth());
                TriggerStateUtils.toggleState(player);
            } else {
                restoreTriggerSavedInventoryToPlayer(player, itemStack);
                itemStack.set(ModDataComponents.IS_ON, false);

                HealthData data = itemStack.getOrDefault(ModDataComponents.HEALTH_DATA, new HealthData(20.0f, 20.0f));
                // Set max health
                AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
                Identifier modifierID = Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "trigger_rewind_max_health");
                if (maxHealthAttr != null) {
                    maxHealthAttr.removeModifier(modifierID);
                    double currentMaxHealth = maxHealthAttr.getValue();
                    double delta = data.max() - currentMaxHealth;
                    if (delta > 0) {
                        AttributeModifier modifier = new AttributeModifier(
                                modifierID,
                                delta,
                                AttributeModifier.Operation.ADD_VALUE
                        );
                        maxHealthAttr.addPermanentModifier(modifier);
                    }
                }
                TriggerStateUtils.toggleState(player);
                // Heal to record health
                player.setHealth(data.current());
                itemStack.remove(ModDataComponents.HEALTH_DATA);
            }

        }

        return InteractionResult.SUCCESS;
    }

    @Override
    // Give enchanted visual effect
    public boolean isFoil(ItemStack itemStack) {
        Boolean isOn = itemStack.getOrDefault(ModDataComponents.IS_ON, false);
        return isOn;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        Boolean isOn = itemStack.getOrDefault(ModDataComponents.IS_ON, false);
        Component msg = Component.translatable("tooltip.wtmod.is_on", (isOn ? "On" : "Off")).withColor(TextColor.GREEN);
        builder.accept(msg);
    }
}
