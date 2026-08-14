package com.JSUSHDX.WorldTriggerMod.util;

import com.JSUSHDX.WorldTriggerMod.blocks.entity.OperatorsTerminalBlockEntity;
import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.data.custom.MotherTriggerNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Handles a trigger item being right-clicked ("used on") a bindable block - currently
 * {@code RecallBedBlock} and {@code OperatorsTerminalBlock}. This is a separate concern from
 * entering/leaving trigger mode (see {@link TriggerModeUtils}): it's about associating a player's
 * trigger with a specific block in the world, not about the on/off transformation itself.
 * Extracted from {@code TriggerItem#useOn} so adding a new bindable block means adding a method
 * here plus one dispatch line in {@code TriggerItem}, instead of growing a single method's
 * if/instanceof chain indefinitely.
 */
public class TriggerBindingUtils {
    private TriggerBindingUtils() {
    }

    public static InteractionResult bindOrUnbindRecallBed(Level level, BlockPos pos, Player player, ItemStack itemStack) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!MotherTriggerNetwork.isNearMotherTrigger(level, pos)) {
            player.sendSystemMessage(Component.translatable("message.wtmod.out_of_mother_trigger_range"));
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            itemStack.remove(ModDataComponents.TRIGGER_RECALL_POS);
            player.sendSystemMessage(Component.translatable("message.wtmod.unbind_recall_pos"));
            return InteractionResult.SUCCESS;
        }

        Vec3 position = Vec3.atCenterOf(pos).add(0, 1, 0);
        itemStack.set(ModDataComponents.TRIGGER_RECALL_POS, position);

        Component msg = Component.translatable("message.wtmod.bind_recall_bed", position.x, position.y, position.z);
        player.sendSystemMessage(msg);
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult bindOrUnbindOperatorsTerminal(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!MotherTriggerNetwork.isNearMotherTrigger(level, pos)) {
            player.sendSystemMessage(Component.translatable("message.wtmod.out_of_mother_trigger_range"));
            return InteractionResult.FAIL;
        }

        if (!(level.getBlockEntity(pos) instanceof OperatorsTerminalBlockEntity blockEntity)) {
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            blockEntity.removeEntry(player.getUUID());
            player.sendSystemMessage(Component.translatable("message.wtmod.terminal_unregistered", player.getName()));
            return InteractionResult.SUCCESS;
        }

        blockEntity.registerEntry(player);
        player.sendSystemMessage(Component.translatable("message.wtmod.terminal_registered", player.getName()));
        return InteractionResult.SUCCESS;
    }
}
