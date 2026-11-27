package com.voxelutopia.ultramarine.client.integration.jade;

import com.voxelutopia.ultramarine.world.block.entity.BrickKilnBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public enum BrickKilnComponent implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
   INSTANCE;

   public static final ResourceLocation BRICK_KILN = ResourceLocation.fromNamespaceAndPath("ultramarine", "brick_kiln");

   @Override
   public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
      CompoundTag data = blockAccessor.getServerData();
      if (data.contains("CookTime")) {
         int progress = data.getIntOr("CookTime", 0);
         int total = data.getIntOr("CookTimeTotal", 0);
         IElementHelper helper = IElementHelper.get();
         tooltip.add(helper.progress(total <= 0 ? 0.0F : Math.min(1.0F, (float) progress / total)));
      }
   }

   @Override
   public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
      BrickKilnBlockEntity kiln = (BrickKilnBlockEntity) blockAccessor.getBlockEntity();
      CompoundTag kilnTag = kiln.saveWithoutMetadata(blockAccessor.getLevel().registryAccess());
      compoundTag.putInt("CookTime", kilnTag.getIntOr("CookTime", 0));
      compoundTag.putInt("CookTimeTotal", kilnTag.getIntOr("CookTimeTotal", 0));
   }

   @Override
   public ResourceLocation getUid() {
      return BRICK_KILN;
   }
}
