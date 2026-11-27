# -*- coding: utf-8 -*-
"""Fix advancement inventory_changed items predicates for MC 26.1/26.2.

26.1+ ItemPredicate has no separate "tag" field: the old 1.21.x form
  {"items": [], "tag": "#minecraft:acacia_logs"}
becomes
  {"items": "#minecraft:acacia_logs"}
and
  {"items": ["minecraft:stick"], "tag": "#minecraft:planks"}
becomes a combined list
  {"items": ["#minecraft:planks", "minecraft:stick"]}
"""
import json
import os
import sys
from collections import OrderedDict

ROOT = sys.argv[1]
changed = 0
files_touched = 0

adv_root = os.path.join(ROOT, "src", "main", "resources", "data", "ultramarine", "advancement")

for dirpath, _d, filenames in os.walk(adv_root):
    for fn in filenames:
        if not fn.endswith(".json"):
            continue
        path = os.path.join(dirpath, fn)
        with open(path, encoding="utf-8-sig") as f:
            data = json.load(f, object_pairs_hook=OrderedDict)
        touched = False
        for crit in data.get("criteria", {}).values():
            if not isinstance(crit, dict):
                continue
            cond = crit.get("conditions")
            if not isinstance(cond, dict):
                continue
            items = cond.get("items")
            if not isinstance(items, list):
                continue
            for it in items:
                if not isinstance(it, dict) or "tag" not in it:
                    continue
                tag = it.pop("tag", None)
                inner = it.get("items")
                if (inner is None or inner == []) and tag:
                    it["items"] = tag
                elif isinstance(inner, list) and tag:
                    merged = [tag] + [x for x in inner]
                    it["items"] = merged
                touched = True
                changed += 1
        if touched:
            files_touched += 1
            with open(path, "w", encoding="utf-8", newline="\n") as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
                f.write("\n")

print("fixed predicates:", changed, "files:", files_touched)
