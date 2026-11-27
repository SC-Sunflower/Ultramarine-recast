package com.voxelutopia.ultramarine.compat.rei;

import java.util.List;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

/**
 * REI display for the woodworking / chisel table / composite smelting recipes.
 * REI 12 displays are created client-side from the synced recipe manager, so no
 * serializer or server-side filler is needed.
 */
public final class UltramarineReiDisplays {
   private UltramarineReiDisplays() {
   }

   public static class Woodworking extends BasicDisplay {
      public Woodworking(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
         super(inputs, outputs);
      }

      @Override
      public CategoryIdentifier<?> getCategoryIdentifier() {
         return UltramarineReiCategories.WOODWORKING;
      }
   }

   public static class ChiselTable extends BasicDisplay {
      public ChiselTable(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
         super(inputs, outputs);
      }

      @Override
      public CategoryIdentifier<?> getCategoryIdentifier() {
         return UltramarineReiCategories.CHISEL_TABLE;
      }
   }

   public static class CompositeSmelting extends BasicDisplay {
      public CompositeSmelting(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
         super(inputs, outputs);
      }

      @Override
      public CategoryIdentifier<?> getCategoryIdentifier() {
         return UltramarineReiCategories.COMPOSITE_SMELTING;
      }
   }
}
