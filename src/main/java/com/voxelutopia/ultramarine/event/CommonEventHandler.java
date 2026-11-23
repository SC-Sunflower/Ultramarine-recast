package com.voxelutopia.ultramarine.event;

import com.voxelutopia.ultramarine.data.ModBlockTags;
import com.voxelutopia.ultramarine.data.registry.EntityTypeRegistry;
import com.voxelutopia.ultramarine.data.registry.ItemRegistry;
import com.voxelutopia.ultramarine.data.registry.PoiTypeRegistry;
import com.voxelutopia.ultramarine.data.registry.VillagerProfessionRegistry;
import com.voxelutopia.ultramarine.world.block.SnowRoofRidge;
import com.voxelutopia.ultramarine.world.entity.TravellingMerchant;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.entity.ai.village.poi.PoiManager;

import java.util.List;

/**
 * Common event handlers ported from Ultramarine 0.5.1 (Forge) to Fabric.
 * Includes the handlers that were missing from the community Fabric build:
 * roof snow fall, travelling merchant natural spawning and the full villager trades.
 */
public class CommonEventHandler {
   public static void init() {
      breakSpeed();
      travellingMerchantSpawnAttempt();
      registerEntityAttributes();
      villagerTraders();
   }

   private static void breakSpeed() {
      PlayerEvents.BREAK_SPEED.register((PlayerEvents.BreakSpeed)(player, state, pos, speed) -> {
         if (state.is(ModBlockTags.MINEABLE_WITH_SHEARS) && player.getItemInHand(player.getUsedItemHand()).is(Items.SHEARS)) {
            speed *= 4.0F;
         }

         return speed;
      });
   }


   // Ported from Ultramarine 0.5.1 (Forge TickEvent.LevelTickEvent): spawns the travelling
   // merchant near a trade POI with the vanilla wandering trader probability, every day at dawn.
   private static void travellingMerchantSpawnAttempt() {
      ServerTickEvents.START_WORLD_TICK.register(world -> {
         if (world.isClientSide() || world.dimension() != Level.OVERWORLD) {
            return;
         }
         ServerLevel level = (ServerLevel) world;
         ServerLevelData levelData = (ServerLevelData) level.getLevelData();
         if (!level.getGameRules().getBoolean(GameRules.RULE_DO_TRADER_SPAWNING)
            || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
            || !level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            return;
         }
         int wanderingTraderSpawnChance = levelData.getWanderingTraderSpawnChance();
         if (level.getDayTime() % 24000 == 0 && level.random.nextInt(100) < wanderingTraderSpawnChance) {
            spawnTrader(level);
         }
      });
   }

   private static void spawnTrader(ServerLevel world) {
      ServerPlayer player = world.getRandomPlayer();
      if (player == null) {
         return;
      }
      BlockPos blockpos = player.blockPosition();
      world.getPoiManager()
         .find(
            poi -> poi.value() == PoiTypeRegistry.TRADE_POI,
            pos -> true,
            blockpos,
            16,
            PoiManager.Occupancy.ANY
         )
         .ifPresent(pos -> {
            BlockPos potentialSpawn = null;
            for (int i = 0; i < 10; ++i) {
               int x = pos.getX() + world.random.nextInt(8) - 4;
               int z = pos.getZ() + world.random.nextInt(8) - 4;
               int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
               BlockPos rolledPos = new BlockPos(x, y, z);
               if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, world, rolledPos, EntityType.WANDERING_TRADER)) {
                  potentialSpawn = rolledPos;
                  break;
               }
            }
            if (potentialSpawn == null) {
               return;
            }

            boolean hasSpaceAtSpawn = true;
            for (BlockPos checkPos : BlockPos.betweenClosed(potentialSpawn, potentialSpawn.offset(1, 2, 1))) {
               if (!world.getBlockState(checkPos).getCollisionShape(world, checkPos).isEmpty()) {
                  hasSpaceAtSpawn = false;
                  break;
               }
            }

            if (hasSpaceAtSpawn && !world.getBiome(pos).is(Biomes.THE_VOID)) {
               TravellingMerchant merchant = EntityTypeRegistry.TRAVELLING_MERCHANT.spawn(world, potentialSpawn, MobSpawnType.EVENT);
               if (merchant == null) {
                  return;
               }
               merchant.setDespawnDelay(12000);
               merchant.setWanderTarget(potentialSpawn);
               merchant.restrictTo(potentialSpawn, 8);
            }
         });
   }

   private static void registerEntityAttributes() {
      FabricDefaultAttributeRegistry.register(EntityTypeRegistry.TRAVELLING_MERCHANT, TravellingMerchant.setCustomAttributes());
   }

   // Ported from Ultramarine 0.5.1 (Forge VillagerTradesEvent / WandererTradesEvent): the full
   // trade sets for vanilla professions plus the cook profession. The community Fabric build
   // only shipped a reduced cook trade list, so the complete lists are restored here.
   private static void villagerTraders() {
      EntityEvents.VILLAGER_TRADE
         .register(
            (EntityEvents.VillagerTrade)(trades, profession) -> {
               if (profession == VillagerProfession.MASON) {
                  ((List<ItemListing>)trades.get(4))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 5), new ItemStack(ItemRegistry.RAW_HEMATITE, 2), 12, 10, 0.05F));
                  ((List<ItemListing>)trades.get(4))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 5), new ItemStack(ItemRegistry.MAGNESITE, 2), 12, 10, 0.05F));
                  ((List<ItemListing>)trades.get(5))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 10), new ItemStack(ItemRegistry.JADE, 1), 6, 20, 0.05F));
               }

               if (profession == VillagerProfession.TOOLSMITH) {
                  ((List<ItemListing>)trades.get(4))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 6), new ItemStack(ItemRegistry.BRONZE_INGOT, 1), 12, 10, 0.05F));
                  ((List<ItemListing>)trades.get(5))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 40), new ItemStack(ItemRegistry.CARRIAGE, 1), 1, 10, 0.05F));
               }

               if (profession == VillagerProfession.FARMER) {
                  ((List<ItemListing>)trades.get(3))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 4), new ItemStack(ItemRegistry.POLISHED_ROSEWOOD_PLANK, 8), 12, 5, 0.05F));
                  ((List<ItemListing>)trades.get(3))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 4), new ItemStack(ItemRegistry.POLISHED_EBONY_PLANK, 8), 12, 5, 0.05F));
                  ((List<ItemListing>)trades.get(3))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 30), new ItemStack(ItemRegistry.SILK, 4), 4, 10, 0.05F));
                  ((List<ItemListing>)trades.get(4))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(ItemRegistry.EMPTY_BAMBOO_TEA_BASKET, 1), new ItemStack(Items.EMERALD, 20), new ItemStack(ItemRegistry.BAMBOO_TEA_BASKET, 1), 4, 10, 0.05F));
               }

               if (profession == VillagerProfession.LIBRARIAN) {
                  ((List<ItemListing>)trades.get(4))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 20), new ItemStack(ItemRegistry.XUAN_PAPER, 4), 10, 20, 0.05F));
                  ((List<ItemListing>)trades.get(5))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 30), new ItemStack(ItemRegistry.PORCELAIN_TEAPOT, 1), 2, 20, 0.05F));
               }

               if (profession == VillagerProfession.CLERIC) {
                  ((List<ItemListing>)trades.get(5))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 40), new ItemStack(ItemRegistry.SUNDIAL, 1), 1, 20, 0.05F));
               }

               if (profession == VillagerProfessionRegistry.COOK) {
                  ((List<ItemListing>)trades.get(1))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(ItemRegistry.RAW_MEAT, 10), new ItemStack(Items.EMERALD, 1), 12, 2, 0.05F));
                  ((List<ItemListing>)trades.get(1))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.PORKCHOP, 15), new ItemStack(Items.EMERALD, 1), 12, 2, 0.05F));
                  ((List<ItemListing>)trades.get(1))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.BEEF, 15), new ItemStack(Items.EMERALD, 1), 12, 2, 0.05F));
                  ((List<ItemListing>)trades.get(1))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.CHICKEN, 20), new ItemStack(Items.EMERALD, 1), 12, 2, 0.05F));
                  ((List<ItemListing>)trades.get(1))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 8), new ItemStack(ItemRegistry.COOKED_MEAT, 4), 12, 2, 0.05F));
                  ((List<ItemListing>)trades.get(2))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.MUTTON, 15), new ItemStack(Items.EMERALD, 1), 12, 4, 0.05F));
                  ((List<ItemListing>)trades.get(2))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.RABBIT, 15), new ItemStack(Items.EMERALD, 1), 12, 4, 0.05F));
                  ((List<ItemListing>)trades.get(2))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 6), new ItemStack(ItemRegistry.GREASE, 2), 8, 5, 0.05F));
                  ((List<ItemListing>)trades.get(2))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 6), new ItemStack(ItemRegistry.FUR, 2), 8, 5, 0.05F));
                  ((List<ItemListing>)trades.get(3))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.DRIED_KELP, 40), new ItemStack(Items.EMERALD, 2), 15, 8, 0.05F));
                  ((List<ItemListing>)trades.get(3))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.SUGAR, 40), new ItemStack(Items.EMERALD, 1), 20, 8, 0.05F));
                  ((List<ItemListing>)trades.get(3))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EGG, 16), new ItemStack(Items.EMERALD, 1), 20, 8, 0.05F));
                  ((List<ItemListing>)trades.get(3))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 12), new ItemStack(ItemRegistry.MUNG_BEAN_CAKE, 4), 12, 10, 0.05F));
                  ((List<ItemListing>)trades.get(3))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 12), new ItemStack(ItemRegistry.MOONCAKE, 4), 12, 10, 0.05F));
                  ((List<ItemListing>)trades.get(4))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 12), new ItemStack(ItemRegistry.CABBAGE_BASKET, 1), 3, 20, 0.05F));
                  ((List<ItemListing>)trades.get(4))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 12), new ItemStack(ItemRegistry.CELERY_BASKET, 1), 3, 20, 0.05F));
                  ((List<ItemListing>)trades.get(4))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 12), new ItemStack(ItemRegistry.ORANGE_BASKET, 1), 3, 20, 0.05F));
                  ((List<ItemListing>)trades.get(4))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 12), new ItemStack(ItemRegistry.APPLE_BASKET, 1), 3, 20, 0.05F));
                  ((List<ItemListing>)trades.get(4))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 12), new ItemStack(ItemRegistry.EGGPLANT_BASKET, 1), 3, 20, 0.05F));
                  ((List<ItemListing>)trades.get(4))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 12), new ItemStack(ItemRegistry.PEAR_BASKET, 1), 3, 20, 0.05F));
                  ((List<ItemListing>)trades.get(4))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 5), new ItemStack(ItemRegistry.BAOZI, 2), 6, 20, 0.05F));
                  ((List<ItemListing>)trades.get(5))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 25), new ItemStack(ItemRegistry.XIAOLONGBAO, 1), 2, 30, 0.05F));
                  ((List<ItemListing>)trades.get(5))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 30), new ItemStack(ItemRegistry.WINE_POT, 1), 2, 20, 0.05F));
                  ((List<ItemListing>)trades.get(5))
                     .add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 40), new ItemStack(ItemRegistry.BOTTLE_GOURD, 1), 1, 30, 0.05F));
               }

               return trades;
            }
         );
   }
}
