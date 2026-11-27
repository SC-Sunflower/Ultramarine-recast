# -*- coding: utf-8 -*-
"""Fix match_tool predicate format in MC 26.1/26.2 loot tables.

26.1+ ItemPredicate uses DataComponentMatchers: the old 1.21.x form
  {"enchantments": [{"enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]}
becomes
  {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}
and the old items list form stays list/string-compatible.
"""
import json
import os
import sys
from collections import OrderedDict

ROOT = sys.argv[1]

changed = 0

def fix_predicate(p):
    if not isinstance(p, dict):
        return p, False
    out = OrderedDict()
    mod = False
    for k, v in p.items():
        if k == "enchantments" and isinstance(v, list):
            # old 1.21.x enchantment list -> new DataComponentMatchers form
            new_list = []
            for e in v:
                if not isinstance(e, dict):
                    new_list.append(e)
                    continue
                ne = OrderedDict()
                for ek, ev in e.items():
                    if ek == "enchantment":
                        ne["enchantments"] = ev
                    else:
                        ne[ek] = ev
                new_list.append(ne)
            out.setdefault("predicates", OrderedDict())["minecraft:enchantments"] = new_list
            mod = True
        else:
            out[k] = v
    return out, mod


def walk(node):
    global changed
    if isinstance(node, dict):
        conds = node.get("conditions")
        if isinstance(conds, list):
            for c in conds:
                if isinstance(c, dict) and c.get("condition") == "minecraft:match_tool":
                    pred = c.get("predicate")
                    if isinstance(pred, dict):
                        fixed, mod = fix_predicate(pred)
                        if mod:
                            c["predicate"] = fixed
                            changed += 1
        for v in node.values():
            walk(v)
    elif isinstance(node, list):
        for item in node:
            walk(item)


for dirpath, _dirnames, filenames in os.walk(os.path.join(ROOT, "src", "main", "resources", "data", "ultramarine", "loot_table")):
    for fn in filenames:
        if not fn.endswith(".json"):
            continue
        path = os.path.join(dirpath, fn)
        with open(path, encoding="utf-8-sig") as f:
            data = json.load(f, object_pairs_hook=OrderedDict)
        walk(data)
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write("\n")

print("fixed predicates:", changed)
