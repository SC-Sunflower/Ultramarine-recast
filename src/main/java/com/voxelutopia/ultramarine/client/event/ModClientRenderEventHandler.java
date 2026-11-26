package com.voxelutopia.ultramarine.client.event;

import com.voxelutopia.ultramarine.client.render.SeatEntityRenderer;
import com.voxelutopia.ultramarine.client.screen.BrickKilnScreen;
import com.voxelutopia.ultramarine.client.screen.ChiselTableScreen;
import com.voxelutopia.ultramarine.client.screen.ContainerDecorativeBlockScreen;
import com.voxelutopia.ultramarine.client.screen.WoodworkingWorkbenchScreen;
import com.voxelutopia.ultramarine.data.registry.BlockRegistry;
import com.voxelutopia.ultramarine.data.registry.EntityTypeRegistry;
import com.voxelutopia.ultramarine.data.registry.MenuTypeRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.WanderingTraderRenderer;
import net.minecraft.world.level.block.Block;

public class ModClientRenderEventHandler {
   public static void onClientSetup() {
      registerMenuScreens();
      registerEntityRenderers();
   }

   private static void registerMenuScreens() {
      MenuScreens.register(MenuTypeRegistry.CONTAINER_DECORATIVE_BLOCK_MENU_GENERIC_9X1, ContainerDecorativeBlockScreen::new);
      MenuScreens.register(MenuTypeRegistry.CONTAINER_DECORATIVE_BLOCK_MENU_GENERIC_9X3, ContainerDecorativeBlockScreen::new);
      MenuScreens.register(MenuTypeRegistry.CONTAINER_DECORATIVE_BLOCK_MENU_GENERIC_9X6, ContainerDecorativeBlockScreen::new);
      MenuScreens.register(MenuTypeRegistry.CONTAINER_DECORATIVE_BLOCK_MENU_FOOD_9X3, ContainerDecorativeBlockScreen::new);
      MenuScreens.register(MenuTypeRegistry.CONTAINER_DECORATIVE_BLOCK_MENU_FOOD_9X6, ContainerDecorativeBlockScreen::new);
      MenuScreens.register(MenuTypeRegistry.BRICK_KILN, BrickKilnScreen::new);
      MenuScreens.register(MenuTypeRegistry.CHISEL_TABLE, ChiselTableScreen::new);
      MenuScreens.register(MenuTypeRegistry.WOODWORKING_WORKBENCH, WoodworkingWorkbenchScreen::new);
   }

   private static void registerEntityRenderers() {
      EntityRendererRegistry.register(EntityTypeRegistry.SEAT, SeatEntityRenderer::new);
      EntityRendererRegistry.register(EntityTypeRegistry.TRAVELLING_MERCHANT, WanderingTraderRenderer::new);
   }
}
