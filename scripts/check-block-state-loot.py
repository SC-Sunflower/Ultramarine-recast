# -*- coding: utf-8 -*-
"""Check block_state_property usage: do all state branches drop the same item? Any anomalies?"""
import json
import os
from collections import defaultdict

ROOT = r"C:\Users\34324\Desktop\um-26.3-wt\src\main\resources\data\ultramarine\loot_table"

anomalies = []
same_item_count = 0
state_files = 0

for dirpath, _dirnames, filenames in os.walk(ROOT):
    for fn in filenames:
        if not fn.endswith(".json"):
            continue
        path = os.path.join(dirpath, fn)
        with open(path, encoding="utf-8-sig") as f:
            data = json.load(f)
        pools = data.get("pools", [])
        for pool in pools:
            entries = pool.get("entries", [])
            # collect entries with block_state_property condition
            st_entries = []
            for e in entries:
                conds = e.get("conditions", [])
                for c in conds:
                    if isinstance(c, dict) and c.get("condition") == "minecraft:block_state_property":
                        st_entries.append(e)
                        break
            if st_entries:
                state_files += 1
                names = set(e.get("name") for e in st_entries)
                if len(names) == 1:
                    same_item_count += 1
                else:
                    anomalies.append((os.path.relpath(path, ROOT), sorted(names)))
                # also check states seen
                states = set()
                for e in st_entries:
                    for c in e.get("conditions", []):
                        if isinstance(c, dict) and c.get("condition") == "minecraft:block_state_property":
                            states.add(json.dumps(c.get("properties", {}), sort_keys=True))
                if not states <= {'{"type": "top"}', '{"type": "bottom"}', '{"type": "double"}', '{"type": "single"}'}:
                    anomalies.append((os.path.relpath(path, ROOT), "unexpected states: " + ",".join(sorted(states))))

print("files with block_state entries:", state_files, " same-item:", same_item_count)
print("anomalies:")
for a in anomalies:
    print(" ", a)
