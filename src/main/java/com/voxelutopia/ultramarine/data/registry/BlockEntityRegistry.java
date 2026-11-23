package com.voxelutopia.ultramarine.data.registry;

import com.voxelutopia.ultramarine.world.block.entity.BottleGourdBlockEntity;
import com.voxelutopia.ultramarine.world.block.entity.BrickKilnBlockEntity;
import com.voxelutopia.ultramarine.world.block.entity.CenserBlockEntity;
import com.voxelutopia.ultramarine.world.block.entity.ContainerDecorativeBlockEntity;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;

public class BlockEntityRegistry {
   public static BlockEntityType<ContainerDecorativeBlockEntity> CONTAINER_DECORATIVE_BLOCK;
   public static BlockEntityType<CenserBlockEntity> CENSER;
   public static BlockEntityType<BottleGourdBlockEntity> BOTTLE_GOURD;
   public static BlockEntityType<BrickKilnBlockEntity> BRICK_KILN;
   static Set<Block> CONTAINER_BLOCKS = Set.of(
      BlockRegistry.SACK,
      BlockRegistry.BAMBOO_TEA_BASKET,
      BlockRegistry.EMPTY_BAMBOO_TEA_BASKET,
      BlockRegistry.GUNNY_SACK,
      BlockRegistry.FRUIT_BOX,
      BlockRegistry.WOODEN_CRATE,
      BlockRegistry.FOOD_HAMPER,
      BlockRegistry.OAK_CABINET,
      BlockRegistry.WARPED_CABINET,
      BlockRegistry.EBONY_CABINET
   );
   static Set<Block> CENSERS = Set.of(BlockRegistry.BRONZE_CENSER, BlockRegistry.ROYAL_CENSER);

   public static void registerModBlockEntities() {
      CONTAINER_DECORATIVE_BLOCK = (BlockEntityType<ContainerDecorativeBlockEntity>)Registry.register(
         BuiltInRegistries.BLOCK_ENTITY_TYPE,
         ResourceLocation.fromNamespaceAndPath("ultramarine", "container_decorative_block_entity"),
         Builder.of(ContainerDecorativeBlockEntity::new, new HashSet<>(CONTAINER_BLOCKS).toArray(new Block[0])).build(null)
      );
      CENSER = (BlockEntityType<CenserBlockEntity>)Registry.register(
         BuiltInRegistries.BLOCK_ENTITY_TYPE,
         ResourceLocation.fromNamespaceAndPath("ultramarine", "censer_block_entity"),
         Builder.of(CenserBlockEntity::new, new HashSet<>(CENSERS).toArray(new Block[0])).build(null)
      );
      BOTTLE_GOURD = (BlockEntityType<BottleGourdBlockEntity>)Registry.register(
         BuiltInRegistries.BLOCK_ENTITY_TYPE,
         ResourceLocation.fromNamespaceAndPath("ultramarine", "bottle_gourd_entity"),
         Builder.of(BottleGourdBlockEntity::new, new Block[]{BlockRegistry.BOTTLE_GOURD}).build(null)
      );
      BRICK_KILN = (BlockEntityType<BrickKilnBlockEntity>)Registry.register(
         BuiltInRegistries.BLOCK_ENTITY_TYPE,
         ResourceLocation.fromNamespaceAndPath("ultramarine", "brick_kiln_block_entity"),
         Builder.of(BrickKilnBlockEntity::new, new Block[]{BlockRegistry.BRICK_KILN}).build(null)
      );
   }
}
