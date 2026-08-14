package com.JSUSHDX.WorldTriggerMod.blocks.custom;

import com.JSUSHDX.WorldTriggerMod.blocks.entity.CombatSimulateConsoleEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class CombatSimulateConsoleBlock extends BaseEntityBlock {
    public static final MapCodec<CombatSimulateConsoleBlock> CODEC = simpleCodec(CombatSimulateConsoleBlock::new);

    public CombatSimulateConsoleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CombatSimulateConsoleEntity(blockPos, blockState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof CombatSimulateConsoleEntity blockEntity) {
                player.openMenu(blockEntity, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
