package com.voxelutopia.ultramarine.client.integration.jade;

import com.voxelutopia.ultramarine.data.registry.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

public enum MalletToggleableComponentProvider implements IBlockComponentProvider {
   INSTANCE;

   public static final Identifier MALLET_TOGGLEABLE = Identifier.fromNamespaceAndPath("ultramarine", "mallet_toggleable");

   @Override
   public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
      tooltip.add(JadeUI.item(ItemRegistry.WOODEN_MALLET.getDefaultInstance(), 0.5F).size(10, 10).offset(0, -1));
      tooltip.append(Component.translatable("gui.jade.plugin_ultramarine.mallet_toggleable"));
   }

   @Override
   public Identifier getUid() {
      return MALLET_TOGGLEABLE;
   }
}
