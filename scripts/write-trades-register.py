# -*- coding: utf-8 -*-
"""Write per-API-group VillagerTradingsRegister versions.

Group 1 (1.20.1-1.20.4): Trade costs are ItemStacks; tables are
    Map<VillagerProfession, Int2ObjectMap<ItemListing[]>> and
    Int2ObjectMap<ItemListing[]> for the wandering trader.
Group 2 (1.20.5-1.21.4): ItemCost-based offers; same table shapes as group 1.
Group 3 (1.21.5+): TRADES keyed by ResourceKey<VillagerProfession>; the wandering
    trader table is a List<Pair<ItemListing[], Integer>>.
"""
import os

BASE = r"C:\Users\34324\Desktop"

GROUP1 = ["um-main-wt", "um-1202-wt", "um-1203-wt", "um-1204-wt"]
GROUP2 = ["um-1205-wt", "um-1206-wt", "um-1211-wt", "um-1214-wt"]
GROUP3 = ["um-1215-wt", "um-1216-wt", "um-1217-wt", "um-1219-wt", "um-12110-wt", "um-12111-wt"]

HEADER = """package com.voxelutopia.ultramarine.data.registry;

import com.voxelutopia.ultramarine.event.EntityEvents;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
"""

RARE_STACK = """   public static void registerWandererTrades() {
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 6), new ItemStack(ItemRegistry.INCENSE, 1), 6, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 6), new ItemStack(ItemRegistry.SILK, 1), 8, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 10), new ItemStack(ItemRegistry.JADE, 1), 2, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 12), new ItemStack(ItemRegistry.PAINTING_SCROLL, 1), 1, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 20), new ItemStack(ItemRegistry.BLUE_AND_WHITE_PORCELAIN_VASE, 1), 1, 30, 0.05F));
   }
"""

RARE_ITEMCOST = """   public static void registerWandererTrades() {
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 6), new ItemStack(ItemRegistry.INCENSE, 1), 6, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 6), new ItemStack(ItemRegistry.SILK, 1), 8, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 10), new ItemStack(ItemRegistry.JADE, 1), 2, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 12), new ItemStack(ItemRegistry.PAINTING_SCROLL, 1), 1, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 20), new ItemStack(ItemRegistry.BLUE_AND_WHITE_PORCELAIN_VASE, 1), 1, 30, 0.05F));
   }
"""

CLASS_G1 = HEADER.replace("import net.minecraft.world.item.trading.MerchantOffer;\n", "import net.minecraft.world.item.trading.MerchantOffer;\n") + """import org.apache.commons.lang3.tuple.Pair;
"""

CLASS_G1 = HEADER + """import org.apache.commons.lang3.tuple.Pair;
""" + """
public class VillagerTradingsRegister {
   // Trades added by this mod, kept separate from the vanilla tables so the live
   // tables can be merged instead of replaced (other mods may register trades too).
   private static final List<ItemListing> WANDERER_RARE_TRADES = new ArrayList<>();

   public static void loadTrades() {
      ServerLifecycleEvents.SERVER_STARTING.register((ServerStarting)server -> {
         postWandererEvent();
         postVillagerEvents();
      });
   }

""" + RARE_STACK + """
   private static void postWandererEvent() {
      // Append our rare trades to whatever is currently in the live table instead of
      // overwriting trades other mods may have registered.
      List<ItemListing> rare = new ArrayList<>(Arrays.asList(safeTrades(VillagerTrades.WANDERING_TRADER_TRADES.get(2))));
      rare.addAll(WANDERER_RARE_TRADES);
      VillagerTrades.WANDERING_TRADER_TRADES.put(2, rare.toArray(new ItemListing[0]));
   }

   private static void postVillagerEvents() {
      for (VillagerProfession prof : BuiltInRegistries.VILLAGER_PROFESSION) {
         // Start from the live table so trades registered by other mods survive this merge.
         Int2ObjectMap<ItemListing[]> current = VillagerTrades.TRADES.get(prof);
         Int2ObjectMap<List<ItemListing>> mutableTrades = new Int2ObjectOpenHashMap<>();
         for (int i = 1; i < 6; i++) {
            mutableTrades.put(i, NonNullList.create());
         }
         if (current != null) {
            for (Int2ObjectMap.Entry<ItemListing[]> entry : current.int2ObjectEntrySet()) {
               mutableTrades.computeIfAbsent(entry.getIntKey(), key -> NonNullList.create())
                  .addAll(Arrays.asList(safeTrades(entry.getValue())));
            }
         }
         ((EntityEvents.VillagerTrade)EntityEvents.VILLAGER_TRADE.invoker()).modifyVillagerTrade(mutableTrades, prof);
         Int2ObjectMap<ItemListing[]> newTrades = new Int2ObjectOpenHashMap<>();
         for (Int2ObjectMap.Entry<List<ItemListing>> entry : mutableTrades.int2ObjectEntrySet()) {
            newTrades.put(entry.getIntKey(), entry.getValue().toArray(new ItemListing[0]));
         }
         VillagerTrades.TRADES.put(prof, newTrades);
      }
   }

   private static ItemListing[] safeTrades(ItemListing[] arr) {
      return arr == null ? new ItemListing[0] : arr;
   }
}
"""

CLASS_G3 = HEADER + """import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.ItemCost;
import org.apache.commons.lang3.tuple.Pair;
""" + """
public class VillagerTradingsRegister {
   // Trades added by this mod, kept separate from the vanilla tables so the live
   // tables can be merged instead of replaced (other mods may register trades too).
   private static final List<ItemListing> WANDERER_RARE_TRADES = new ArrayList<>();

   public static void loadTrades() {
      ServerLifecycleEvents.SERVER_STARTING.register((ServerStarting)server -> {
         postWandererEvent();
         postVillagerEvents();
      });
   }

""" + RARE_ITEMCOST + """
   private static void postWandererEvent() {
      // 1.21.5+ keeps the wandering trader table as a list of (listings, level) pairs;
      // rebuild it with our rare trades merged so other mods' entries survive.
      List<Pair<ItemListing[], Integer>> updated = new ArrayList<>();
      boolean rareTierFound = false;
      for (Pair<ItemListing[], Integer> pair : VillagerTrades.WANDERING_TRADER_TRADES) {
         if (pair.getRight() == 2) {
            List<ItemListing> listings = new ArrayList<>(Arrays.asList(safeTrades(pair.getLeft())));
            listings.addAll(WANDERER_RARE_TRADES);
            updated.add(Pair.of(listings.toArray(new ItemListing[0]), pair.getRight()));
            rareTierFound = true;
         } else {
            updated.add(pair);
         }
      }
      if (!rareTierFound) {
         updated.add(Pair.of(WANDERER_RARE_TRADES.toArray(new ItemListing[0]), 2));
      }
      VillagerTrades.WANDERING_TRADER_TRADES = updated;
   }

   private static void postVillagerEvents() {
      for (VillagerProfession prof : BuiltInRegistries.VILLAGER_PROFESSION) {
         ResourceKey<VillagerProfession> key = BuiltInRegistries.VILLAGER_PROFESSION.getResourceKey(prof).orElse(null);
         if (key == null) {
            continue;
         }
         // Start from the live table so trades registered by other mods survive this merge.
         Int2ObjectMap<ItemListing[]> current = VillagerTrades.TRADES.get(key);
         Int2ObjectMap<List<ItemListing>> mutableTrades = new Int2ObjectOpenHashMap<>();
         for (int i = 1; i < 6; i++) {
            mutableTrades.put(i, NonNullList.create());
         }
         if (current != null) {
            for (Int2ObjectMap.Entry<ItemListing[]> entry : current.int2ObjectEntrySet()) {
               mutableTrades.computeIfAbsent(entry.getIntKey(), key2 -> NonNullList.create())
                  .addAll(Arrays.asList(safeTrades(entry.getValue())));
            }
         }
         ((EntityEvents.VillagerTrade)EntityEvents.VILLAGER_TRADE.invoker()).modifyVillagerTrade(mutableTrades, prof);
         Int2ObjectMap<ItemListing[]> newTrades = new Int2ObjectOpenHashMap<>();
         for (Int2ObjectMap.Entry<List<ItemListing>> entry : mutableTrades.int2ObjectEntrySet()) {
            newTrades.put(entry.getIntKey(), entry.getValue().toArray(new ItemListing[0]));
         }
         VillagerTrades.TRADES.put(key, newTrades);
      }
   }

   private static ItemListing[] safeTrades(ItemListing[] arr) {
      return arr == null ? new ItemListing[0] : arr;
   }
}
"""


def write_class(group_name, wts, body):
    for d in wts:
        p = os.path.join(BASE, d, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                         "data", "registry", "VillagerTradingsRegister.java")
        if not os.path.exists(p):
            print(group_name, d, "NO FILE")
            continue
        with open(p, "w", encoding="utf-8", newline="\n") as f:
            f.write(body)
        print(group_name, d, "written")


write_class("G1", GROUP1, CLASS_G1)
write_class("G2", GROUP2, CLASS_G1.replace(
    "import net.minecraft.world.item.trading.MerchantOffer;\n",
    "import net.minecraft.world.item.trading.MerchantOffer;\nimport net.minecraft.world.item.trading.ItemCost;\n",
).replace(RARE_STACK, RARE_ITEMCOST))
write_class("G3", GROUP3, CLASS_G3)
