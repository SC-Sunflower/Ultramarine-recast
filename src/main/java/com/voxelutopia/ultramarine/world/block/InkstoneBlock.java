package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Brush-and-inkstone block ported from Ultramarine 0.5.1 (Forge) with the chisel table
 * multiblock behaviour: placing it on top of a porcelain inlaid table turns it into a
 * chisel table.
 */
public class InkstoneBlock extends DecorativeBlock {
    public InkstoneBlock(DecorativeBlock.Builder builder) {
        super(builder);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide()) {
            return;
        }
        BlockState blockBelow = level.getBlockState(pos.below());
        if (blockBelow.is(BlockRegistry.PORCELAIN_INLAID_TABLE)) {
            level.setBlock(
                    pos,
                    BlockRegistry.CHISEL_TABLE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, state.getValue(HorizontalDirectionalBlock.FACING)),
                    3
            );
        }
    }
}
