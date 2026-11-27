# -*- coding: utf-8 -*-
"""Enumerate loot table condition/function patterns in the 26.3 worktree."""
import json
import os
from collections import Counter

ROOT = r"C:\Users\34324\Desktop\um-26.3-wt\src\main\resources\data\ultramarine\loot_table"

cond_counter = Counter()
func_counter = Counter()
cond_fields = Counter()
other_keys = Counter()
files = 0

def scan_entry(entry, path):
    if not isinstance(entry, dict):
        return
    t = entry.get("type", "")
    # conditions
    conds = entry.get("conditions", [])
    if isinstance(conds, list):
        for c in conds:
            if isinstance(c, dict):
                cond_counter[c.get("condition")] += 1
                for k in c.keys():
                    cond_fields[(c.get("condition"), k)] += 1
            elif isinstance(c, str):
                cond_counter["STR:" + c] += 1
    elif isinstance(conds, str):
        cond_counter["STR:" + conds] += 1
    # single condition?
    if "condition" in entry:
        c = entry["condition"]
        if isinstance(c, str):
            cond_counter["FIELD:" + c] += 1
    # functions
    funcs = entry.get("functions", [])
    if isinstance(funcs, list):
        for f in funcs:
            if isinstance(f, dict):
                func_counter[f.get("function")] += 1
    if "modifier" in entry:
        mods = entry["modifier"]
        if isinstance(mods, list):
            for f in mods:
                if isinstance(f, dict):
                    func_counter["MODIFIER:" + f.get("type")] += 1
        elif isinstance(mods, dict):
            func_counter["MODIFIER:" + mods.get("type")] += 1
        elif isinstance(mods, str):
            func_counter["MODIFIER:" + mods] += 1
    # pools handled by caller; recurse into children
    for key in ("children", "entries"):
        kids = entry.get(key)
        if isinstance(kids, list):
            for kid in kids:
                scan_entry(kid, path + "/" + key)

for dirpath, _dirnames, filenames in os.walk(ROOT):
    for fn in filenames:
        if not fn.endswith(".json"):
            continue
        path = os.path.join(dirpath, fn)
        files += 1
        with open(path, encoding="utf-8-sig") as f:
            data = json.load(f)
        pools = data.get("pools", [])
        for pool in pools:
            for entry in pool.get("entries", []):
                scan_entry(entry, os.path.relpath(path, ROOT))
            # pool-level conditions/functions
            scan_entry(pool, os.path.relpath(path, ROOT))

print("files:", files)
print("=== conditions ===")
for k, v in cond_counter.most_common():
    print("%5d  %s" % (v, k))
print("=== functions/modifiers ===")
for k, v in func_counter.most_common():
    print("%5d  %s" % (v, k))
