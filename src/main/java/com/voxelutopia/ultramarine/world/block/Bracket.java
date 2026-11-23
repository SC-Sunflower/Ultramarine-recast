package com.voxelutopia.ultramarine.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Bracket extends BaseHorizontalDirectionalBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final VoxelShape TOP = Block.box(0.0, 10.0, 0.0, 16.0, 16.0, 16.0);
   private static final VoxelShape FUNNEL = Block.box(3.0, 2.0, 3.0, 13.0, 10.0, 13.0);
   private static final VoxelShape BASE = Shapes.or(FUNNEL, TOP);
   private static final VoxelShape EAST_SHAPE = Shapes.or(BASE, Block.box(12.0, 2.0, 3.0, 16.0, 8.0, 13.0));
   private static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, Block.box(3.0, 2.0, 0.0, 13.0, 8.0, 4.0));
   private static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE, Block.box(3.0, 2.0, 12.0, 13.0, 8.0, 16.0));
   private static final VoxelShape WEST_SHAPE = Shapes.or(BASE, Block.box(0.0, 2.0, 3.0, 4.0, 8.0, 13.0));

   public Bracket(BaseBlockProperty property) {
      super(property);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
            .setValue(WATERLOGGED, Boolean.FALSE)
      );
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return switch ((Direction)pState.getValue(FACING)) {
         case NORTH -> NORTH_SHAPE;
         case SOUTH -> SOUTH_SHAPE;
         case WEST -> WEST_SHAPE;
         case EAST -> EAST_SHAPE;
         default -> Shapes.empty();
      };
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection()))
         .setValue(WATERLOGGED, pContext.getLevel().getFluidState(pContext.getClickedPos()).getType() == Fluids.WATER);
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
      pBuilder.add(new Property[]{WATERLOGGED});
   }
}
