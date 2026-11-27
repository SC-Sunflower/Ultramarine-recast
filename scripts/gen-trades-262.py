# -*- coding: utf-8 -*-
"""Generate data-driven villager trade JSON files for MC 26.2 (Ultramarine Recast).

Layout (data-driven trades, MC 26.1+):
  data/ultramarine/trade_set/cook/level_<N>.json          - trade set per level
  data/ultramarine/tags/villager_trade/cook/level_<N>.json - tag listing that level's trades
  data/ultramarine/villager_trade/cook/<N>/<name>.json     - individual trades
  data/ultramarine/villager_trade/<prof>/<N>/<name>.json   - extra trades for vanilla professions
  data/minecraft/tags/villager_trade/<prof>/level_<N>.json - tag merge (append) into vanilla tags
  data/minecraft/tags/villager_trade/wandering_trader/uncommon.json - rare trader trades
"""
import json
import os
from collections import OrderedDict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources", "data")

# item id -> (wants_count, gives_count) shape helpers
E = "minecraft:emerald"


def trade(gives_id, gives_count, wants_id, wants_count, max_uses, xp, discount=0.05,
          additional_wants_id=None, additional_wants_count=None):
    d = OrderedDict()
    if additional_wants_id:
        aw = OrderedDict([("id", additional_wants_id)])
        if additional_wants_count:
            aw["count"] = float(additional_wants_count)
        d["additional_wants"] = aw
    d["gives"] = OrderedDict([("id", gives_id)])
    if gives_count != 1:
        d["gives"]["count"] = gives_count
    d["max_uses"] = float(max_uses)
    d["reputation_discount"] = float(discount)
    d["wants"] = OrderedDict([("count", float(wants_count)), ("id", wants_id)])
    d["xp"] = float(xp)
    return d


def write_json(path, obj):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("wrote", path)


# ---------------------------------------------------------------- cook trades
# (name, trade dict) per level; content mirrors the 1.21.x VillagerTradesEvent code
cook_trades = {
    1: [
        ("raw_meat_for_emerald", trade(E, 1, "ultramarine:raw_meat", 10, 12, 2)),
        ("porkchop_for_emerald", trade(E, 1, "minecraft:porkchop", 15, 12, 2)),
        ("beef_for_emerald", trade(E, 1, "minecraft:beef", 15, 12, 2)),
        ("chicken_for_emerald", trade(E, 1, "minecraft:chicken", 20, 12, 2)),
        ("emerald_for_cooked_meat", trade("ultramarine:cooked_meat", 4, E, 8, 12, 2)),
    ],
    2: [
        ("mutton_for_emerald", trade(E, 1, "minecraft:mutton", 15, 12, 4)),
        ("rabbit_for_emerald", trade(E, 1, "minecraft:rabbit", 15, 12, 4)),
        ("emerald_for_grease", trade("ultramarine:grease", 2, E, 6, 8, 5)),
        ("emerald_for_fur", trade("ultramarine:fur", 2, E, 6, 8, 5)),
    ],
    3: [
        ("dried_kelp_for_emeralds", trade(E, 2, "minecraft:dried_kelp", 40, 15, 8)),
        ("sugar_for_emerald", trade(E, 1, "minecraft:sugar", 40, 20, 8)),
        ("egg_for_emerald", trade(E, 1, "minecraft:egg", 16, 20, 8)),
        ("emerald_for_mung_bean_cakes", trade("ultramarine:mung_bean_cake", 4, E, 12, 12, 10)),
        ("emerald_for_mooncakes", trade("ultramarine:mooncake", 4, E, 12, 12, 10)),
    ],
    4: [
        ("emerald_for_cabbage_basket", trade("ultramarine:cabbage_basket", 1, E, 12, 3, 20)),
        ("emerald_for_celery_basket", trade("ultramarine:celery_basket", 1, E, 12, 3, 20)),
        ("emerald_for_orange_basket", trade("ultramarine:orange_basket", 1, E, 12, 3, 20)),
        ("emerald_for_apple_basket", trade("ultramarine:apple_basket", 1, E, 12, 3, 20)),
        ("emerald_for_eggplant_basket", trade("ultramarine:eggplant_basket", 1, E, 12, 3, 20)),
        ("emerald_for_pear_basket", trade("ultramarine:pear_basket", 1, E, 12, 3, 20)),
        ("emerald_for_baozi", trade("ultramarine:baozi", 2, E, 5, 6, 20)),
    ],
    5: [
        ("emerald_for_xiaolongbao", trade("ultramarine:xiaolongbao", 1, E, 25, 2, 30)),
        ("emerald_for_wine_pot", trade("ultramarine:wine_pot", 1, E, 30, 2, 20)),
        ("emerald_for_bottle_gourd", trade("ultramarine:bottle_gourd", 1, E, 40, 1, 30)),
    ],
}

for level, entries in cook_trades.items():
    tag_values = []
    for name, obj in entries:
        tid = "ultramarine:cook/%d/%s" % (level, name)
        write_json("ultramarine/villager_trade/cook/%d/%s.json" % (level, name), obj)
        tag_values.append(tid)
    write_json(
        "ultramarine/tags/villager_trade/cook/level_%d.json" % level,
        {"values": tag_values},
    )
    write_json(
        "ultramarine/trade_set/cook/level_%d.json" % level,
        OrderedDict([
            ("amount", 2.0),
            ("random_sequence", "ultramarine:trade_set/cook/level_%d" % level),
            ("trades", "#ultramarine:cook/level_%d" % level),
        ]),
    )

# ------------------------------------------------- vanilla profession extras
# prof -> level -> [(name, trade dict)]
vanilla_extras = {
    "mason": {
        4: [
            ("emerald_for_raw_hematite", trade("ultramarine:raw_hematite", 2, E, 5, 12, 10)),
            ("emerald_for_magnesite", trade("ultramarine:magnesite", 2, E, 5, 12, 10)),
        ],
        5: [
            ("emerald_for_jade", trade("ultramarine:jade", 1, E, 10, 6, 20)),
        ],
    },
    "toolsmith": {
        4: [
            ("emerald_for_bronze_ingot", trade("ultramarine:bronze_ingot", 1, E, 6, 12, 10)),
        ],
        5: [
            ("emerald_for_carriage", trade("ultramarine:carriage", 1, E, 40, 1, 10)),
        ],
    },
    "farmer": {
        3: [
            ("emerald_for_polished_rosewood_planks", trade("ultramarine:polished_rosewood_plank", 8, E, 4, 12, 5)),
            ("emerald_for_polished_ebony_planks", trade("ultramarine:polished_ebony_plank", 8, E, 4, 12, 5)),
            ("emerald_for_silk", trade("ultramarine:silk", 4, E, 30, 4, 10)),
        ],
        4: [
            ("bamboo_tea_basket", trade(
                "ultramarine:bamboo_tea_basket", 1, "ultramarine:empty_bamboo_tea_basket", 1,
                4, 10, 0.05, additional_wants_id=E, additional_wants_count=20)),
        ],
    },
    "librarian": {
        4: [
            ("emerald_for_xuan_paper", trade("ultramarine:xuan_paper", 4, E, 20, 10, 20)),
        ],
        5: [
            ("emerald_for_porcelain_teapot", trade("ultramarine:porcelain_teapot", 1, E, 30, 2, 20)),
        ],
    },
    "cleric": {
        5: [
            ("emerald_for_sundial", trade("ultramarine:sundial", 1, E, 40, 1, 20)),
        ],
    },
}

for prof, levels in vanilla_extras.items():
    for level, entries in levels.items():
        tag_values = []
        for name, obj in entries:
            tid = "ultramarine:%s/%d/%s" % (prof, level, name)
            write_json("ultramarine/villager_trade/%s/%d/%s.json" % (prof, level, name), obj)
            tag_values.append(tid)
        # merge (append) into the vanilla trade tag for this profession level
        write_json("minecraft/tags/villager_trade/%s/level_%d.json" % (prof, level), {"values": tag_values})

# ------------------------------------------------------- wandering trader rares
wanderer_trades = [
    ("emerald_for_incense", trade("ultramarine:incense", 1, E, 6, 6, 30)),
    ("emerald_for_silk", trade("ultramarine:silk", 1, E, 6, 8, 30)),
    ("emerald_for_jade", trade("ultramarine:jade", 1, E, 10, 2, 30)),
    ("emerald_for_painting_scroll", trade("ultramarine:painting_scroll", 1, E, 12, 1, 30)),
    ("emerald_for_porcelain_vase", trade("ultramarine:blue_and_white_porcelain_vase", 1, E, 20, 1, 30)),
]
wanderer_values = []
for name, obj in wanderer_trades:
    tid = "ultramarine:wandering_trader/%s" % name
    write_json("ultramarine/villager_trade/wandering_trader/%s.json" % name, obj)
    wanderer_values.append(tid)
write_json("minecraft/tags/villager_trade/wandering_trader/uncommon.json", {"values": wanderer_values})

print("done")
