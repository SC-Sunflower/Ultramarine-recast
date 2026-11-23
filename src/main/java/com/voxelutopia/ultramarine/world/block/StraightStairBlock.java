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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StraightStairBlock extends BaseHorizontalDirectionalBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
   private static final VoxelShape BOTTOM_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
   private static final VoxelShape TOP_AABB = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
   private static final VoxelShape EAST_SHAPE_BOTTOM = Shapes.or(BOTTOM_AABB, Block.box(8.0, 8.0, 0.0, 16.0, 16.0, 16.0));
   private static final VoxelShape NORTH_SHAPE_BOTTOM = Shapes.or(BOTTOM_AABB, Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 8.0));
   private static final VoxelShape SOUTH_SHAPE_BOTTOM = Shapes.or(BOTTOM_AABB, Block.box(0.0, 8.0, 8.0, 16.0, 16.0, 16.0));
   private static final VoxelShape WEST_SHAPE_BOTTOM = Shapes.or(BOTTOM_AABB, Block.box(0.0, 8.0, 0.0, 8.0, 16.0, 16.0));
   private static final VoxelShape EAST_SHAPE_TOP = Shapes.or(TOP_AABB, Block.box(8.0, 0.0, 0.0, 16.0, 8.0, 16.0));
   private static final VoxelShape NORTH_SHAPE_TOP = Shapes.or(TOP_AABB, Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 8.0));
   private static final VoxelShape SOUTH_SHAPE_TOP = Shapes.or(TOP_AABB, Block.box(0.0, 0.0, 8.0, 16.0, 8.0, 16.0));
   private static final VoxelShape WEST_SHAPE_TOP = Shapes.or(TOP_AABB, Block.box(0.0, 0.0, 0.0, 8.0, 8.0, 16.0));

   public StraightStairBlock(BaseBlockProperty property) {
      super(property);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
               .setValue(HALF, Half.BOTTOM))
            .setValue(WATERLOGGED, Boolean.FALSE)
      );
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      boolean bottom = pState.getValue(HALF) == Half.BOTTOM;

      return switch ((Direction)pState.getValue(FACING)) {
         case NORTH -> bottom ? NORTH_SHAPE_BOTTOM : NORTH_SHAPE_TOP;
         case SOUTH -> bottom ? SOUTH_SHAPE_BOTTOM : SOUTH_SHAPE_TOP;
         case WEST -> bottom ? WEST_SHAPE_BOTTOM : WEST_SHAPE_TOP;
         case EAST -> bottom ? EAST_SHAPE_BOTTOM : EAST_SHAPE_TOP;
         default -> Shapes.empty();
      };
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      Direction direction = pContext.getClickedFace();
      BlockPos blockpos = pContext.getClickedPos();
      FluidState fluidstate = pContext.getLevel().getFluidState(blockpos);
      return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection()))
            .setValue(
               HALF,
               direction == Direction.DOWN || direction != Direction.UP && pContext.getClickLocation().y - blockpos.getY() > 0.5
                  ? Half.TOP
                  : Half.BOTTOM
            ))
         .setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
      pBuilder.add(new Property[]{WATERLOGGED, HALF});
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return true;
   }
}
