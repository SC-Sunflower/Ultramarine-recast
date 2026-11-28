package com.voxelutopia.ultramarine.data.registry;

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
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

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

   // Ported from Ultramarine 0.5.1 (Forge WandererTradesEvent): rare trades for the vanilla
   // wandering trader. Must be called before the server starts (from the mod initializer).
   public static void registerWandererTrades() {
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 6), new ItemStack(ItemRegistry.INCENSE, 1), 6, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 6), new ItemStack(ItemRegistry.SILK, 1), 8, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 10), new ItemStack(ItemRegistry.JADE, 1), 2, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 12), new ItemStack(ItemRegistry.PAINTING_SCROLL, 1), 1, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 20), new ItemStack(ItemRegistry.BLUE_AND_WHITE_PORCELAIN_VASE, 1), 1, 30, 0.05F));
   }

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
