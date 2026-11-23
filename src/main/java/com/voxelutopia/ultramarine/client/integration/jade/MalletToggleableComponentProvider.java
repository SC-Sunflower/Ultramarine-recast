package com.voxelutopia.ultramarine.client.integration.jade;

import com.voxelutopia.ultramarine.data.registry.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum MalletToggleableComponentProvider implements IBlockComponentProvider {
   INSTANCE;

   public static final ResourceLocation MALLET_TOGGLEABLE = new ResourceLocation("ultramarine", "mallet_toggleable");

   public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
      IElementHelper elements = tooltip.getElementHelper();
      IElement icon = elements.item(ItemRegistry.WOODEN_MALLET.getDefaultInstance(), 0.5F).size(new Vec2(10.0F, 10.0F)).translate(new Vec2(0.0F, -1.0F));
      tooltip.add(icon);
      tooltip.append(Component.translatable("gui.jade.plugin_ultramarine.mallet_toggleable"));
   }

   public ResourceLocation getUid() {
      return MALLET_TOGGLEABLE;
   }
}
