package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.shape.RawVoxelShape;
import com.voxelutopia.ultramarine.data.shape.ShapeFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class WindChime extends DecorativeBlock {
   public WindChime() {
      super(
         DecorativeBlock.with(BaseBlockProperty.BRONZE)
            .shaped(ShapeFunction.simpleShape(new RawVoxelShape(5.0, -5.0, 5.0, 11.0, 16.0, 11.0)))
            .noCollision()
            .noOcclusion()
      );
   }

   public boolean isRandomlyTicking(BlockState pState) {
      return true;
   }

   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      float rainLevel = pLevel.getRainLevel(0.1F);
      if (pRandom.nextFloat() * rainLevel > 0.2F) {
         this.playSound(pLevel, pPos, pRandom);
         if (pRandom.nextFloat() > 0.5F) {
            pLevel.scheduleTick(pPos, this, pRandom.nextInt(3));
         }
      }
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      this.playSound(pLevel, pPos, pRandom);
   }

   private void playSound(ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      pLevel.playSound(null, pPos, (SoundEvent)SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1.5F, 2.0F - pRandom.nextFloat() * 0.15F);
   }
}
