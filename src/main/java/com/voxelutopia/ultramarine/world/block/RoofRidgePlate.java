package com.voxelutopia.ultramarine.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RoofRidgePlate extends BaseHorizontalDirectionalBlock {
   public RoofRidgePlate(BaseBlockProperty property) {
      super(property);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
   }
}
