package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.registry.BlockEntityRegistry;
import com.voxelutopia.ultramarine.world.block.entity.BottleGourdBlockEntity;
import com.voxelutopia.ultramarine.data.storage.ItemHandlerHelper;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BottleGourd extends DecorativeBlock implements EntityBlock {
   public static final VoxelShape GOURD = Block.box(5.0, 0.0, 5.0, 11.0, 14.0, 11.0);

   public BottleGourd(DecorativeBlock.Builder builder) {
      super(builder);
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      ItemStack item = pPlayer.getItemInHand(pHand);
      Optional<BottleGourdBlockEntity> optionalBlockEntity = pLevel.getBlockEntity(pPos, BlockEntityRegistry.BOTTLE_GOURD);
      if (!optionalBlockEntity.isPresent()) {
         return InteractionResult.PASS;
      }

      BottleGourdBlockEntity blockEntity = optionalBlockEntity.get();
      if (item.is(Items.POTION)) {
         Potion potion = PotionUtils.getPotion(item);
         if (blockEntity.addPotionCharge(potion)) {
            if (!pLevel.isClientSide() && !pPlayer.getAbilities().instabuild) {
               item.shrink(1);
               ItemHandlerHelper.giveItemToPlayer(pPlayer, new ItemStack(Items.GLASS_BOTTLE));
            }

            pLevel.playSound(null, pPos, SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
         }
      } else if (blockEntity.hasCharges()) {
         Optional<Potion> potion1 = blockEntity.takePotionCharge();
         if (!potion1.isPresent()) {
            return InteractionResult.PASS;
         }

         if (!pLevel.isClientSide()) {
            for (MobEffectInstance effectInstance : potion1.get().getEffects()) {
               if (effectInstance.getEffect().isInstantenous()) {
                  effectInstance.getEffect().applyInstantenousEffect(pPlayer, pPlayer, pPlayer, effectInstance.getAmplifier(), 1.0);
               } else {
                  pPlayer.addEffect(new MobEffectInstance(effectInstance));
               }
            }
         }

         pLevel.playSound(null, pPlayer, SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      }

      return InteractionResult.PASS;
   }

   public BlockState playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
      super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
      pLevel.getBlockEntity(pPos, BlockEntityRegistry.BOTTLE_GOURD).ifPresent(entity -> {
         if (entity.hasCharges() && !pLevel.isClientSide()) {
            int charges = entity.getCharges();
            List<MobEffectInstance> effects = entity.getPotion().getEffects();
            if (!effects.isEmpty()) {
               AreaEffectCloud areaeffectcloud = new AreaEffectCloud(pLevel, pPos.getX(), pPos.getY(), pPos.getZ());
               areaeffectcloud.setRadius(0.5F * charges);
               areaeffectcloud.setRadiusOnUse(-0.5F);
               areaeffectcloud.setWaitTime(10);
               areaeffectcloud.setDuration(areaeffectcloud.getDuration() * charges / 2);
               areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / areaeffectcloud.getDuration());

               for (MobEffectInstance mobeffectinstance : effects) {
                  areaeffectcloud.addEffect(new MobEffectInstance(mobeffectinstance));
               }

               pLevel.addFreshEntity(areaeffectcloud);
            }
         }
      });
      return pState;
   }
   @Nullable
   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new BottleGourdBlockEntity(pPos, pState);
   }
}
