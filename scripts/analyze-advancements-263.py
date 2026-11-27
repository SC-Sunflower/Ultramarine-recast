# -*- coding: utf-8 -*-
"""Analyze advancement JSON patterns in the 26.3 worktree."""
import json
import os
import sys
from collections import Counter

ROOT = r"C:\Users\34324\Desktop\um-26.3-wt\src\main\resources\data\ultramarine\advancement"

triggers = Counter()
item_patterns = Counter()
recipe_patterns = Counter()
issues = []

for dirpath, _dirnames, filenames in os.walk(ROOT):
    for fn in filenames:
        if not fn.endswith(".json"):
            continue
        path = os.path.join(dirpath, fn)
        rel = os.path.relpath(path, ROOT)
        try:
            with open(path, encoding="utf-8") as f:
                data = json.load(f)
        except Exception as e:
            issues.append((rel, "parse error: %s" % e))
            continue
        criteria = data.get("criteria", {})
        for crit_name, crit in criteria.items():
            trig = crit.get("trigger")
            triggers[trig] += 1
            cond = crit.get("conditions", {})
            if trig == "minecraft:recipe_unlocked":
                if "recipe" in cond:
                    recipe_patterns["singular recipe"] += 1
                elif "recipes" in cond:
                    r = cond["recipes"]
                    if isinstance(r, list):
                        recipe_patterns["recipes array len=%d" % len(r)] += 1
                    elif isinstance(r, str):
                        recipe_patterns["recipes string"] += 1
                    else:
                        recipe_patterns["recipes other"] += 1
                else:
                    recipe_patterns["missing recipes key"] += 1
            if trig == "minecraft:inventory_changed":
                items = cond.get("items")
                if items is None:
                    item_patterns["no items key"] += 1
                    continue
                for it in items:
                    if not isinstance(it, dict):
                        item_patterns["items non-dict"] += 1
                        continue
                    keys = set(it.keys())
                    if "tag" in keys:
                        inner = it.get("items")
                        if inner == [] or inner is None:
                            item_patterns["items:[]+tag"] += 1
                        else:
                            item_patterns["items:list+tag (n=%d)" % (len(inner) if isinstance(inner, list) else "?")] += 1
                    else:
                        inner = it.get("items")
                        if isinstance(inner, str):
                            item_patterns["items:string"] += 1
                        elif isinstance(inner, list):
                            item_patterns["items:list (n=%d)" % len(inner)] += 1
                        else:
                            item_patterns["items:other keys=%s" % sorted(keys)] += 1

print("=== triggers ===")
for k, v in triggers.most_common():
    print("%5d  %s" % (v, k))
print("=== recipe_unlocked patterns ===")
for k, v in recipe_patterns.most_common():
    print("%5d  %s" % (v, k))
print("=== inventory_changed item patterns ===")
for k, v in item_patterns.most_common():
    print("%5d  %s" % (v, k))
print("=== issues ===")
for rel, msg in issues[:20]:
    print(rel, msg)
print("total issues:", len(issues))
