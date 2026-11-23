package com.voxelutopia.ultramarine.data;

import com.voxelutopia.ultramarine.data.registry.CreativeTabRegistry;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public enum ModCreativeTab {
   MATERIALS(CreativeTabRegistry.MATERIALS),
   TOOLS(CreativeTabRegistry.TOOLS),
   BUILDING_BLOCKS(CreativeTabRegistry.BUILDING_BLOCKS),
   DECORATIVE_BLOCKS(CreativeTabRegistry.DECORATIVE_BLOCKS),
   DECORATIONS(CreativeTabRegistry.DECORATIONS),
   FURNITURE(CreativeTabRegistry.FURNITURE),
   WINDOWS_AND_DOORS(CreativeTabRegistry.WINDOWS_AND_DOORS),
   PLANTS(CreativeTabRegistry.PLANTS),
   LAMPS(CreativeTabRegistry.LAMPS);

   public static final Map<ModCreativeTab, Set<Item>> itemSets = new LinkedHashMap<>();
   final CreativeModeTab tab;

   ModCreativeTab(CreativeModeTab tab) {
      this.tab = tab;
   }

   public static void putItemInSet(Item item, ModCreativeTab tab) {
      itemSets.get(tab).add(item);
   }

   public CreativeModeTab getTab() {
      return this.tab;
   }

   static {
      Arrays.stream(values()).forEach(tab -> itemSets.put(tab, new LinkedHashSet<>()));
   }
}
