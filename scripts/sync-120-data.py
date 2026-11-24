# -*- coding: utf-8 -*-
"""Sync 0.7 loot tables and advancements from fabric/1.21.2 to the 1.20.x worktrees."""
import json
import shutil
import subprocess
from pathlib import Path

REPO = Path(r"C:\Users\34324\Desktop\Ultramarine-recast-fabric")
WT_BASE = Path(r"C:\Users\34324\Desktop\um-sync")

LOOT = [
    "acacia_rafter.json", "acacia_rafter_end.json", "birch_rafter.json", "birch_rafter_end.json",
    "blue_carved_carpet.json", "cherry_rafter.json", "cherry_rafter_end.json",
    "crimson_rafter.json", "crimson_rafter_end.json", "glazed_central_wall_pattern.json",
    "grass_lucky_motif_1.json", "grass_lucky_motif_2.json", "grass_lucky_motif_3.json",
    "jungle_rafter.json", "jungle_rafter_end.json", "loong_tuanhua.json", "lotus_leaves_bowl.json",
    "mangrove_rafter.json", "mangrove_rafter_end.json", "oak_rafter.json", "oak_rafter_end.json",
    "red_carved_carpet.json", "red_floor_tile.json", "red_floor_tile_slab.json",
    "red_floor_tile_stairs.json", "red_floor_tile_wall.json",
    "spruce_rafter.json", "spruce_rafter_end.json", "warped_rafter.json", "warped_rafter_end.json",
    "yellow_green_glazed_top_layer_eaves.json",
]
assert len(LOOT) == 31

ADV = [
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
assert len(ADV) == 53

BRANCHES = {
    "um-1201": {"mc": "1.20.1", "rewrite_tag": True},
    "um-1202": {"mc": "1.20.2", "rewrite_tag": True},
    "um-1203": {"mc": "1.20.3", "rewrite_tag": True},
    "um-1204": {"mc": "1.20.4", "rewrite_tag": True},
    "um-1205": {"mc": "1.20.5", "rewrite_tag": False},
    "um-1206": {"mc": "1.20.6", "rewrite_tag": False},
}


def read_upstream(path):
    out = subprocess.run(
        ["git", "-C", str(REPO), "show", f"fabric/1.21.2:{path}"],
        capture_output=True, text=True, encoding="utf-8", errors="replace")
    assert out.returncode == 0, f"git show failed: {path}"
    return out.stdout


def rewrite_tags(data):
    """1.20.1-1.20.4: items strings -> arrays; {"items": "#tag"} -> {"tag": "ns:path"}; drop load_conditions."""
    if isinstance(data, dict):
        if "fabric:load_conditions" in data:
            del data["fabric:load_conditions"]
        for k, v in list(data.items()):
            if k == "items" and isinstance(v, str):
                if v.startswith("#"):
                    data["tag"] = v[1:]
                    del data["items"]
                else:
                    data["items"] = [v]
                continue
            data[k] = rewrite_tags(v)
        return data
    if isinstance(data, list):
        return [rewrite_tags(x) for x in data]
    return data


def main():
    total_loot = 0
    total_adv = 0
    for wt_name, cfg in BRANCHES.items():
        wt = WT_BASE / wt_name
        data_dir = wt / "src/main/resources/data/ultramarine"
        # loot tables
        for name in LOOT:
            text = read_upstream(f"src/main/resources/data/ultramarine/loot_table/blocks/{name}")
            dest = data_dir / "loot_tables/blocks" / name
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_text(text, encoding="utf-8", newline="\n")
            total_loot += 1
        # advancements
        for rel in ADV:
            text = read_upstream(f"src/main/resources/data/ultramarine/advancement/recipes/{rel}")
            if cfg["rewrite_tag"]:
                data = json.loads(text)
                data = rewrite_tags(data)
                text = json.dumps(data, indent=2, ensure_ascii=False) + "\n"
            dest = data_dir / "advancements/recipes" / rel
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_text(text, encoding="utf-8", newline="\n")
            total_adv += 1
        print(f"{wt_name} ({cfg['mc']}): copied {len(LOOT)} loot + {len(ADV)} adv (rewrite_tag={cfg['rewrite_tag']})")
    print(f"total: {total_loot} loot, {total_adv} adv")


if __name__ == "__main__":
    main()
