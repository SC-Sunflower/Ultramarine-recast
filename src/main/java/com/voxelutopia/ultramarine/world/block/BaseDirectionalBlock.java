package com.voxelutopia.ultramarine.world.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;

public class BaseDirectionalBlock extends DirectionalBlock implements BaseBlockPropertyHolder {
   @Override
   protected MapCodec<? extends DirectionalBlock> codec() {
      return BlockBehaviour.simpleCodec(props -> this);
   }
   protected final BaseBlockProperty property;

   public BaseDirectionalBlock(BaseBlock block) {
      this(block.getProperty());
   }

   public BaseDirectionalBlock(BaseBlockProperty property) {
      super(property.properties);
      this.property = property;
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.UP));
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return (BlockState)this.defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection().getOpposite());
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      pBuilder.add(new Property[]{FACING});
   }

   public BlockState rotate(BlockState pState, Rotation pRot) {
      BlockState newState = pState;
      Direction facing = (Direction)pState.getValue(FACING);
      if (facing.getAxis().isHorizontal()) {
         newState = (BlockState)newState.setValue(FACING, pRot.rotate(facing));
      }

      return newState;
   }

   public BlockState mirror(BlockState pState, Mirror pMirror) {
      BlockState newState = pState;
      Direction facing = (Direction)pState.getValue(FACING);
      if (facing.getAxis().isHorizontal()) {
         newState = newState.rotate(pMirror.getRotation(facing));
      }

      return newState;
   }

   @Override
   public BaseBlockProperty getProperty() {
      return this.property;
   }
}
