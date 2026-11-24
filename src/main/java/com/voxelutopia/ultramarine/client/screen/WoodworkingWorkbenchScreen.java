package com.voxelutopia.ultramarine.client.screen;

import com.voxelutopia.ultramarine.world.block.menu.WoodworkingWorkbenchMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class WoodworkingWorkbenchScreen extends AbstractContainerScreen<WoodworkingWorkbenchMenu> {
   private static final Identifier BG_LOCATION = Identifier.withDefaultNamespace("textures/gui/container/stonecutter.png");
   private static final int SCROLLER_WIDTH = 12;
   private static final int SCROLLER_HEIGHT = 15;
   private static final int RECIPES_COLUMNS = 4;
   private static final int RECIPES_ROWS = 3;
   private static final int RECIPES_IMAGE_SIZE_WIDTH = 16;
   private static final int RECIPES_IMAGE_SIZE_HEIGHT = 18;
   private static final int SCROLLER_FULL_HEIGHT = 54;
   private static final int RECIPES_X = 52;
   private static final int RECIPES_Y = 14;
   private float scrollOffs;
   private boolean scrolling;
   private int startIndex;
   private boolean displayRecipes;

   public WoodworkingWorkbenchScreen(WoodworkingWorkbenchMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, pPlayerInventory, pTitle);
      pMenu.registerUpdateListener(this::containerChanged);
      this.titleLabelY--;
   }

   public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
      this.renderTooltip(guiGraphics, pMouseX, pMouseY);
   }

   protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pX, int pY) {
                        int i = this.leftPos;
      int j = this.topPos;
      guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BG_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
      int k = (int)(41.0F * this.scrollOffs);
      guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BG_LOCATION, i + 119, j + 15 + k, 176 + (this.isScrollBarActive() ? 0 : 12), 0, 12, 15, 256, 256);
      int l = this.leftPos + 52;
      int i1 = this.topPos + 14;
      int j1 = this.startIndex + 12;
      this.renderButtons(guiGraphics, pX, pY, l, i1, j1);
      this.renderRecipes(guiGraphics, l, i1, j1);
   }

   protected void renderTooltip(GuiGraphics guiGraphics, int pX, int pY) {
      super.renderTooltip(guiGraphics, pX, pY);
      if (this.displayRecipes) {
         int i = this.leftPos + 52;
         int j = this.topPos + 14;
         int k = this.startIndex + 12;
         WoodworkingWorkbenchMenu workbenchMenu = (WoodworkingWorkbenchMenu)this.menu;

         for (int l = this.startIndex; l < k && l < workbenchMenu.getNumRecipes(); l++) {
            int i1 = l - this.startIndex;
            int j1 = i + i1 % 4 * 16;
            int k1 = j + i1 / 4 * 18 + 2;
            if (pX >= j1 && pX < j1 + 16 && pY >= k1 && pY < k1 + 18) {
               guiGraphics.setTooltipForNextFrame(this.font, workbenchMenu.getRecipeResult(l), pX, pY);
            }
         }
      }
   }

   private void renderButtons(GuiGraphics guiGraphics, int pMouseX, int pMouseY, int pX, int pY, int pLastVisibleElementIndex) {
      for (int i = this.startIndex; i < pLastVisibleElementIndex && i < ((WoodworkingWorkbenchMenu)this.menu).getNumRecipes(); i++) {
         int j = i - this.startIndex;
         int k = pX + j % 4 * 16;
         int l = j / 4;
         int i1 = pY + l * 18 + 2;
         int j1 = this.imageHeight;
         if (i == ((WoodworkingWorkbenchMenu)this.menu).getSelectedRecipeIndex()) {
            j1 += 18;
         } else if (pMouseX >= k && pMouseY >= i1 && pMouseX < k + 16 && pMouseY < i1 + 18) {
            j1 += 36;
         }

         guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BG_LOCATION, k, i1 - 1, 0, j1, 16, 18, 256, 256);
      }
   }

   private void renderRecipes(GuiGraphics guiGraphics, int pLeft, int pTop, int pRecipeIndexOffsetMax) {
      WoodworkingWorkbenchMenu workbenchMenu = (WoodworkingWorkbenchMenu)this.menu;

      for (int i = this.startIndex; i < pRecipeIndexOffsetMax && i < workbenchMenu.getNumRecipes(); i++) {
         int j = i - this.startIndex;
         int k = pLeft + j % 4 * 16;
         int l = j / 4;
         int i1 = pTop + l * 18 + 2;
         guiGraphics.renderItem(workbenchMenu.getRecipeResult(i), k, i1);
      }
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean isHovered) {
      double pMouseX = event.x();
      double pMouseY = event.y();
      this.scrolling = false;
      if (this.displayRecipes) {
         int i = this.leftPos + 52;
         int j = this.topPos + 14;
         int k = this.startIndex + 12;

         for (int l = this.startIndex; l < k; l++) {
            int i1 = l - this.startIndex;
            double d0 = pMouseX - (i + i1 % 4 * 16);
            double d1 = pMouseY - (j + i1 / 4 * 18);
            if (d0 >= 0.0 && d1 >= 0.0 && d0 < 16.0 && d1 < 18.0 && ((WoodworkingWorkbenchMenu)this.menu).clickMenuButton(this.minecraft.player, l)) {
               Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
               this.minecraft.gameMode.handleInventoryButtonClick(((WoodworkingWorkbenchMenu)this.menu).containerId, l);
               return true;
            }
         }

         i = this.leftPos + 119;
         j = this.topPos + 9;
         if (pMouseX >= i && pMouseX < i + 12 && pMouseY >= j && pMouseY < j + 54) {
            this.scrolling = true;
         }
      }

      return super.mouseClicked(event, isHovered);
   }

   public boolean mouseDragged(MouseButtonEvent event, double pDragX, double pDragY) {
      double pMouseY = event.y();
      if (this.scrolling && this.isScrollBarActive()) {
         int i = this.topPos + 14;
         int j = i + 54;
         this.scrollOffs = ((float)pMouseY - i - 7.5F) / (j - i - 15.0F);
         this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
         this.startIndex = (int)(this.scrollOffs * this.getOffscreenRows() + 0.5) * 4;
         return true;
      } else {
         return super.mouseDragged(event, pDragX, pDragY);
      }
   }

   public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
      if (this.isScrollBarActive()) {
         int i = this.getOffscreenRows();
         float f = (float)pDelta / i;
         this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
         this.startIndex = (int)(this.scrollOffs * i + 0.5) * 4;
      }

      return true;
   }

   private boolean isScrollBarActive() {
      return this.displayRecipes && ((WoodworkingWorkbenchMenu)this.menu).getNumRecipes() > 12;
   }

   protected int getOffscreenRows() {
      return (((WoodworkingWorkbenchMenu)this.menu).getNumRecipes() + 4 - 1) / 4 - 3;
   }

   private void containerChanged() {
      this.displayRecipes = ((WoodworkingWorkbenchMenu)this.menu).hasInputItem();
      if (!this.displayRecipes) {
         this.scrollOffs = 0.0F;
         this.startIndex = 0;
      }
   }
}
