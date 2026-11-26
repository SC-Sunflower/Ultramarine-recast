package com.voxelutopia.ultramarine.data.registry;

import com.voxelutopia.ultramarine.util.IMenuType;
import com.voxelutopia.ultramarine.world.block.menu.BrickKilnMenu;
import com.voxelutopia.ultramarine.world.block.menu.ChiselTableMenu;
import com.voxelutopia.ultramarine.world.block.menu.ContainerDecorativeBlockMenu;
import com.voxelutopia.ultramarine.world.block.menu.WoodworkingWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;

public class MenuTypeRegistry {
   public static MenuType<ContainerDecorativeBlockMenu> CONTAINER_DECORATIVE_BLOCK_MENU_GENERIC_9X1;
   public static MenuType<ContainerDecorativeBlockMenu> CONTAINER_DECORATIVE_BLOCK_MENU_GENERIC_9X3;
   public static MenuType<ContainerDecorativeBlockMenu> CONTAINER_DECORATIVE_BLOCK_MENU_GENERIC_9X6;
   public static MenuType<ContainerDecorativeBlockMenu> CONTAINER_DECORATIVE_BLOCK_MENU_FOOD_9X3;
   public static MenuType<ContainerDecorativeBlockMenu> CONTAINER_DECORATIVE_BLOCK_MENU_FOOD_9X6;
   public static MenuType<WoodworkingWorkbenchMenu> WOODWORKING_WORKBENCH;
   public static MenuType<BrickKilnMenu> BRICK_KILN;
   public static MenuType<ChiselTableMenu> CHISEL_TABLE;

   public static void registerModMenus() {
      CONTAINER_DECORATIVE_BLOCK_MENU_GENERIC_9X1 = (MenuType<ContainerDecorativeBlockMenu>)Registry.register(
         BuiltInRegistries.MENU,
         Identifier.fromNamespaceAndPath("ultramarine", "container_decorative_block_menu_generic_9x1"),
         new MenuType<>(ContainerDecorativeBlockMenu::genericOneRow, FeatureFlagSet.of())
      );
      CONTAINER_DECORATIVE_BLOCK_MENU_GENERIC_9X3 = (MenuType<ContainerDecorativeBlockMenu>)Registry.register(
         BuiltInRegistries.MENU,
         Identifier.fromNamespaceAndPath("ultramarine", "container_decorative_block_menu_generic_9x3"),
         new MenuType<>(ContainerDecorativeBlockMenu::genericThreeRows, FeatureFlagSet.of())
      );
      CONTAINER_DECORATIVE_BLOCK_MENU_GENERIC_9X6 = (MenuType<ContainerDecorativeBlockMenu>)Registry.register(
         BuiltInRegistries.MENU,
         Identifier.fromNamespaceAndPath("ultramarine", "container_decorative_block_menu_generic_9x6"),
         new MenuType<>(ContainerDecorativeBlockMenu::genericSixRows, FeatureFlagSet.of())
      );
      CONTAINER_DECORATIVE_BLOCK_MENU_FOOD_9X3 = (MenuType<ContainerDecorativeBlockMenu>)Registry.register(
         BuiltInRegistries.MENU,
         Identifier.fromNamespaceAndPath("ultramarine", "container_decorative_block_menu_food_9x3"),
         new MenuType<>(ContainerDecorativeBlockMenu::foodThreeRows, FeatureFlagSet.of())
      );
      CONTAINER_DECORATIVE_BLOCK_MENU_FOOD_9X6 = (MenuType<ContainerDecorativeBlockMenu>)Registry.register(
         BuiltInRegistries.MENU,
         Identifier.fromNamespaceAndPath("ultramarine", "container_decorative_block_menu_food_9x6"),
         new MenuType<>(ContainerDecorativeBlockMenu::foodSixRows, FeatureFlagSet.of())
      );
      WOODWORKING_WORKBENCH = (MenuType<WoodworkingWorkbenchMenu>)Registry.register(
         BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath("ultramarine", "woodworking_workbench"), new MenuType<>(WoodworkingWorkbenchMenu::new, FeatureFlagSet.of())
      );
      BRICK_KILN = (MenuType<BrickKilnMenu>)Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath("ultramarine", "brick_kiln"), IMenuType.create((windowId, inv, data) -> {
         BlockPos pos = data != null ? data.readBlockPos() : BlockPos.ZERO;
         return new BrickKilnMenu(windowId, pos, inv);
      }));
      CHISEL_TABLE = (MenuType<ChiselTableMenu>)Registry.register(
         BuiltInRegistries.MENU,
         Identifier.fromNamespaceAndPath("ultramarine", "chisel_table"),
         IMenuType.create((windowId, inv, data) -> new ChiselTableMenu(windowId, inv, ContainerLevelAccess.NULL))
      );
   }
}
