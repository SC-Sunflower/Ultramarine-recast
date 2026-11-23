package com.voxelutopia.ultramarine.world.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.SmithingTemplateItem;

public class BlueAndWhitePorcelainUpgradeSmithingTemplate extends SmithingTemplateItem {
   private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
   private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;
   private static final Component BLUE_AND_WHITE_PORCELAIN_UPGRADE_APPLIES_TO = Component.translatable(
         Util.makeDescriptionId("item", Identifier.fromNamespaceAndPath("ultramarine", "smithing_template.blue_and_white_porcelain_upgrade.applies_to"))
      )
      .withStyle(DESCRIPTION_FORMAT);
   private static final Component BLUE_AND_WHITE_PORCELAIN_UPGRADE_INGREDIENT = Component.translatable(
         Util.makeDescriptionId("item", Identifier.fromNamespaceAndPath("ultramarine", "smithing_template.blue_and_white_porcelain_upgrade.ingredients"))
      )
      .withStyle(DESCRIPTION_FORMAT);
   private static final Component BLUE_AND_WHITE_PORCELAIN_UPGRADE = Component.translatable(
         Util.makeDescriptionId("upgrade", Identifier.fromNamespaceAndPath("ultramarine", "blue_and_white_porcelain_upgrade"))
      )
      .withStyle(TITLE_FORMAT);
   private static final Component BLUE_AND_WHITE_PORCELAIN_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable(
      Util.makeDescriptionId("item", Identifier.fromNamespaceAndPath("ultramarine", "smithing_template.blue_and_white_porcelain_upgrade.base_slot_description"))
   );
   private static final Component BLUE_AND_WHITE_PORCELAIN_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(
      Util.makeDescriptionId("item", Identifier.fromNamespaceAndPath("ultramarine", "smithing_template.blue_and_white_porcelain_upgrade.additions_slot_description"))
   );
   private static final Identifier EMPTY_SLOT_PIECE = Identifier.fromNamespaceAndPath("ultramarine", "item/empty_slot_piece");
   private static final Identifier EMPTY_SLOT_AXE = Identifier.withDefaultNamespace("item/empty_slot_axe");
   private static final Identifier EMPTY_SLOT_SWORD = Identifier.withDefaultNamespace("item/empty_slot_sword");
   private static final Identifier EMPTY_SLOT_SHOVEL = Identifier.withDefaultNamespace("item/empty_slot_shovel");
   private static final Identifier EMPTY_SLOT_PICKAXE = Identifier.withDefaultNamespace("item/empty_slot_pickaxe");

   public BlueAndWhitePorcelainUpgradeSmithingTemplate() {
      super(
         BLUE_AND_WHITE_PORCELAIN_UPGRADE_APPLIES_TO,
         BLUE_AND_WHITE_PORCELAIN_UPGRADE_INGREDIENT,
         BLUE_AND_WHITE_PORCELAIN_UPGRADE,
         BLUE_AND_WHITE_PORCELAIN_UPGRADE_BASE_SLOT_DESCRIPTION,
         
         List.of(EMPTY_SLOT_SWORD, EMPTY_SLOT_PICKAXE, EMPTY_SLOT_AXE, EMPTY_SLOT_SHOVEL),
         List.of(EMPTY_SLOT_PIECE),
         new Properties()
      );
   }
}
