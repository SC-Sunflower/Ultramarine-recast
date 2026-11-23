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

public class BeamHead extends BaseHorizontalDirectionalBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final VoxelShape SOUTH_SHAPE = Shapes.or(
      Block.box(4.0, 2.0, 0.0, 12.0, 14.0, 8.0), Block.box(4.0, 6.0, 8.0, 12.0, 14.0, 10.0)
   );
   private static final VoxelShape EAST_SHAPE = Shapes.or(
      Block.box(0.0, 2.0, 4.0, 8.0, 14.0, 12.0), Block.box(8.0, 6.0, 4.0, 10.0, 14.0, 12.0)
   );
   private static final VoxelShape NORTH_SHAPE = Shapes.or(
      Block.box(4.0, 2.0, 8.0, 12.0, 14.0, 16.0), Block.box(4.0, 6.0, 6.0, 12.0, 14.0, 8.0)
   );
   private static final VoxelShape WEST_SHAPE = Shapes.or(
      Block.box(8.0, 2.0, 4.0, 16.0, 14.0, 12.0), Block.box(6.0, 6.0, 4.0, 8.0, 14.0, 12.0)
   );

   public BeamHead(BaseBlockProperty property) {
      super(property.copy());
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

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite()))
         .setValue(WATERLOGGED, pContext.getLevel().getFluidState(pContext.getClickedPos()).getType() == Fluids.WATER);
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
      pBuilder.add(new Property[]{WATERLOGGED});
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return Shapes.empty();
   }
}
