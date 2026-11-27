# -*- coding: utf-8 -*-
"""Show all distinct match_tool / apply_bonus / set_count / random_chance shapes in mod loot files."""
import json
import os
from collections import Counter

ROOT = r"C:\Users\34324\Desktop\um-26.3-wt\src\main\resources\data\ultramarine\loot_table"

shapes = Counter()

def walk(node, path):
    if isinstance(node, dict):
        if "condition" in node and node.get("condition") == "minecraft:match_tool":
            shapes["match_tool:" + json.dumps(node.get("predicate", {}), sort_keys=True)] += 1
        if "function" in node:
            if node["function"] == "minecraft:apply_bonus":
                shapes["apply_bonus:" + json.dumps({k: v for k, v in node.items() if k != "function"}, sort_keys=True)] += 1
            elif node["function"] == "minecraft:set_count":
                shapes["set_count:" + json.dumps({k: v for k, v in node.items() if k != "function"}, sort_keys=True)] += 1
        if "condition" in node and node.get("condition") == "minecraft:random_chance":
            shapes["random_chance:" + json.dumps({k: v for k, v in node.items() if k != "condition"}, sort_keys=True)] += 1
        if "condition" in node and node.get("condition") == "minecraft:inverted":
            shapes["inverted:" + json.dumps({k: v for k, v in node.items() if k != "condition"}, sort_keys=True)] += 1
        if "condition" in node and node.get("condition") == "minecraft:block_state_property":
            shapes["block_state:" + json.dumps({k: v for k, v in node.items() if k != "condition"}, sort_keys=True)] += 1
        for v in node.values():
            walk(v, path)
    elif isinstance(node, list):
        for item in node:
            walk(item, path)

count = 0
for dirpath, _dirnames, filenames in os.walk(ROOT):
    for fn in filenames:
        if not fn.endswith(".json"):
            continue
        with open(os.path.join(dirpath, fn), encoding="utf-8-sig") as f:
            data = json.load(f)
        walk(data, fn)
        count += 1

print("files:", count)
for k, v in shapes.most_common(40):
    print("%5d  %s" % (v, k[:400]))
