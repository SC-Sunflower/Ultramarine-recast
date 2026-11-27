package com.voxelutopia.ultramarine;

import com.voxelutopia.ultramarine.client.event.ModClientRenderEventHandler;
import com.voxelutopia.ultramarine.network.WoodworkingRecipesS2CPayload;
import com.voxelutopia.ultramarine.world.block.menu.WoodworkingWorkbenchMenu;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class UltramarineClient implements ClientModInitializer {
   public void onInitializeClient() {
      ModClientRenderEventHandler.onClientSetup();

      // The payload type itself is registered in the common initializer so dedicated
      // servers can encode it as well; only the receiver is client-specific.
      ClientPlayNetworking.registerGlobalReceiver(WoodworkingRecipesS2CPayload.TYPE, (payload, context) -> {
         context.client().execute(() -> {
            if (context.client().player != null
                  && context.client().player.containerMenu instanceof WoodworkingWorkbenchMenu menu) {
               menu.receiveRecipes(payload.results(), payload.recipeIds());
            }
         });
      });
   }
}
