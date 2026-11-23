package com.voxelutopia.ultramarine.world.block;

import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class BaseAxialBlock extends Block implements AxialBlock, BaseBlockPropertyHolder {
   protected final BaseBlockProperty property;
   public static final EnumProperty<Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

   public BaseAxialBlock(BaseBlock block) {
      this(block.getProperty());
   }

   public BaseAxialBlock(BaseBlockProperty property) {
      super(property.properties);
      this.property = property;
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(AXIS, Axis.X));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      pBuilder.add(new Property[]{AXIS});
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return (BlockState)this.defaultBlockState().setValue(AXIS, pContext.getHorizontalDirection().getAxis());
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

   @Override
   public BaseBlockProperty getProperty() {
      return this.property;
   }
}
