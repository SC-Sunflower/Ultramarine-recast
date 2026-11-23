package com.voxelutopia.ultramarine.data.registry;

import com.voxelutopia.ultramarine.event.EntityEvents;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.lang3.tuple.Pair;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

public class VillagerTradingsRegister {
   private static final Map<ResourceKey<VillagerProfession>, Int2ObjectMap<ItemListing[]>> VANILLA_TRADES = new HashMap<>();
   private static final Int2ObjectMap<ItemListing[]> WANDERER_TRADES = new Int2ObjectOpenHashMap();
   private static ItemListing[] safeTrades(ItemListing[] arr) {
      return arr == null ? new ItemListing[0] : arr;
   }


   public static void loadTrades() {
      ServerLifecycleEvents.SERVER_STARTING.register((ServerStarting)server -> {
         postWandererEvent();
         postVillagerEvents();
      });
   }

   // Ported from Ultramarine 0.5.1 (Forge WandererTradesEvent): rare trades for the vanilla
   // wandering trader. Must be called before the server starts (from the mod initializer).
   public static void registerWandererTrades() {
      List<ItemListing> rareTrades = new ArrayList<>(Arrays.asList(safeTrades(WANDERER_TRADES.get(2))));
      rareTrades.add((trader, random) -> new MerchantOffer(new ItemCost((Items.EMERALD), 6), new ItemStack(ItemRegistry.INCENSE, 1), 6, 30, 0.05F));
      rareTrades.add((trader, random) -> new MerchantOffer(new ItemCost((Items.EMERALD), 6), new ItemStack(ItemRegistry.SILK, 1), 8, 30, 0.05F));
      rareTrades.add((trader, random) -> new MerchantOffer(new ItemCost((Items.EMERALD), 10), new ItemStack(ItemRegistry.JADE, 1), 2, 30, 0.05F));
      rareTrades.add((trader, random) -> new MerchantOffer(new ItemCost((Items.EMERALD), 12), new ItemStack(ItemRegistry.PAINTING_SCROLL, 1), 1, 30, 0.05F));
      rareTrades.add((trader, random) -> new MerchantOffer(new ItemCost((Items.EMERALD), 20), new ItemStack(ItemRegistry.BLUE_AND_WHITE_PORCELAIN_VASE, 1), 1, 30, 0.05F));
      WANDERER_TRADES.put(2, rareTrades.toArray(new ItemListing[0]));
   }

   private static void postWandererEvent() {
      List<ItemListing> generic = NonNullList.create();
      List<ItemListing> rare = NonNullList.create();
      generic.addAll(Arrays.asList(safeTrades(WANDERER_TRADES.get(1))));
      rare.addAll(Arrays.asList(safeTrades(WANDERER_TRADES.get(2))));
      // replaced via mixin
      // replaced via mixin
   }

   private static void postVillagerEvents() {
      for (VillagerProfession prof : BuiltInRegistries.VILLAGER_PROFESSION) {
         Int2ObjectMap<ItemListing[]> trades = VANILLA_TRADES.getOrDefault(prof, new Int2ObjectOpenHashMap());
         Int2ObjectMap<List<ItemListing>> mutableTrades = new Int2ObjectOpenHashMap();

         for (int i = 1; i < 6; i++) {
            mutableTrades.put(i, NonNullList.create());
         }

         trades.int2ObjectEntrySet().forEach(e -> Arrays.stream((ItemListing[])e.getValue()).forEach(((List)mutableTrades.get(e.getIntKey()))::add));
         ((EntityEvents.VillagerTrade)EntityEvents.VILLAGER_TRADE.invoker()).modifyVillagerTrade(mutableTrades, prof);
         Int2ObjectMap<ItemListing[]> newTrades = new Int2ObjectOpenHashMap();
         mutableTrades.int2ObjectEntrySet().forEach(e -> newTrades.put(e.getIntKey(), ((List<ItemListing>)e.getValue()).toArray(new ItemListing[0])));
         VillagerTrades.TRADES.put(BuiltInRegistries.VILLAGER_PROFESSION.getResourceKey(prof).orElseThrow(), newTrades);
      }
   }

   static {
      VillagerTrades.TRADES
         .forEach(
            (key, value) -> {
               Int2ObjectMap<ItemListing[]> copy = new Int2ObjectOpenHashMap();
               value.int2ObjectEntrySet()
                  .forEach(
                     ent -> copy.put(ent.getIntKey(), (ItemListing[])Arrays.copyOf((ItemListing[])ent.getValue(), ((ItemListing[])ent.getValue()).length))
                  );
               VANILLA_TRADES.put(key, copy);
            }
         );
      for (Pair<ItemListing[], Integer> pair : VillagerTrades.WANDERING_TRADER_TRADES) {
         WANDERER_TRADES.put(pair.getRight(), (ItemListing[])Arrays.copyOf((ItemListing[])pair.getLeft(), ((ItemListing[])pair.getLeft()).length));
      }
   }
}
