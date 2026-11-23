package com.voxelutopia.ultramarine.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator.Pack;

public class ModDataGenerator implements DataGeneratorEntrypoint {
   public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
      Pack pack = fabricDataGenerator.createPack();
      pack.addProvider(ModDatapackBuiltinEntriesProvider::new);
   }

   public String getEffectiveModId() {
      return "ultramarine";
   }
}
