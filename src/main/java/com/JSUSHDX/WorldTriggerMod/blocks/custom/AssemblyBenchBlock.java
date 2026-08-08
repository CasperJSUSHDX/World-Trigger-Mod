package com.JSUSHDX.WorldTriggerMod.blocks.custom;

import com.JSUSHDX.WorldTriggerMod.blocks.entity.AssemblyBenchBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AssemblyBenchBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<AssemblyBenchHalf> HALF = EnumProperty.create("half", AssemblyBenchHalf.class);

    public static final MapCodec<AssemblyBenchBlock> CODEC = simpleCodec(AssemblyBenchBlock::new);

    public AssemblyBenchBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, AssemblyBenchHalf.LEFT));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /**
     * Get the position of the other half of the multiblock.
     * RIGHT is clockwise from LEFT when viewed from the player's facing direction.
     */
    private BlockPos getOtherHalfPos(BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        AssemblyBenchHalf half = state.getValue(HALF);
        Direction rightDir = facing.getClockWise();
        if (half == AssemblyBenchHalf.LEFT) {
            return pos.relative(rightDir);
        } else {
            return pos.relative(rightDir.getOpposite());
        }
    }

    /**
     * Get the master (LEFT) block position.
     */
    private BlockPos getMasterPos(BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == AssemblyBenchHalf.LEFT) {
            return pos;
        }
        return getOtherHalfPos(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos pos = context.getClickedPos();
        Direction rightDir = facing.getClockWise();
        BlockPos rightPos = pos.relative(rightDir);

        Level level = context.getLevel();
        // Check if there's room for the right half
        if (!level.getBlockState(rightPos).canBeReplaced(context)) {
            return null;
        }

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(HALF, AssemblyBenchHalf.LEFT);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);
            Direction rightDir = facing.getClockWise();
            BlockPos rightPos = pos.relative(rightDir);

            // Place the RIGHT half
            level.setBlock(rightPos, state.setValue(HALF, AssemblyBenchHalf.RIGHT), 3);
            level.updateNeighborsAt(rightPos, Blocks.AIR);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            AssemblyBenchHalf half = state.getValue(HALF);
            BlockPos otherPos = getOtherHalfPos(pos, state);
            BlockState otherState = level.getBlockState(otherPos);

            if (otherState.is(this)) {
                // Drop contents from the master (LEFT) half
                BlockPos masterPos = (half == AssemblyBenchHalf.LEFT) ? pos : otherPos;
                BlockEntity be = level.getBlockEntity(masterPos);
                if (be instanceof AssemblyBenchBlockEntity blockEntity) {
                    blockEntity.dropContents(level, masterPos);
                }

                // Remove the other half
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 35);
                level.levelEvent(player, 2001, otherPos, Block.getId(otherState));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockPos masterPos = getMasterPos(pos, state);
            BlockEntity be = level.getBlockEntity(masterPos);
            if (be instanceof AssemblyBenchBlockEntity blockEntity) {
                player.openMenu(blockEntity, masterPos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Only the LEFT (master) half gets a BlockEntity
        if (state.getValue(HALF) == AssemblyBenchHalf.LEFT) {
            return new AssemblyBenchBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
