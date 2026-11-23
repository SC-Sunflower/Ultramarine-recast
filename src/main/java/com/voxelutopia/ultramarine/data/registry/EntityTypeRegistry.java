package com.voxelutopia.ultramarine.data.registry;

import com.voxelutopia.ultramarine.world.entity.SeatEntity;
import com.voxelutopia.ultramarine.world.entity.TravellingMerchant;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;

public class EntityTypeRegistry {
   public static EntityType<SeatEntity> SEAT;
   public static EntityType<TravellingMerchant> TRAVELLING_MERCHANT;

   public static void registerModEntities() {
      SEAT = (EntityType<SeatEntity>)Registry.register(
         BuiltInRegistries.ENTITY_TYPE,
         ResourceLocation.fromNamespaceAndPath("ultramarine", "seat"),
         Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC).sized(0.1F, 0.1F).clientTrackingRange(64).updateInterval(20).build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("ultramarine", "seat")))
      );
      TRAVELLING_MERCHANT = (EntityType<TravellingMerchant>)Registry.register(
         BuiltInRegistries.ENTITY_TYPE,
         ResourceLocation.fromNamespaceAndPath("ultramarine", "travelling_merchant"),
         Builder.of(TravellingMerchant::new, MobCategory.MISC)
            .sized(0.6F, 0.2F)
            .clientTrackingRange(64)
            .updateInterval(20)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("ultramarine", "travelling_merchant")))
      );
   }
}
