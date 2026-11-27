# -*- coding: utf-8 -*-
"""Generate MC 26.3-format worldgen files (feature + placed_feature) for the four ores.

MC 26.3 merged configured_feature into the `feature` registry:
  data/<ns>/worldgen/feature/<name>.json        (flat ore config, no "config" wrapper)
  data/<ns>/worldgen/placed_feature/<name>.json ("feature" points at the feature entry)
"""
import json
import os
from collections import OrderedDict

ROOT = r"C:\Users\34324\Desktop\um-26.3-wt"
RES = os.path.join(ROOT, "src", "main", "resources", "data", "ultramarine", "worldgen")

FEATURES = {
    "overworld_jade_ore": {
        "size": 4,
        "targets": [
            ("ultramarine:jade_ore", "minecraft:stone_ore_replaceables"),
            ("ultramarine:deepslate_jade_ore", "minecraft:deepslate_ore_replaceables"),
        ],
    },
    "overworld_magnesite_ore": {
        "size": 7,
        "targets": [
            ("ultramarine:magnesite_ore", "minecraft:stone_ore_replaceables"),
            ("ultramarine:deepslate_magnesite_ore", "minecraft:deepslate_ore_replaceables"),
        ],
    },
    "overworld_hematite_ore": {
        "size": 9,
        "targets": [
            ("ultramarine:hematite_ore", "minecraft:stone_ore_replaceables"),
            ("ultramarine:deepslate_hematite_ore", "minecraft:deepslate_ore_replaceables"),
        ],
    },
    "nether_cobalt_ore": {
        "size": 6,
        "targets": [
            ("ultramarine:nether_cobalt_ore", "minecraft:netherrack"),
        ],
    },
}

PLACED = {
    "overworld_jade_ore_placed": {
        "feature": "ultramarine:overworld_jade_ore",
        "count": 7,
        "height": ("minecraft:trapezoid", -80, 80),
    },
    "overworld_magnesite_ore_placed": {
        "feature": "ultramarine:overworld_magnesite_ore",
        "count": 4,
        "height": ("minecraft:trapezoid", -40, 60),
    },
    "overworld_hematite_ore_placed": {
        "feature": "ultramarine:overworld_hematite_ore",
        "count": 10,
        "height": ("minecraft:trapezoid", -24, 56),
    },
    "nether_cobalt_ore_placed": {
        "feature": "ultramarine:nether_cobalt_ore",
        "count": 8,
        "height": ("minecraft:uniform", 0, 32),
    },
}


def rule_test(tag):
    # vanilla 26.3 uses tag_match for ore replaceable tags, block_match for single blocks
    if tag == "minecraft:netherrack":
        return OrderedDict([("predicate_type", "minecraft:block_match"), ("block", "minecraft:netherrack")])
    return OrderedDict([("predicate_type", "minecraft:tag_match"), ("tag", tag)])


for name, spec in FEATURES.items():
    targets = []
    for state, tag in spec["targets"]:
        targets.append(OrderedDict([("state", state), ("target", rule_test(tag))]))
    feature = OrderedDict([
        ("type", "minecraft:ore"),
        ("discard_chance_on_air_exposure", 0.0),
        ("size", spec["size"]),
        ("targets", targets),
    ])
    path = os.path.join(RES, "feature", name + ".json")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(feature, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("wrote feature/" + name)

for name, spec in PLACED.items():
    height_type, hmin, hmax = spec["height"]
    if height_type == "minecraft:uniform":
        height = OrderedDict([
            ("type", "minecraft:uniform"),
            ("max_inclusive", OrderedDict([("absolute", hmax)])),
            ("min_inclusive", OrderedDict([("absolute", hmin)])),
        ])
    else:
        height = OrderedDict([
            ("type", "minecraft:trapezoid"),
            ("max_inclusive", OrderedDict([("absolute", hmax)])),
            ("min_inclusive", OrderedDict([("absolute", hmin)])),
        ])
    placed = OrderedDict([
        ("feature", spec["feature"]),
        ("placement", [
            OrderedDict([("type", "minecraft:count"), ("count", spec["count"])]),
            OrderedDict([("type", "minecraft:in_square")]),
            OrderedDict([("type", "minecraft:height_range"), ("height", height)]),
            OrderedDict([("type", "minecraft:biome")]),
        ]),
    ])
    path = os.path.join(RES, "placed_feature", name + ".json")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(placed, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("wrote placed_feature/" + name)
