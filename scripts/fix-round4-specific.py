# -*- coding: utf-8 -*-
"""Round 4 branch-specific fixes:

  A. ChiselTableMenu.onTake: explicit write-back instead of relying on handler
     reference semantics (mirror of the BrickKiln grow bug).
  B. 1.20.x SeatDecorativeBlock: consistent client/server result + discard on
     failed mount (sidedSuccess form).
  C. main (1.20.1): safe Porting Lib fuel lookup (no unboxing NPE).
  D. main (1.20.1): deep-copy every MerchantOffer stack in copyOffer.
  E. VillagerTradingsRegister: merge into the live tables instead of replacing
     them with the static snapshot.
"""
import os
import re

BASE = r"C:\Users\34324\Desktop"
WTS = [os.path.join(BASE, d) for d in os.listdir(BASE) if re.match(r"^um-[\w.-]+-wt$", d)]
WTS.append(r"C:\Users\34324\Desktop\Ultramarine-recast-fabric")

SEAT_SIDED_OLD = (
    "         boolean ridingSuccess = false;\n"
    "         if (!pLevel.isClientSide()) {\n"
    "            SeatEntity seat = new SeatEntity(pLevel, Vec3.atCenterOf(pPos).add(this.seatOffset));\n"
    "            pLevel.addFreshEntity(seat);\n"
    "            ridingSuccess = pPlayer.startRiding(seat);\n"
    "         }\n"
    "\n"
    "         return ridingSuccess ? InteractionResult.sidedSuccess(pLevel.isClientSide()) : InteractionResult.PASS;"
)
SEAT_SIDED_NEW = (
    "         if (pLevel.isClientSide()) {\n"
    "            // Client prediction: the seat entity is only created server-side, so\n"
    "            // report success here to keep both sides' interaction results consistent.\n"
    "            return InteractionResult.sidedSuccess(true);\n"
    "         }\n"
    "         SeatEntity seat = new SeatEntity(pLevel, Vec3.atCenterOf(pPos).add(this.seatOffset));\n"
    "         pLevel.addFreshEntity(seat);\n"
    "         if (!pPlayer.startRiding(seat)) {\n"
    "            // Mounting failed; remove the freshly created entity so it does not linger.\n"
    "            seat.discard();\n"
    "            return InteractionResult.PASS;\n"
    "         }\n"
    "         return InteractionResult.sidedSuccess(false);"
)


def fix_on_take(s):
    pat = re.compile(
        r'(\s*)protected void onTake\(Player player, ItemStack itemStack, SlotItemHandler slot\) \{\n'
        r'\s*for \(int i = 0; i < 6; i\+\+\) \{\n'
        r'\s*ItemStack item = this\.crafting\.getStackInSlot\(i\);\n'
        r'\s*this\.crafting\.setStackInSlot\(i, item\);\n'
        r'\s*if \(i != 1\) \{\n'
        r'\s*item\.shrink\(1\);\n'
        r'\s*\}\n'
        r'\s*\}\n'
        r'\n\s*this\.slotsChanged\(slot\);\n'
        r'\s*\}'
    )
    def repl(m):
        ind = m.group(1)
        i1 = ind + "   "
        i2 = ind + "      "
        i3 = ind + "         "
        return "\n".join([
            ind + "protected void onTake(Player player, ItemStack itemStack, SlotItemHandler slot) {",
            i1 + "for (int i = 0; i < 6; i++) {",
            i2 + "ItemStack item = this.crafting.getStackInSlot(i);",
            i2 + "if (item.isEmpty()) {",
            i3 + "continue;",
            i2 + "}",
            i2 + "// Consume one from every ingredient except the template slot (1) and",
            i2 + "// write the reduced stack back explicitly: never rely on the handler",
            i2 + "// returning a live reference.",
            i2 + "if (i != 1) {",
            i3 + "this.crafting.setStackInSlot(i, item.copyWithCount(item.getCount() - 1));",
            i2 + "} else {",
            i3 + "this.crafting.setStackInSlot(i, item);",
            i2 + "}",
            i1 + "}",
            "",
            i1 + "this.slotsChanged(slot);",
            ind + "}",
        ])
    return pat.subn(repl, s)


VILLAGER_CLASS = '''package com.voxelutopia.ultramarine.data.registry;

import com.voxelutopia.ultramarine.event.EntityEvents;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

public class VillagerTradingsRegister {
   // Trades added by this mod, kept separate from the vanilla tables so the live
   // tables can be merged instead of replaced (other mods may register trades too).
   private static final List<ItemListing> WANDERER_RARE_TRADES = new ArrayList<>();

   public static void loadTrades() {
      ServerLifecycleEvents.SERVER_STARTING.register((ServerStarting)server -> {
         postWandererEvent();
         postVillagerEvents();
      });
   }

   // Ported from Ultramarine 0.5.1 (Forge WandererTradesEvent): rare trades for the vanilla
   // wandering trader. Must be called before the server starts (from the mod initializer).
   public static void registerWandererTrades() {
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 6), new ItemStack(ItemRegistry.INCENSE, 1), 6, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 6), new ItemStack(ItemRegistry.SILK, 1), 8, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 10), new ItemStack(ItemRegistry.JADE, 1), 2, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 12), new ItemStack(ItemRegistry.PAINTING_SCROLL, 1), 1, 30, 0.05F));
      WANDERER_RARE_TRADES.add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 20), new ItemStack(ItemRegistry.BLUE_AND_WHITE_PORCELAIN_VASE, 1), 1, 30, 0.05F));
   }

   private static void postWandererEvent() {
      // Append our rare trades to whatever is currently in the live table instead of
      // overwriting trades other mods may have registered.
      List<ItemListing> rare = new ArrayList<>(Arrays.asList(safeTrades(VillagerTrades.WANDERING_TRADER_TRADES.get(2))));
      rare.addAll(WANDERER_RARE_TRADES);
      VillagerTrades.WANDERING_TRADER_TRADES.put(2, rare.toArray(new ItemListing[0]));
   }

   private static void postVillagerEvents() {
      for (VillagerProfession prof : BuiltInRegistries.VILLAGER_PROFESSION) {
         // Start from the live table so trades registered by other mods survive this merge.
         Int2ObjectMap<ItemListing[]> current = VillagerTrades.TRADES.get(prof);
         Int2ObjectMap<List<ItemListing>> mutableTrades = new Int2ObjectOpenHashMap<>();
         for (int i = 1; i < 6; i++) {
            mutableTrades.put(i, NonNullList.create());
         }
         if (current != null) {
            for (Int2ObjectMap.Entry<ItemListing[]> entry : current.int2ObjectEntrySet()) {
               mutableTrades.computeIfAbsent(entry.getIntKey(), key -> NonNullList.create())
                  .addAll(Arrays.asList(safeTrades(entry.getValue())));
            }
         }
         ((EntityEvents.VillagerTrade)EntityEvents.VILLAGER_TRADE.invoker()).modifyVillagerTrade(mutableTrades, prof);
         Int2ObjectMap<ItemListing[]> newTrades = new Int2ObjectOpenHashMap<>();
         for (Int2ObjectMap.Entry<List<ItemListing>> entry : mutableTrades.int2ObjectEntrySet()) {
            newTrades.put(entry.getIntKey(), entry.getValue().toArray(new ItemListing[0]));
         }
         VillagerTrades.TRADES.put(prof, newTrades);
      }
   }

   private static ItemListing[] safeTrades(ItemListing[] arr) {
      return arr == null ? new ItemListing[0] : arr;
   }
}
'''

for wt in WTS:
    name = os.path.basename(wt)
    results = []

    # A. onTake
    p = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                     "world", "block", "menu", "ChiselTableMenu.java")
    with open(p, encoding="utf-8-sig") as f:
        s = f.read()
    s2, n = fix_on_take(s)
    if n:
        with open(p, "w", encoding="utf-8", newline="\n") as f:
            f.write(s2)
        results.append("onTake")

    # B. seat (1.20.x sidedSuccess form)
    p = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                     "world", "block", "SeatDecorativeBlock.java")
    with open(p, encoding="utf-8-sig") as f:
        s = f.read()
    if SEAT_SIDED_OLD in s:
        s = s.replace(SEAT_SIDED_OLD, SEAT_SIDED_NEW)
        with open(p, "w", encoding="utf-8", newline="\n") as f:
            f.write(s)
        results.append("seat")

    # C/D. main-only fixes
    if name == "um-main-wt":
        p = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                         "world", "block", "entity", "BrickKilnBlockEntity.java")
        with open(p, encoding="utf-8-sig") as f:
            s = f.read()
        old_bt = (
            "   private int getBurnTime(ItemStack stack) {\n"
            "      return stack.isEmpty() ? 0 : (Integer)FuelRegistry.INSTANCE.get(stack.getItem());\n"
            "   }"
        )
        new_bt = (
            "   private int getBurnTime(ItemStack stack) {\n"
            "      if (stack.isEmpty()) {\n"
            "         return 0;\n"
            "      }\n"
            "      Integer burnTime = FuelRegistry.INSTANCE.get(stack.getItem());\n"
            "      // A non-fuel item (e.g. inserted by a hopper or restored from an old save)\n"
            "      // must not crash the block entity; treat it as zero burn time.\n"
            "      return burnTime == null ? 0 : burnTime;\n"
            "   }"
        )
        if old_bt in s:
            s = s.replace(old_bt, new_bt)
            results.append("fuel-getBurnTime")
        old_lit = "pBlockEntity.litTime = (Integer)FuelRegistry.INSTANCE.get(fuelItem.getItem());"
        if old_lit in s:
            s = s.replace(old_lit, "pBlockEntity.litTime = pBlockEntity.getBurnTime(fuelItem);")
            results.append("fuel-tick")
        with open(p, "w", encoding="utf-8", newline="\n") as f:
            f.write(s)

        p = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                         "world", "entity", "TravellingMerchant.java")
        with open(p, encoding="utf-8-sig") as f:
            s = f.read()
        changed = False
        if "offer.getCostB().copy()," not in s:
            s = s.replace("         offer.getCostB(),", "         offer.getCostB().copy(),")
            changed = True
        if "offer.getResult().copy()," not in s:
            s = s.replace("         offer.getResult(),", "         offer.getResult().copy(),")
            changed = True
        if changed:
            with open(p, "w", encoding="utf-8", newline="\n") as f:
                f.write(s)
            results.append("copyOffer")

    # E. villager trades register rewrite
    p = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                     "data", "registry", "VillagerTradingsRegister.java")
    if os.path.exists(p):
        with open(p, "w", encoding="utf-8", newline="\n") as f:
            f.write(VILLAGER_CLASS)
        results.append("trades-merge")

    print(name, "->", ", ".join(results) if results else "no change")
