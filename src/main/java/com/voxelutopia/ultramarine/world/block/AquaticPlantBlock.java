package com.voxelutopia.ultramarine.world.block;

import io.github.fabricators_of_create.porting_lib.common.util.IPlantable;
import io.github.fabricators_of_create.porting_lib.common.util.PlantType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class AquaticPlantBlock extends DecorativeBlock implements IPlantable {
   public AquaticPlantBlock(DecorativeBlock.Builder builder) {
      super(builder);
   }

   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return !pState.canSurvive(pLevel, pCurrentPos)
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   // Note: the original Forge implementation delegates to BlockState#canSustainPlant here, but
   // Porting Lib's port of that hook has no WATER plant-type branch and always returns false,
   // which made aquatic plants impossible to place on Fabric. The water/ice check from
   // mayPlaceOn is the actual placement rule for these plants.
   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      BlockPos blockpos = pPos.below();
      return this.mayPlaceOn(pLevel.getBlockState(blockpos), pLevel, blockpos);
   }

   protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      FluidState fluidstate = pLevel.getFluidState(pPos);
      FluidState fluidstate1 = pLevel.getFluidState(pPos.above());
      return (fluidstate.getType() == Fluids.WATER || pState.is(BlockTags.ICE))
         && fluidstate1.getType() == Fluids.EMPTY;
   }

   public BlockState getPlant(BlockGetter world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      return state.getBlock() != this ? this.defaultBlockState() : state;
   }

   public PlantType getPlantType(BlockGetter level, BlockPos pos) {
      return PlantType.WATER;
   }
}
