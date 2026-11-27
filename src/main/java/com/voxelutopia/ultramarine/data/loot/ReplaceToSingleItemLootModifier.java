package com.voxelutopia.ultramarine.data.loot;

import com.voxelutopia.ultramarine.data.registry.ItemRegistry;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.UniformGenerator;

/**
 * Loot table injections ported from Ultramarine 0.7 global loot modifiers (NeoForge 1.21.1).
 * Minecraft 26.3 changed loot number providers to Holder-based ContextIntProvider values.
 */
public class ReplaceToSingleItemLootModifier {
    public static void register() {
        // Trail ruins archaeology loot (0.7)
        appendArchaeology(Identifier.tryBuild("minecraft", "archaeology/trail_ruins_rare"), 0.1F, trailRuinsRare());
        appendArchaeology(Identifier.tryBuild("minecraft", "archaeology/trail_ruins_common"), 0.2F, trailRuinsCommon());
        // Animal fur and raw meat drops (0.7)
        appendEntityDrop(Identifier.tryBuild("minecraft", "entities/fox"), ItemRegistry.FUR, 0.6F, between(1, 2));
        appendEntityDrop(Identifier.tryBuild("minecraft", "entities/goat"), ItemRegistry.FUR, 0.8F, between(1, 2));
        appendEntityDrop(Identifier.tryBuild("minecraft", "entities/rabbit"), ItemRegistry.FUR, 0.8F, exactly(1));
        appendEntityDrop(Identifier.tryBuild("minecraft", "entities/hoglin"), ItemRegistry.RAW_MEAT, 0.8F, between(1, 3));
        appendEntityDrop(Identifier.tryBuild("minecraft", "entities/ravager"), ItemRegistry.RAW_MEAT, 0.8F, between(1, 2));
    }

    private static Holder<ContextIntProvider> exactly(int value) {
        return Holder.direct(new ConstantValue(value));
    }

    private static Holder<ContextIntProvider> between(int min, int max) {
        return Holder.direct(new UniformGenerator(exactly(min), exactly(max)));
    }

    private static void appendEntityDrop(Identifier target, ItemLike item, float chance, Holder<ContextIntProvider> count) {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (key.identifier().equals(target)) {
                tableBuilder.pool(LootPool.lootPool()
                        .setRolls(exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(chance))
                        .add(LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(count)))
                        .build());
            }
        });
    }

    private static void appendArchaeology(Identifier target, float chance, Map<ItemLike, Integer> items) {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (key.identifier().equals(target)) {
                LootPool.Builder builder = LootPool.lootPool()
                        .setRolls(exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(chance));
                items.forEach((item, weight) -> builder.add(LootItem.lootTableItem(item).setWeight(weight)));
                tableBuilder.pool(builder.build());
            }
        });
    }

    private static Map<ItemLike, Integer> trailRuinsRare() {
        Map<ItemLike, Integer> items = new LinkedHashMap<>();
        items.put(ItemRegistry.BLUE_AND_WHITE_PORCELAIN_PIECE, 20);
        items.put(ItemRegistry.RAW_HEMATITE, 20);
        items.put(ItemRegistry.RAW_COBALT, 20);
        items.put(ItemRegistry.TALL_BLUE_AND_WHITE_PORCELAIN_VASE, 10);
        items.put(ItemRegistry.BLUE_AND_WHITE_PORCELAIN_PLATE, 10);
        items.put(ItemRegistry.CLAY_DOLL_MALE, 10);
        items.put(ItemRegistry.CLAY_DOLL_FEMALE, 10);
        items.put(ItemRegistry.BAMBOO_SLIPS, 10);
        items.put(ItemRegistry.BOOK_STACK, 10);
        items.put(ItemRegistry.BLUE_BOOK, 10);
        items.put(ItemRegistry.JADE, 5);
        items.put(ItemRegistry.PAINTING_SCROLL, 5);
        items.put(ItemRegistry.BRONZE_CENSER, 5);
        items.put(ItemRegistry.KNIFE, 5);
        items.put(ItemRegistry.BOTTLE_GOURD, 5);
        items.put(ItemRegistry.JADE_PENDANT, 2);
        items.put(ItemRegistry.BLUE_AND_WHITE_PORCELAIN_VASE, 2);
        items.put(ItemRegistry.LARGE_BLUE_AND_WHITE_PORCELAIN_VASE, 2);
        items.put(ItemRegistry.SHORT_BLUE_AND_WHITE_PORCELAIN_POT, 2);
        items.put(ItemRegistry.TALL_BLUE_AND_WHITE_PORCELAIN_POT, 2);
        items.put(ItemRegistry.BLUE_AND_WHITE_PORCELAIN_BOWL, 2);
        items.put(ItemRegistry.IMPERIAL_JADE_SEAL, 1);
        items.put(ItemRegistry.BRONZE_DING, 1);
        return items;
    }

    private static Map<ItemLike, Integer> trailRuinsCommon() {
        Map<ItemLike, Integer> items = new LinkedHashMap<>();
        items.put(ItemRegistry.CYAN_BRICK, 8);
        items.put(ItemRegistry.BLACK_BRICK, 8);
        items.put(ItemRegistry.BROWNISH_RED_STONE_BRICK, 8);
        items.put(ItemRegistry.POLISHED_ROSEWOOD_PLANK, 8);
        items.put(ItemRegistry.POLISHED_EBONY_PLANK, 8);
        items.put(ItemRegistry.GRAY_ROOF_TILE, 4);
        items.put(ItemRegistry.YELLOW_ROOF_TILE, 4);
        items.put(ItemRegistry.GREEN_ROOF_TILE, 4);
        items.put(ItemRegistry.BLUE_ROOF_TILE, 4);
        items.put(ItemRegistry.CYAN_ROOF_TILE, 4);
        items.put(ItemRegistry.BLACK_ROOF_TILE, 4);
        items.put(ItemRegistry.BRONZE_INGOT, 2);
        items.put(ItemRegistry.COPPER_CASH_COIN, 2);
        items.put(ItemRegistry.SILK, 2);
        items.put(ItemRegistry.MAGNESITE, 2);
        items.put(ItemRegistry.BLUE_AND_WHITE_PORCELAIN_SHARDS, 2);
        items.put(ItemRegistry.PORCELAIN_PIECE, 2);
        items.put(ItemRegistry.SMALL_WHITE_PORCELAIN_VASE, 1);
        items.put(ItemRegistry.MEDIUM_WHITE_PORCELAIN_VASE, 1);
        items.put(ItemRegistry.LARGE_WHITE_PORCELAIN_VASE, 1);
        items.put(ItemRegistry.SMALL_GREEN_PORCELAIN_VASE, 1);
        items.put(ItemRegistry.MEDIUM_GREEN_PORCELAIN_VASE, 1);
        items.put(ItemRegistry.TALL_BLUE_PORCELAIN_VASE, 1);
        items.put(ItemRegistry.BLACK_PORCELAIN_PLATE, 1);
        return items;
    }
}
