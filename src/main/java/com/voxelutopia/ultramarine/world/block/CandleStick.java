package com.voxelutopia.ultramarine.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class CandleStick extends DecorativeBlock {
   private final Vec3 flameOffset;

   public CandleStick(DecorativeBlock.Builder builder, Vec3 flameOffset) {
      super(builder);
      this.flameOffset = flameOffset;
   }

   @NotNull
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockState state = super.getStateForPlacement(pContext);
      return this.isLuminous() ? (BlockState)state.setValue(LIT, false) : state;
   }

   public InteractionResult useItemOn(ItemStack stack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      ItemStack item = pPlayer.getItemInHand(pHand);
      if (item.is(Items.FLINT_AND_STEEL) && pState.hasProperty(LIT) && !(Boolean)pState.getValue(LIT)) {
         item.hurtAndBreak(1, pPlayer, EquipmentSlot.MAINHAND);
         pLevel.setBlock(pPos, (BlockState)pState.setValue(LIT, true), 3);
         pLevel.updateNeighborsAt(pPos, this);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      super.animateTick(pState, pLevel, pPos, pRandom);
      if (pState.hasProperty(LIT) && (Boolean)pState.getValue(LIT)) {
         if (pLevel.getGameTime() % (2 - pRandom.nextInt(1)) == 0L) {
            pLevel.addParticle(
               ParticleTypes.FLAME,
               pPos.getX() + this.flameOffset.x,
               pPos.getY() + this.flameOffset.y,
               pPos.getZ() + this.flameOffset.z,
               0.0,
               pRandom.nextDouble() * 0.01,
               0.0
            );
         }
      }
   }
}
