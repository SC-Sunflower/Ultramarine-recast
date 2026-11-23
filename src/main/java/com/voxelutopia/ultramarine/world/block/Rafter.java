package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.shape.RawVoxelShape;
import com.voxelutopia.ultramarine.data.shape.ShapeFunction;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Rafter extends ShiftableBlock {
   public static final EnumProperty<Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
   public static final Function<BlockState, VoxelShape> RAFTER_SHAPE = ShapeFunction.axialRotations(new RawVoxelShape(0.0, 10.0, 5.0, 16.0, 16.0, 11.0));
   public static final Function<BlockState, VoxelShape> RAFTER_SHAPE_SHIFTED = ShapeFunction.axialRotations(new RawVoxelShape(0.0, 18.0, 5.0, 16.0, 24.0, 11.0));

   public Rafter(BaseBlockProperty property) {
      super(property.copy().properties.noOcclusion());
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(AXIS, Axis.X))
               .setValue(SHIFTED, Boolean.FALSE))
            .setValue(WATERLOGGED, Boolean.FALSE)
      );
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
      pBuilder.add(new Property[]{AXIS});
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return (BlockState)super.getStateForPlacement(pContext).setValue(AXIS, pContext.getHorizontalDirection().getAxis());
   }

   public BlockState rotate(BlockState pState, Rotation pRot) {
      BlockState newState = pState;
      if (pRot == Rotation.CLOCKWISE_90 || pRot == Rotation.COUNTERCLOCKWISE_90) {
         newState = switch ((Axis)pState.getValue(AXIS)) {
            case X -> (BlockState)pState.setValue(AXIS, Axis.Z);
            case Z -> (BlockState)pState.setValue(AXIS, Axis.X);
            default -> newState;
         };
      }

      return newState;
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
