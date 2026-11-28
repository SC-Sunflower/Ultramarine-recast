package com.voxelutopia.ultramarine.world.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.voxelutopia.ultramarine.data.registry.BlockEntityRegistry;
import com.voxelutopia.ultramarine.world.block.DecorativeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class CenserBlockEntity extends BlockEntity {
   public static final Codec<CenserBlockEntity> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(entity -> entity.worldPosition),
            BlockState.CODEC.fieldOf("blockState").forGetter(entity -> entity.getBlockState()),
            Codec.INT.fieldOf("remainingTime").forGetter(entity -> entity.remainingTime),
            Codec.BOOL.fieldOf("lit").forGetter(entity -> entity.lit)
         )
         .apply(instance, (pos, state, remainingTime, lit) -> {
            CenserBlockEntity entity = new CenserBlockEntity(pos, state);
            entity.remainingTime = remainingTime;
            entity.lit = lit;
            return entity;
         })
   );
   private final int BURN_TIME = 1200;
   private int remainingTime = 0;
   private boolean lit = false;

   public CenserBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityRegistry.CENSER, pos, state);
   }

   public static void tick(Level pLevel, BlockPos pPos, BlockState pState, CenserBlockEntity pBlockEntity) {
      // Only burn while lit; an unlit censer must not keep flipping state, calling
      // setChanged() and dirtying chunks every tick.
      if (!pBlockEntity.lit) {
         return;
      }
      if (pBlockEntity.remainingTime <= 0) {
         pBlockEntity.finishIncense(pLevel, pPos, pState);
      } else {
         pBlockEntity.remainingTime--;
         // Persist about once per second instead of every tick: 1200 ticks of
         // burning would otherwise dirty the chunk 1200 times.
         if (pBlockEntity.remainingTime % 20 == 0) {
            pBlockEntity.setChanged();
         }
         if (pLevel.getGameTime() % 80L == 0L) {
            pLevel.getEntitiesOfClass(LivingEntity.class, new AABB(pPos).inflate(10.0))
               .forEach(e -> e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0)));
         }
      }
   }

   public void lightIncense(Level pLevel, BlockPos pPos, BlockState pState) {
      this.lit = true;
      this.remainingTime = 1200;
      pLevel.setBlock(pPos, (BlockState)pState.setValue(DecorativeBlock.LIT, true), 3);
      this.setChanged();
   }

   public void finishIncense(Level pLevel, BlockPos pPos, BlockState pState) {
      this.lit = false;
      this.remainingTime = 0;
      pLevel.setBlock(pPos, (BlockState)pState.setValue(DecorativeBlock.LIT, false), 3);
      this.setChanged();
   }

   public int getRemainingTime() {
      return this.remainingTime;
   }

   protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider provider) {
      super.loadAdditional(pTag, provider);
      this.remainingTime = pTag.getInt("BurnTime");
      // The lit flag mirrors the LIT block state, which is not persisted in NBT;
      // restore it from the block state so a burning censer keeps ticking after reload.
      BlockState state = this.getBlockState();
      this.lit = state.hasProperty(DecorativeBlock.LIT) && state.getValue(DecorativeBlock.LIT);
   }

   protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider provider) {
      super.saveAdditional(pTag, provider);
      pTag.putInt("BurnTime", this.remainingTime);
   }
}
