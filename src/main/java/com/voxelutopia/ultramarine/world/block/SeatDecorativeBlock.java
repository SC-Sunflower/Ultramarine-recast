package com.voxelutopia.ultramarine.world.block;

import net.minecraft.world.item.ItemStack;

import com.voxelutopia.ultramarine.world.entity.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class SeatDecorativeBlock extends DecorativeBlock {
   private final Vec3 seatOffset;

   public SeatDecorativeBlock(SeatDecorativeBlock.Builder builder) {
      super(builder);
      this.seatOffset = builder.seatOffset;
   }

   public static SeatDecorativeBlock.Builder with(BaseBlockProperty property) {
      return new SeatDecorativeBlock.Builder(property);
   }

   public ItemInteractionResult useItemOn(ItemStack stack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pState.is(this) && pLevel.getEntitiesOfClass(SeatEntity.class, new AABB(pPos)).isEmpty()) {
         if (pLevel.isClientSide()) {
            // Client prediction: the seat entity is only created server-side, so
            // report success here to keep both sides' interaction results consistent.
            return ItemInteractionResult.SUCCESS;
         }
         SeatEntity seat = new SeatEntity(pLevel, Vec3.atCenterOf(pPos).add(this.seatOffset));
         pLevel.addFreshEntity(seat);
         if (!pPlayer.startRiding(seat)) {
            // Mounting failed; remove the freshly created entity so it does not linger.
            seat.discard();
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
         }
         return ItemInteractionResult.SUCCESS;
      } else {
         return super.useItemOn(stack, pState, pLevel, pPos, pPlayer, pHand, pHit);
      }
   }

   public static class Builder extends DecorativeBlock.Builder {
      private Vec3 seatOffset = new Vec3(0.0, 0.5, 0.0);

      public Builder(BaseBlockProperty property) {
         super(property);
      }

      public SeatDecorativeBlock.Builder seatOffset(Vec3 offset) {
         this.seatOffset = offset;
         return this;
      }

      public SeatDecorativeBlock build() {
         return new SeatDecorativeBlock(this);
      }
   }
}
