package com.voxelutopia.ultramarine;

import com.voxelutopia.ultramarine.client.event.ModClientRenderEventHandler;
import net.fabricmc.api.ClientModInitializer;

public class UltramarineClient implements ClientModInitializer {
   public void onInitializeClient() {
      ModClientRenderEventHandler.onClientSetup();
   }
}
