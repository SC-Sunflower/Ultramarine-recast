package com.voxelutopia.ultramarine.client.screen;

import com.voxelutopia.ultramarine.world.block.menu.ChiselTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ChiselTableScreen extends AbstractContainerScreen<ChiselTableMenu> {
   private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("ultramarine", "textures/gui/sprites/container/chisel_table.png");

   public ChiselTableScreen(ChiselTableMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, pPlayerInventory, pTitle);
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.render(guiGraphics, mouseX, mouseY, partialTicks);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
                        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
   }
}
