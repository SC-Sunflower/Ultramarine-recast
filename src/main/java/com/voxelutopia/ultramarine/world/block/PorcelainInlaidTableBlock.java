package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Porcelain inlaid table block ported from Ultramarine 0.5.1 (Forge) with the chisel
 * table multiblock behaviour: breaking it reverts the chisel table above back into a
 * brush-and-inkstone.
 */
public class PorcelainInlaidTableBlock extends DecorativeBlock {
    public PorcelainInlaidTableBlock(DecorativeBlock.Builder builder) {
        super(builder);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && level.getBlockState(pos.above()).is(BlockRegistry.CHISEL_TABLE)) {
            level.setBlock(
                    pos.above(),
                    BlockRegistry.BRUSH_AND_INKSTONE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, state.getValue(HorizontalDirectionalBlock.FACING)),
                    3
            );
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
