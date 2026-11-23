package com.voxelutopia.ultramarine.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

public class RoofRidgeConnection extends BaseAxialBlock implements SnowRoofRidge {
   private final DyeColor color;
   private final SnowRoofRidge.RoofRidgeType type;

   public RoofRidgeConnection(DyeColor color, SnowRoofRidge.RoofRidgeType type) {
      super(BaseBlockProperty.STONE);
      this.type = type;
      this.color = color;
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(SNOW_LAYERS, 0));
   }

   @Override
   public ItemInteractionResult useItemOn(ItemStack stack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      return SnowRoofRidge.super.useItemOn(stack, pState, pLevel, pPos, pPlayer, pHand, pHit);
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
      pBuilder.add(new Property[]{SNOW_LAYERS});
   }

   @Override
   public DyeColor getColor() {
      return this.color;
   }

   @Override
   public SnowRoofRidge.RoofRidgeType getType() {
      return this.type;
   }
}
