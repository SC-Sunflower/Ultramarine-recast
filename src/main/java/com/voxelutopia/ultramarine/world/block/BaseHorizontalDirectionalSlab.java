package com.voxelutopia.ultramarine.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class BaseHorizontalDirectionalSlab extends BaseSlab implements SimpleWaterloggedBlock {
   protected final BaseBlockProperty property;
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final EnumProperty<SlabType> TYPE = BlockStateProperties.SLAB_TYPE;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

   public BaseHorizontalDirectionalSlab(BaseBlock block) {
      this(block.getProperty());
   }

   public BaseHorizontalDirectionalSlab(BaseBlockProperty property) {
      super(property);
      this.property = property;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
               .setValue(TYPE, SlabType.BOTTOM))
            .setValue(WATERLOGGED, false)
      );
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = pContext.getLevel().getBlockState(blockpos);
      if (blockstate.is(this)) {
         return (BlockState)((BlockState)blockstate.setValue(TYPE, SlabType.DOUBLE)).setValue(WATERLOGGED, Boolean.FALSE);
      }

      FluidState fluidstate = pContext.getLevel().getFluidState(blockpos);
      BlockState blockstate1 = (BlockState)((BlockState)this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM))
         .setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
      Direction direction = pContext.getHorizontalDirection();
      blockstate1 = (BlockState)blockstate1.setValue(FACING, direction);
      return !(pContext.getClickLocation().y - blockpos.getY() > 0.5)
         ? blockstate1
         : (BlockState)blockstate1.setValue(TYPE, SlabType.TOP);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      pBuilder.add(new Property[]{FACING, TYPE, WATERLOGGED});
   }

   @Override
   public BaseBlockProperty getProperty() {
      return this.property;
   }
}
