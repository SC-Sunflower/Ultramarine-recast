package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.shape.RawVoxelShape;
import com.voxelutopia.ultramarine.data.shape.ShapeFunction;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CentralAxialBlock extends Block implements AxialBlock, SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final EnumProperty<Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
   protected Map<Axis, VoxelShape> shapeByAxis;
   private final boolean hasCollision;
   private final ShapeFunction shapeFunction;

   public CentralAxialBlock(BaseBlockProperty property, ShapeFunction shapeFunction, boolean hasCollision) {
      super(property.properties);
      BlockState state = (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(WATERLOGGED, Boolean.FALSE))
         .setValue(AXIS, Axis.X);
      this.registerDefaultState(state);
      this.shapeFunction = shapeFunction;
      this.hasCollision = hasCollision;
   }

   public CentralAxialBlock(BaseBlockProperty property, int thickness, int height, boolean hasCollision) {
      this(
         property,
         ShapeFunction.axialRotations(new RawVoxelShape(0.0, 0.0, (16 - thickness) / 2.0F, 16.0, height, 16.0F - (16 - thickness) / 2.0F)),
         hasCollision
      );
   }

   public CentralAxialBlock(BaseBlockProperty property, int thickness) {
      this(property, thickness, 16, false);
   }

   public CentralAxialBlock(BaseBlockProperty property, int thickness, int height) {
      this(property, thickness, height, true);
   }

   public CentralAxialBlock(BaseBlockProperty property, ShapeFunction shapeFunction) {
      this(property, shapeFunction, false);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.shapeFunction.apply(pState);
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockState state = (BlockState)this.defaultBlockState()
         .setValue(WATERLOGGED, pContext.getLevel().getFluidState(pContext.getClickedPos()).getType() == Fluids.WATER);
      return (BlockState)state.setValue(AXIS, pContext.getHorizontalDirection().getClockWise().getAxis());
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      pBuilder.add(new Property[]{WATERLOGGED});
      pBuilder.add(new Property[]{AXIS});
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.hasCollision ? this.getShape(pState, pLevel, pPos, pContext) : Shapes.empty();
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
}
