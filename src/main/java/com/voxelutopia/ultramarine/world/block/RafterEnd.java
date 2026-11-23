package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.shape.RawVoxelShape;
import com.voxelutopia.ultramarine.data.shape.ShapeFunction;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RafterEnd extends ShiftableBlock {
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final Function<BlockState, VoxelShape> RAFTER_SHAPE = ShapeFunction.cardinalRotations(new RawVoxelShape(5.0, 10.0, -4.0, 11.0, 16.0, 16.0));
   public static final Function<BlockState, VoxelShape> RAFTER_SHAPE_SHIFTED = ShapeFunction.cardinalRotations(
      new RawVoxelShape(5.0, 18.0, -4.0, 11.0, 24.0, 16.0)
   );

   public RafterEnd(BaseBlockProperty property) {
      super(property.copy().properties.noOcclusion());
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
               .setValue(SHIFTED, Boolean.FALSE))
            .setValue(WATERLOGGED, Boolean.FALSE)
      );
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
      pBuilder.add(new Property[]{FACING});
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return (BlockState)super.getStateForPlacement(pContext).setValue(FACING, pContext.getHorizontalDirection().getOpposite());
   }

   public BlockState rotate(BlockState pState, Rotation pRot) {
      return (BlockState)pState.setValue(FACING, pRot.rotate((Direction)pState.getValue(FACING)));
   }

   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.rotate(pMirror.getRotation((Direction)pState.getValue(FACING)));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return pState.getValue(SHIFTED) ? RAFTER_SHAPE_SHIFTED.apply(pState) : RAFTER_SHAPE.apply(pState);
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.getShape(pState, pLevel, pPos, pContext);
   }

   @Override
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }
}
