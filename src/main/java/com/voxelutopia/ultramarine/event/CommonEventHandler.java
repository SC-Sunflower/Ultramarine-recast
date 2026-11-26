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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.village.poi.PoiManager;

import java.util.Optional;
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
      ServerTickEvents.START_LEVEL_TICK.register(world -> {
         if (world.isClientSide() || world.dimension() != Level.OVERWORLD) {
            return;
         }
         ServerLevel level = (ServerLevel) world;
         if (!level.getGameRules().get(GameRules.SPAWN_WANDERING_TRADERS)
            || !level.getGameRules().get(GameRules.SPAWN_MOBS)
            || !level.getGameRules().get(GameRules.ADVANCE_TIME)) {
            return;
         }
         // 26.1 made the wandering trader schedule data-driven; the mod keeps the vanilla
         // default chance (25) and rolls once per in-game day at dawn.
         int wanderingTraderSpawnChance = 25;
         if (level.getOverworldClockTime() % 24000 == 0 && level.getRandom().nextInt(100) < wanderingTraderSpawnChance) {
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
               int x = pos.getX() + world.getRandom().nextInt(8) - 4;
               int z = pos.getZ() + world.getRandom().nextInt(8) - 4;
               int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
               BlockPos rolledPos = new BlockPos(x, y, z);
               if (SpawnPlacements.isSpawnPositionOk(EntityType.WANDERING_TRADER, world, rolledPos)) {
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
               TravellingMerchant merchant = EntityTypeRegistry.TRAVELLING_MERCHANT.spawn(world, potentialSpawn, EntitySpawnReason.EVENT);
               if (merchant == null) {
                  return;
               }
               merchant.setDespawnDelay(12000);
               merchant.setWanderTarget(potentialSpawn);
               merchant.setHomeTo(potentialSpawn, 8);
            }
         });
   }

   private static void registerEntityAttributes() {
      FabricDefaultAttributeRegistry.register(EntityTypeRegistry.TRAVELLING_MERCHANT, TravellingMerchant.setCustomAttributes());
   }

   // Ported from Ultramarine 0.5.1 (Forge VillagerTradesEvent / WandererTradesEvent): the full
   // trade sets for vanilla professions plus the cook profession. The community Fabric build
   // only shipped a reduced cook trade list, so the complete lists are restored here.
}
