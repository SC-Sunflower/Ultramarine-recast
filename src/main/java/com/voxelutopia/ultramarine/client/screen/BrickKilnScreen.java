package com.voxelutopia.ultramarine.client.screen;

import com.voxelutopia.ultramarine.world.block.menu.BrickKilnMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BrickKilnScreen extends AbstractContainerScreen<BrickKilnMenu> {
   private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("ultramarine", "container/brick_kiln");

   public BrickKilnScreen(BrickKilnMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, pPlayerInventory, pTitle);
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
      super.render(guiGraphics, mouseX, mouseY, partialTicks);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
      guiGraphics.blitSprite(RenderType::guiTextured, BACKGROUND, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
      if (((BrickKilnMenu)this.menu).isLit()) {
         int k = ((BrickKilnMenu)this.menu).getLitProgress();
         guiGraphics.blit(RenderType::guiTextured, BACKGROUND, this.leftPos + 56, this.topPos + 36 + 12 - k, 176, 12 - k, 14, k + 1, 256, 256);
      }

      int l = ((BrickKilnMenu)this.menu).getBurnProgress();
      guiGraphics.blit(RenderType::guiTextured, BACKGROUND, this.leftPos + 79, this.topPos + 34, 176, 14, l + 1, 16, 256, 256);
   }
}
