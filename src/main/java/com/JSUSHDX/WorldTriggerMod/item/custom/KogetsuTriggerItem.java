package com.JSUSHDX.WorldTriggerMod.item.custom;

import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.item.ModToolTiers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class KogetsuTriggerItem extends Item {
    public KogetsuTriggerItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack itemStack = player.getItemInHand(hand);
            boolean isOn = itemStack.getOrDefault(ModDataComponents.IS_ON, false);
            if (isOn) {
                // Set damage attribute to 0
                itemStack.set(ModDataComponents.IS_ON, false);
                itemStack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
            } else {
                // Rewind damage attribute
                itemStack.set(ModDataComponents.IS_ON, true);
                ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                builder.add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
                                3.0 + ModToolTiers.TRION.attackDamageBonus(),
                                AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
                builder.add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,
                                -2.4,
                                AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
                itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    // Give enchanted visual effect
    public boolean isFoil(ItemStack itemStack) {
        return itemStack.getOrDefault(ModDataComponents.IS_ON, false);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        Boolean isOn = itemStack.getOrDefault(ModDataComponents.IS_ON, false);
        Component msg = Component.translatable("tooltip.wtmod.is_on", (isOn ? "On" : "Off")).withColor(TextColor.GREEN);
        builder.accept(msg);
    }
}
