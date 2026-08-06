package com.JSUSHDX.WorldTriggerMod.item.custom;

import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ShieldTriggerItem extends net.minecraft.world.item.ShieldItem {
    public ShieldTriggerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            boolean newState = !stack.get(ModDataComponents.IS_ON);
            stack.set(ModDataComponents.IS_ON, newState);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    // Give enchanted visual effect
    public boolean isFoil(ItemStack itemStack) {
        return itemStack.get(ModDataComponents.IS_ON);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        Boolean isOn = itemStack.get(ModDataComponents.IS_ON);
        Component msg = Component.translatable("tooltip.wtmod.is_on", (isOn ? "On" : "Off")).withColor(TextColor.GREEN);
        builder.accept(msg);
    }
}
