# -*- coding: utf-8 -*-
"""Inspect the 53 missing advancement files for #tag usages and format quirks."""
import json
import subprocess
from pathlib import Path

REPO = str(Path(__file__).resolve().parents[1])
MISSING = [
    "blue_carved_carpet.json",
    "building_blocks/red_floor_tile_from_crafting.json",
    "building_blocks/red_floor_tile_from_stonecutting.json",
    "building_blocks/red_floor_tile_slab_from_crafting.json",
    "building_blocks/red_floor_tile_slab_from_stonecutting.json",
    "building_blocks/red_floor_tile_stairs_from_crafting.json",
    "building_blocks/red_floor_tile_stairs_from_stonecutting.json",
    "combat/blue_and_white_porcelain_sword.json",
    "decorations/acacia_rafter.json", "decorations/acacia_rafter_end.json",
    "decorations/birch_rafter.json", "decorations/birch_rafter_end.json",
    "decorations/candle_from_grease.json",
    "decorations/cherry_rafter.json", "decorations/cherry_rafter_end.json",
    "decorations/crimson_rafter.json", "decorations/crimson_rafter_end.json",
    "decorations/jungle_rafter.json", "decorations/jungle_rafter_end.json",
    "decorations/lotus_leaves_bowl.json",
    "decorations/mangrove_rafter.json", "decorations/mangrove_rafter_end.json",
    "decorations/oak_rafter.json", "decorations/oak_rafter_end.json",
    "decorations/red_floor_tile_wall_from_crafting.json",
    "decorations/red_floor_tile_wall_from_stonecutting.json",
    "decorations/spruce_rafter.json", "decorations/spruce_rafter_end.json",
    "decorations/warped_rafter.json", "decorations/warped_rafter_end.json",
    "decorations/yellow_green_glazed_top_layer_eaves.json",
    "glazed_central_wall_pattern.json",
    "grass_lucky_motif_1.json", "grass_lucky_motif_2.json", "grass_lucky_motif_3.json",
    "loong_tuanhua.json",
    "misc/blue_and_white_porcelain_piece_grinding.json",
    "misc/brick_from_fired_brick.json",
    "misc/bronze_block.json", "misc/bronze_ingot.json",
    "misc/carved_wool_template.json",
    "misc/cobalt_dust_compat.json",
    "misc/cooked_meat_from_smelting.json", "misc/cooked_meat_from_smoking.json",
    "misc/jade.json", "misc/jade_block.json",
    "misc/porcelain_parts_from_blue_and_white_porcelain_piece.json",
    "misc/porcelain_parts_from_porcelain_piece.json",
    "misc/porcelain_piece_grinding.json",
    "red_carved_carpet.json",
    "tools/blue_and_white_porcelain_axe.json",
    "tools/blue_and_white_porcelain_pickaxe.json",
    "tools/blue_and_white_porcelain_shovel.json",
]
assert len(MISSING) == 53, len(MISSING)

tag_files = []
for rel in MISSING:
    out = subprocess.run(
        ["git", "-C", REPO, "show", "fabric/1.21.2:src/main/resources/data/ultramarine/advancement/recipes/" + rel],
        capture_output=True, text=True, encoding="utf-8", errors="replace")
    text = out.stdout
    data = json.loads(text)
    def walk(node, path=""):
        if isinstance(node, dict):
            for k, v in node.items():
                if k == "items" and isinstance(v, str) and v.startswith("#"):
                    tag_files.append((rel, path + "/items", v))
                elif k == "items" and isinstance(v, list):
                    for i, item in enumerate(v):
                        if isinstance(item, str) and item.startswith("#"):
                            tag_files.append((rel, f"{path}/items[{i}]", item))
                else:
                    walk(v, path + "/" + k)
        elif isinstance(node, list):
            for i, item in enumerate(node):
                walk(item, f"{path}[{i}]")
    walk(data)
    # detect load conditions and telemetry
    conds = "fabric:load_conditions" in data
    tele = "sends_telemetry_event" in data
    print(f"{rel}: conditions={conds} telemetry={tele}")

print()
print("=== #tag usages ===")
for rel, path, val in tag_files:
    print(f"{rel} @ {path} = {val}")
