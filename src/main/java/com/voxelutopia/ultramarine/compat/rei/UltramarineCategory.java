package com.voxelutopia.ultramarine.compat.rei;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Minimal REI 12 display category: identifier + title + icon. */
public class UltramarineCategory<T extends BasicDisplay> implements DisplayCategory<T> {
   private final CategoryIdentifier<T> identifier;
   private final Component title;
   private final Renderer icon;

   public UltramarineCategory(CategoryIdentifier<T> identifier, Component title, ItemStack icon) {
      this.identifier = identifier;
      this.title = title;
      this.icon = EntryStacks.of(icon);
   }

   @Override
   public CategoryIdentifier<? extends T> getCategoryIdentifier() {
      return this.identifier;
   }

   @Override
   public Component getTitle() {
      return this.title;
   }

   @Override
   public Renderer getIcon() {
      return this.icon;
   }
}
