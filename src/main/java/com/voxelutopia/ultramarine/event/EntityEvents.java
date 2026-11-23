package com.voxelutopia.ultramarine.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;

public class EntityEvents {
   public static final Event<EntityEvents.VillagerTrade> VILLAGER_TRADE = EventFactory.createArrayBacked(
      EntityEvents.VillagerTrade.class, callbacks -> (trades, type) -> {
         for (EntityEvents.VillagerTrade callback : callbacks) {
            trades = callback.modifyVillagerTrade(trades, type);
         }

         return trades;
      }
   );

   @FunctionalInterface
   public interface VillagerTrade {
      Int2ObjectMap<List<ItemListing>> modifyVillagerTrade(Int2ObjectMap<List<ItemListing>> var1, VillagerProfession var2);
   }
}
