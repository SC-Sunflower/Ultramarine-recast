# -*- coding: utf-8 -*-
"""Apply mechanical 26.1 API renames in the port."""
import re
from pathlib import Path

SRC = Path(r"C:\Users\34324\Desktop\um-261\src\main\java\com\voxelutopia\ultramarine")


def patch(path, fn):
    p = SRC / path
    text = p.read_text(encoding="utf-8", errors="replace")
    new = fn(text)
    if new != text:
        p.write_text(new, encoding="utf-8")
        print(f"patched {path}")


# 1. ModClientRenderEventHandler: drop BlockRenderLayerMap (26.1 auto-assigns render layers)
def drop_brlm(text):
    text = text.replace("import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;\n", "")
    return "\n".join(line for line in text.splitlines() if "BlockRenderLayerMap.putBlock(" not in line) + "\n"

patch("client/event/ModClientRenderEventHandler.java", drop_brlm)


# 2. PoiHelper
patch("data/registry/PoiTypeRegistry.java",
      lambda t: t.replace("PointOfInterestHelper", "PoiHelper")
                 .replace("import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;",
                          "import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;"))

# 3. FabricCreativeModeTab
patch("data/registry/CreativeTabRegistry.java",
      lambda t: t.replace("import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;",
                          "import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;")
                 .replace("FabricItemGroup.builder()", "FabricCreativeModeTab.builder()"))

# 4. VillagerTrades package move
for f in ("event/EntityEvents.java", "event/CommonEventHandler.java"):
    patch(f, lambda t: t.replace("net.minecraft.world.entity.npc.villager.VillagerTrades",
                                 "net.minecraft.world.item.trading.VillagerTrades"))


# 5. loot v3 + 4-arg Modify
def loot_v3(text):
    text = text.replace("import net.fabricmc.fabric.api.loot.v2.LootTableEvents;",
                        "import net.fabricmc.fabric.api.loot.v3.LootTableEvents;")
    text = text.replace("LootTableEvents.MODIFY.register((key, tableBuilder, source) ->",
                        "LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) ->")
    return text

patch("data/loot/ReplaceToSingleItemLootModifier.java", loot_v3)

print("done")
