package com.voxelutopia.ultramarine.data.loot;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents.Modify;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootPool.Builder;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Loot table injections ported from Ultramarine 0.7 global loot modifiers (NeoForge 1.21.1).
 * NeoForge uses {@code neoforge:add_table} GLMs; on Fabric we append a LootTableReference pool
 * to the matching vanilla loot tables.
 */
public class ReplaceToSingleItemLootModifier {
   public static void register() {
      // Trail ruins archaeology loot (0.7)
      addTableWithChance("minecraft", "archaeology/trail_ruins_rare", "ultramarine", "archaeology/trail_ruins_rare", 0.1F);
      addTableWithChance("minecraft", "archaeology/trail_ruins_common", "ultramarine", "archaeology/trail_ruins_common", 0.2F);
      // Animal fur and raw meat drops (0.7)
      addTable("minecraft", "entities/fox", "ultramarine", "entities/fox");
      addTable("minecraft", "entities/goat", "ultramarine", "entities/goat");
      addTable("minecraft", "entities/rabbit", "ultramarine", "entities/rabbit");
      addTable("minecraft", "entities/hoglin", "ultramarine", "entities/hoglin");
      addTable("minecraft", "entities/ravager", "ultramarine", "entities/ravager");
   }

   private static void addTable(String targetNamespace, String targetPath, String sourceNamespace, String sourcePath) {
      LootTableEvents.MODIFY.register(
         (Modify) (resourceManager, lootManager, id, tableBuilder, source) -> {
            if (id.equals(new ResourceLocation(targetNamespace, targetPath))) {
               Builder poolBuilder = LootPool.lootPool()
                  .setRolls(ConstantValue.exactly(1.0F))
                  .add(LootTableReference.lootTableReference(new ResourceLocation(sourceNamespace, sourcePath)));
               tableBuilder.pool(poolBuilder.build());
            }
         }
      );
   }

   private static void addTableWithChance(String targetNamespace, String targetPath, String sourceNamespace, String sourcePath, float chance) {
      LootTableEvents.MODIFY.register(
         (Modify) (resourceManager, lootManager, id, tableBuilder, source) -> {
            if (id.equals(new ResourceLocation(targetNamespace, targetPath))) {
               Builder poolBuilder = LootPool.lootPool()
                  .setRolls(ConstantValue.exactly(1.0F))
                  .when(LootItemRandomChanceCondition.randomChance(chance))
                  .add(LootTableReference.lootTableReference(new ResourceLocation(sourceNamespace, sourcePath)));
               tableBuilder.pool(poolBuilder.build());
            }
         }
      );
   }
}
