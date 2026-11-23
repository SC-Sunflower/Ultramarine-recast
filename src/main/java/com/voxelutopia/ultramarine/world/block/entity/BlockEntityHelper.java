package com.voxelutopia.ultramarine.world.block.entity;

import javax.annotation.Nullable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityHelper {
   @Nullable
   public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
      BlockEntityType<A> aBlockEntityType, BlockEntityType<E> eBlockEntityType, BlockEntityTicker<? super E> blockEntityTicker
   ) {
      return eBlockEntityType == aBlockEntityType ? (BlockEntityTicker<A>) blockEntityTicker : null;
   }
}
