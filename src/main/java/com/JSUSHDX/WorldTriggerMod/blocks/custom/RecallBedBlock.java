package com.JSUSHDX.WorldTriggerMod.blocks.custom;

import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class RecallBedBlock extends Block {
    public RecallBedBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (itemStack.is(ModItems.TRIGGER) && !level.isClientSide()) {
            Vec3 position = Vec3.atCenterOf(pos).add(0, 1, 0);
            itemStack.set(ModDataComponents.TRIGGER_RECALL_POS, position);

            Component msg = Component.translatable("message.wtmod.record_pos");
            player.sendSystemMessage(msg);
        }

        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
    }
}
