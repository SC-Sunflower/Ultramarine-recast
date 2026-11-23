package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.world.block.state.ModBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class RailingSlant extends SideAxialBlock {
   public static final BooleanProperty SHIFTED = ModBlockStateProperties.SHIFTED;

   public RailingSlant(BaseBlockProperty property, int thickness) {
      super(property, thickness, 16, true);
      BlockState state = (BlockState)((BlockState)this.stateDefinition.any()).setValue(SHIFTED, Boolean.FALSE);
      this.registerDefaultState(state);
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
      super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston);
      pState.updateIndirectNeighbourShapes(pLevel, pPos, 3);
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
      pBuilder.add(new Property[]{SHIFTED});
   }
}
