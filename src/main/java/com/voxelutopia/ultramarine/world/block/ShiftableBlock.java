package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.world.block.state.ModBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public abstract class ShiftableBlock extends Block implements SimpleWaterloggedBlock {
   public static final BooleanProperty SHIFTED = ModBlockStateProperties.SHIFTED;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

   public ShiftableBlock(Properties properties) {
      super(properties.noOcclusion());
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockPos blockpos = pContext.getClickedPos();
      FluidState fluidstate = pContext.getLevel().getFluidState(blockpos);
      return (BlockState)this.defaultBlockState().setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      pBuilder.add(new Property[]{SHIFTED, WATERLOGGED});
   }

   public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
      if ((Boolean)state.getValue(WATERLOGGED)) {
         tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      return super.updateShape(state, level, tickAccess, pos, facing, facingPos, facingState, random);
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }
}
