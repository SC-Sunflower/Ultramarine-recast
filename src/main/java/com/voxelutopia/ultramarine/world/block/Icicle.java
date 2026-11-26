package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.shape.RawVoxelShape;
import com.voxelutopia.ultramarine.data.shape.ShapeFunction;
import com.voxelutopia.ultramarine.world.block.state.ModBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class Icicle extends WallSideBlock {
   public static final IntegerProperty VARIANT = ModBlockStateProperties.VARIANT;
   private final int numVariants;
   private final ShapeFunction shapeFunction;

   public Icicle(int numVariants, ShapeFunction shapeFunction) {
      super(BaseBlockProperty.ICE);
      this.registerDefaultState((BlockState)((BlockState)this.getStateDefinition().any()).setValue(VARIANT, 0));
      this.numVariants = numVariants;
      this.shapeFunction = shapeFunction;
   }

   public Icicle(int numVariants) {
      this(numVariants, ShapeFunction.cardinalRotations(new RawVoxelShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0)));
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      int variant = pLevel.getRandom().nextInt(3);
      BlockState newState = (BlockState)pState.setValue(VARIANT, variant);
      pLevel.setBlockAndUpdate(pPos, newState);
   }

   @Override
   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.shapeFunction.apply(pState);
   }

   public int getNumVariants() {
      return this.numVariants;
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockState state = super.getStateForPlacement(pContext);
      return state == null ? null : (BlockState)state.setValue(VARIANT, 0);
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
      pBuilder.add(new Property[]{VARIANT});
   }
}
