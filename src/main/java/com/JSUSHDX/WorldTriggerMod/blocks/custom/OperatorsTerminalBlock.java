package com.JSUSHDX.WorldTriggerMod.blocks.custom;

import com.JSUSHDX.WorldTriggerMod.blocks.entity.OperatorsTerminalBlockEntity;
import com.JSUSHDX.WorldTriggerMod.data.custom.MotherTriggerNetwork;
import com.JSUSHDX.WorldTriggerMod.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class OperatorsTerminalBlock extends BaseEntityBlock {
    public static final MapCodec<OperatorsTerminalBlock> CODEC = simpleCodec(OperatorsTerminalBlock::new);

    public OperatorsTerminalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OperatorsTerminalBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (itemStack.is(ModItems.TRIGGER) && !level.isClientSide()) {
            if (!MotherTriggerNetwork.isNearMotherTrigger(level, pos)) {
                player.sendSystemMessage(Component.translatable("message.wtmod.out_of_mother_trigger_range"));
                return InteractionResult.FAIL;
            }

            if (level.getBlockEntity(pos) instanceof OperatorsTerminalBlockEntity blockEntity) {
                blockEntity.registerEntry(player);
                player.sendSystemMessage(Component.translatable("message.wtmod.terminal_registered", player.getName()));
            }

            return InteractionResult.SUCCESS;
        }

        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (!MotherTriggerNetwork.isNearMotherTrigger(level, pos)) {
                player.sendSystemMessage(Component.translatable("message.wtmod.out_of_mother_trigger_range"));
                return InteractionResult.FAIL;
            }

            if (level.getBlockEntity(pos) instanceof OperatorsTerminalBlockEntity blockEntity) {
                blockEntity.refreshOnlineEntries();
                player.openMenu(blockEntity, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
