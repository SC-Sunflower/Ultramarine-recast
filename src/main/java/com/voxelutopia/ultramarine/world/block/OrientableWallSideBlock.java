package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.shape.RawVoxelShape;
import com.voxelutopia.ultramarine.data.shape.ShapeFunction;
import com.voxelutopia.ultramarine.world.block.state.ModBlockStateProperties;
import com.voxelutopia.ultramarine.world.block.state.OrientableBlockType;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OrientableWallSideBlock extends WallSideBlock implements SideBlock {
   public static final EnumProperty<OrientableBlockType> TYPE = ModBlockStateProperties.ORIENTABLE_BLOCK_TYPE;
   private final ShapeFunction shapeFunction;

   public OrientableWallSideBlock(BaseBlockProperty property) {
      this(property, ShapeFunction.sideOrientedShape(new RawVoxelShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0)));
   }

   public OrientableWallSideBlock(BaseBlockProperty property, ShapeFunction shapeFunction) {
      super(property);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue(TYPE, OrientableBlockType.LEFT))
               .setValue(FACING, Direction.NORTH))
            .setValue(WATERLOGGED, false)
      );
      this.shapeFunction = shapeFunction;
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockState state = this.defaultBlockState();
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      Direction faceDir = pContext.getClickedFace();
      Direction[] lookDirs = pContext.getNearestLookingDirections();
      if (faceDir.getAxis().isHorizontal()) {
         state = (BlockState)((BlockState)state.setValue(FACING, faceDir)).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);

         for (Direction dir : lookDirs) {
            if (dir.getAxis().isHorizontal() && dir != faceDir && dir != faceDir.getOpposite()) {
               if (dir == faceDir.getOpposite().getClockWise()) {
                  state = (BlockState)state.setValue(TYPE, OrientableBlockType.LEFT);
               }

               if (dir == faceDir.getOpposite().getCounterClockWise()) {
                  state = (BlockState)state.setValue(TYPE, OrientableBlockType.RIGHT);
               }
            }
         }

         return state;
      } else {
         return null;
      }
   }

   @Override
   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.shapeFunction.apply(pState);
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
      pBuilder.add(new Property[]{TYPE});
   }

   @Override
   public BlockState rotate(BlockState pState, Rotation pRot) {
      return (BlockState)pState.setValue(FACING, pRot.rotate((Direction)pState.getValue(FACING)));
   }

   @Override
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      BlockState newState = pState;
      if (pMirror != Mirror.NONE) {
         newState = (BlockState)newState.setValue(TYPE, ((OrientableBlockType)pState.getValue(TYPE)).getOpposite());
      }

      return (BlockState)newState.setValue(FACING, pMirror.rotation().rotate((Direction)pState.getValue(FACING)));
   }
}
