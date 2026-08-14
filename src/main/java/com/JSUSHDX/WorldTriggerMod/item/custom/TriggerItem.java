package com.JSUSHDX.WorldTriggerMod.item.custom;

import com.JSUSHDX.WorldTriggerMod.blocks.custom.OperatorsTerminalBlock;
import com.JSUSHDX.WorldTriggerMod.blocks.custom.RecallBedBlock;
import com.JSUSHDX.WorldTriggerMod.data.ModDataAttachment;
import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.data.custom.TrionData;
import com.JSUSHDX.WorldTriggerMod.data.records.HealthData;
import com.JSUSHDX.WorldTriggerMod.util.TriggerBindingUtils;
import com.JSUSHDX.WorldTriggerMod.util.TriggerModeUtils;
import com.JSUSHDX.WorldTriggerMod.util.TriggerStateUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class TriggerItem extends Item {
    public TriggerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return super.useOn(context);
        }

        ItemStack itemStack = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Block block = level.getBlockState(pos).getBlock();

        if (!itemStack.getOrDefault(ModDataComponents.IS_ON, false) &&
                block instanceof RecallBedBlock) {
            return TriggerBindingUtils.bindOrUnbindRecallBed(level, pos, player, context.getItemInHand());
        }

        if (!itemStack.getOrDefault(ModDataComponents.IS_ON, false) &&
                block instanceof OperatorsTerminalBlock) {
            return TriggerBindingUtils.bindOrUnbindOperatorsTerminal(level, pos, player);
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            boolean isOn = itemStack.getOrDefault(ModDataComponents.IS_ON, false);
            TrionData data = player.getData(ModDataAttachment.TRION_DATA);

            if (!isOn && data.trion() < data.maxTrion() * 0.8) {
                // Not enough Trion to transform
                player.sendSystemMessage(Component.translatable("message.wtmod.trion_not_enough"));
                return InteractionResult.SUCCESS;
            }

            if (!isOn) {
                // Inventory operations
                TriggerModeUtils.savePlayerInventoryToTrigger(player, itemStack);
                TriggerModeUtils.moveTriggerToLastSlot(player, itemStack);
                TriggerModeUtils.changePlayerSelectedSlot(player, 0);

                // Data components operations
                itemStack.set(ModDataComponents.IS_ON, true);
                itemStack.set(ModDataComponents.HEALTH_DATA, new HealthData(player.getHealth(), player.getMaxHealth()));

                TriggerStateUtils.toggleState(player);

                player.setHealth(player.getMaxHealth());

                TriggerModeUtils.provideChosenTriggers(player, itemStack);
            } else {
                if (player instanceof ServerPlayer serverPlayer) {
                    if (player.isShiftKeyDown()) {
                        bailOut(serverPlayer, itemStack);
                    } else {
                        TriggerModeUtils.resetPlayerStatus(serverPlayer, itemStack);
                    }
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Forces this trigger off. Thin wrapper kept here as the public entry point other systems call
     * (e.g. {@code ServerPlayerEvents} on trion depletion) - the actual lifecycle, including combat
     * simulation priority handling, lives in {@link TriggerModeUtils#bailOut}.
     */
    public static void bailOut(ServerPlayer player, ItemStack itemStack) {
        TriggerModeUtils.bailOut(player, itemStack);
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
