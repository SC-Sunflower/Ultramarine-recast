# -*- coding: utf-8 -*-
"""Generate Minecraft 26.1 data-driven villager trade JSON for the mod.

Layout:
  data/ultramarine/villager_trade/<name>.json          single trade
  data/ultramarine/tags/villager_trade/<set>/level_N.json   trade tag
  data/ultramarine/trade_set/<set>/level_N.json         trade set
  data/minecraft/tags/villager_trade/<prof>/level_N.json    vanilla additions
"""
import json
from pathlib import Path

DATA = Path(r"C:\Users\34324\Desktop\um-261\src\main\resources\data")


def write_json(path: Path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def trade(wants_id, wants_count, gives_id, gives_count, max_uses, xp, discount=0.05):
    wants = {"id": wants_id}
    if wants_count != 1:
        wants["count"] = float(wants_count)
    gives = {"id": gives_id}
    if gives_count != 1:
        gives["count"] = float(gives_count)
    return {
        "wants": wants,
        "gives": gives,
        "max_uses": float(max_uses),
        "reputation_discount": discount,
        "xp": float(xp),
    }


# --- COOK trades (ported from 1.21.11 villagerTraders code) ---
cook = {
    "cook/1": [
        ("raw_meat_emerald", "ultramarine:raw_meat", 10, "minecraft:emerald", 1, 12, 2),
        ("porkchop_emerald", "minecraft:porkchop", 15, "minecraft:emerald", 1, 12, 2),
        ("beef_emerald", "minecraft:beef", 15, "minecraft:emerald", 1, 12, 2),
        ("chicken_emerald", "minecraft:chicken", 20, "minecraft:emerald", 1, 12, 2),
        ("emerald_cooked_meat", "minecraft:emerald", 8, "ultramarine:cooked_meat", 4, 12, 2),
    ],
    "cook/2": [
        ("mutton_emerald", "minecraft:mutton", 15, "minecraft:emerald", 1, 12, 4),
        ("rabbit_emerald", "minecraft:rabbit", 15, "minecraft:emerald", 1, 12, 4),
        ("emerald_grease", "minecraft:emerald", 6, "ultramarine:grease", 2, 8, 5),
        ("emerald_fur", "minecraft:emerald", 6, "ultramarine:fur", 2, 8, 5),
    ],
    "cook/3": [
        ("dried_kelp_emerald", "minecraft:dried_kelp", 40, "minecraft:emerald", 2, 15, 10),
        ("sugar_emerald", "minecraft:sugar", 40, "minecraft:emerald", 1, 20, 10),
        ("egg_emerald", "minecraft:egg", 16, "minecraft:emerald", 1, 20, 10),
        ("emerald_mung_bean_cake", "minecraft:emerald", 12, "ultramarine:mung_bean_cake", 4, 12, 10),
        ("emerald_mooncake", "minecraft:emerald", 12, "ultramarine:mooncake", 4, 12, 10),
    ],
    "cook/4": [
        ("emerald_cabbage_basket", "minecraft:emerald", 12, "ultramarine:cabbage_basket", 1, 3, 20),
        ("emerald_celery_basket", "minecraft:emerald", 12, "ultramarine:celery_basket", 1, 3, 20),
        ("emerald_orange_basket", "minecraft:emerald", 12, "ultramarine:orange_basket", 1, 3, 20),
        ("emerald_apple_basket", "minecraft:emerald", 12, "ultramarine:apple_basket", 1, 3, 20),
        ("emerald_eggplant_basket", "minecraft:emerald", 12, "ultramarine:eggplant_basket", 1, 3, 20),
        ("emerald_pear_basket", "minecraft:emerald", 12, "ultramarine:pear_basket", 1, 3, 20),
        ("emerald_baozi", "minecraft:emerald", 5, "ultramarine:baozi", 2, 6, 20),
    ],
    "cook/5": [
        ("emerald_xiaolongbao", "minecraft:emerald", 25, "ultramarine:xiaolongbao", 1, 2, 30),
        ("emerald_wine_pot", "minecraft:emerald", 30, "ultramarine:wine_pot", 1, 2, 20),
        ("emerald_bottle_gourd", "minecraft:emerald", 40, "ultramarine:bottle_gourd", 1, 1, 30),
    ],
}

for set_name, entries in cook.items():
    values = []
    for name, want, wc, give, gc, uses, xp in entries:
        key = f"ultramarine:{name}"
        write_json(DATA / "ultramarine" / "villager_trade" / f"{name}.json",
                   trade(want, wc, give, gc, uses, xp))
        values.append(key)
    write_json(DATA / "ultramarine" / "tags" / "villager_trade" / f"{set_name}.json", {"values": values})
    write_json(DATA / "ultramarine" / "trade_set" / f"{set_name}.json", {
        "amount": 2.0,
        "random_sequence": f"ultramarine:{set_name}",
        "trades": f"#ultramarine:{set_name}",
    })

# --- Vanilla villager additions (mod items, merged into vanilla tags) ---
vanilla = {
    "mason/4": [
        ("mason_emerald_raw_hematite", "minecraft:emerald", 5, "ultramarine:raw_hematite", 2, 12, 10),
        ("mason_emerald_magnesite", "minecraft:emerald", 5, "ultramarine:magnesite", 2, 12, 10),
    ],
    "mason/5": [
        ("mason_emerald_jade", "minecraft:emerald", 10, "ultramarine:jade", 1, 6, 20),
    ],
    "toolsmith/4": [
        ("toolsmith_emerald_bronze_ingot", "minecraft:emerald", 6, "ultramarine:bronze_ingot", 1, 12, 10),
    ],
    "toolsmith/5": [
        ("toolsmith_emerald_carriage", "minecraft:emerald", 40, "ultramarine:carriage", 1, 1, 10),
    ],
    "farmer/3": [
        ("farmer_emerald_rosewood_plank", "minecraft:emerald", 4, "ultramarine:polished_rosewood_plank", 8, 12, 5),
        ("farmer_emerald_ebony_plank", "minecraft:emerald", 4, "ultramarine:polished_ebony_plank", 8, 12, 5),
        ("farmer_emerald_silk", "minecraft:emerald", 30, "ultramarine:silk", 4, 4, 10),
    ],
    "farmer/4": [
        ("farmer_bamboo_tea_basket", "ultramarine:empty_bamboo_tea_basket", 1, "ultramarine:bamboo_tea_basket", 1, 4, 10),
    ],
    "librarian/4": [
        ("librarian_emerald_xuan_paper", "minecraft:emerald", 20, "ultramarine:xuan_paper", 4, 10, 20),
    ],
    "librarian/5": [
        ("librarian_emerald_porcelain_teapot", "minecraft:emerald", 30, "ultramarine:porcelain_teapot", 1, 2, 20),
    ],
    "cleric/5": [
        ("cleric_emerald_sundial", "minecraft:emerald", 40, "ultramarine:sundial", 1, 1, 20),
    ],
}

for tag_name, entries in vanilla.items():
    values = []
    for name, want, wc, give, gc, uses, xp in entries:
        key = f"ultramarine:{name}"
        write_json(DATA / "ultramarine" / "villager_trade" / f"{name}.json",
                   trade(want, wc, give, gc, uses, xp))
        values.append(key)
    write_json(DATA / "minecraft" / "tags" / "villager_trade" / f"{tag_name}.json", {"values": values})

print("done")
