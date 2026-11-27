package com.voxelutopia.ultramarine;

import com.voxelutopia.ultramarine.data.ModFoods;
import com.voxelutopia.ultramarine.data.loot.ReplaceToSingleItemLootModifier;
import com.voxelutopia.ultramarine.data.registry.BlockApiLookupRegistry;
import com.voxelutopia.ultramarine.data.registry.BlockEntityRegistry;
import com.voxelutopia.ultramarine.data.registry.BlockRegistry;
import com.voxelutopia.ultramarine.data.registry.CreativeTabRegistry;
import com.voxelutopia.ultramarine.data.registry.EntityTypeRegistry;
import com.voxelutopia.ultramarine.data.registry.ItemRegistry;
import com.voxelutopia.ultramarine.data.registry.MenuTypeRegistry;
import com.voxelutopia.ultramarine.data.registry.PoiTypeRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeSerializerRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import com.voxelutopia.ultramarine.data.registry.SoundRegistry;
import com.voxelutopia.ultramarine.data.registry.VillagerProfessionRegistry;
import com.voxelutopia.ultramarine.event.CommonEventHandler;
import com.voxelutopia.ultramarine.network.WoodworkingRecipesS2CPayload;
import com.voxelutopia.ultramarine.world.worldgen.ModBiomeModifiers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ultramarine implements ModInitializer {
   public static final String MOD_ID = "ultramarine";
   public static final Logger LOGGER = LoggerFactory.getLogger("ultramarine");

   public void onInitialize() {
      // Payload types must be registered on both sides before any packet is sent or handled.
      PayloadTypeRegistry.clientboundPlay().register(WoodworkingRecipesS2CPayload.TYPE, WoodworkingRecipesS2CPayload.STREAM_CODEC);
      ModBiomeModifiers.register();
      SoundRegistry.registerModSounds();
      ModFoods.registerModFoods();
      BlockRegistry.registerModBlocks();
      ItemRegistry.registerModItems();
      BlockEntityRegistry.registerModBlockEntities();
      EntityTypeRegistry.registerModEntities();
      VillagerProfessionRegistry.registerModVillagerProfession();
      PoiTypeRegistry.registerModPOI();
      MenuTypeRegistry.registerModMenus();
      RecipeTypeRegistry.registerModRecipeTypes();
      RecipeSerializerRegistry.registerModRecipeSerializers();
      CreativeTabRegistry.registerModGroups();
      BlockApiLookupRegistry.init();
      CommonEventHandler.init();
      ReplaceToSingleItemLootModifier.register();
   }
}
