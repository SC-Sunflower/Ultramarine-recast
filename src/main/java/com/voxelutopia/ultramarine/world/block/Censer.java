package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.registry.BlockEntityRegistry;
import com.voxelutopia.ultramarine.data.registry.ItemRegistry;
import com.voxelutopia.ultramarine.world.block.entity.BlockEntityHelper;
import com.voxelutopia.ultramarine.world.block.entity.CenserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Censer extends DecorativeBlock implements EntityBlock {
   private final Vec3 smokeOffset;

   public Censer(DecorativeBlock.Builder builder, Vec3 smokeOffset) {
      super(builder);
      this.smokeOffset = smokeOffset;
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      ItemStack item = pPlayer.getItemInHand(pHand);
      if (item.is(ItemRegistry.INCENSE)) {
         if (!pPlayer.getAbilities().instabuild) {
            item.shrink(1);
         }

         pLevel.getBlockEntity(pPos, BlockEntityRegistry.CENSER).ifPresent(entity -> entity.lightIncense(pLevel, pPos, pState));
         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new CenserBlockEntity(pPos, pState);
   }

   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      super.animateTick(pState, pLevel, pPos, pRandom);
      if (pState.hasProperty(LIT) && (Boolean)pState.getValue(LIT)) {
         if (pLevel.getGameTime() % (3 - pRandom.nextInt(1)) == 0L) {
            for (int i = 0; i < 5; i++) {
               pLevel.addParticle(
                  ParticleTypes.CAMPFIRE_COSY_SMOKE,
                  pPos.getX() + this.smokeOffset.x + pRandom.nextFloat() * 0.4F - 0.2,
                  pPos.getY() + this.smokeOffset.y + pRandom.nextFloat() * 0.1F,
                  pPos.getZ() + this.smokeOffset.z + pRandom.nextFloat() * 0.4F - 0.2,
                  0.0,
                  pRandom.nextDouble() * 0.01,
                  0.0
               );
            }
         }
      }
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return pLevel.isClientSide ? null : BlockEntityHelper.createTickerHelper(pBlockEntityType, BlockEntityRegistry.CENSER, CenserBlockEntity::tick);
   }

   @NotNull
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockState state = super.getStateForPlacement(pContext);
      return this.isLuminous() ? (BlockState)state.setValue(LIT, false) : state;
   }

   public int getLightBlock(@NotNull BlockState state, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos) {
      if (this.isLuminous()) {
         return state.getValue(LIT) ? 4 : 0;
      } else {
         return 0;
      }
   }
}
