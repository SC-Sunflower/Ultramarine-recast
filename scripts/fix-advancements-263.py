# -*- coding: utf-8 -*-
"""Fix advancement JSON for MC 26.3.

MC 26.3 expects:
  recipe_unlocked conditions: "recipes": "modid:recipe"   (string, was: singular "recipe" or 1-elem array)
  inventory_changed items:   [{"items": "#tag"}] or [{"items": "modid:item"}]
                             (old form: {"items": [], "tag": "#tag"} / {"items": ["id"]})
"""
import json
import os
from collections import OrderedDict

ROOT = r"C:\Users\34324\Desktop\um-26.3-wt\src\main\resources\data\ultramarine\advancement"

changed = 0
checked = 0

for dirpath, _dirnames, filenames in os.walk(ROOT):
    for fn in filenames:
        if not fn.endswith(".json"):
            continue
        path = os.path.join(dirpath, fn)
        rel = os.path.relpath(path, ROOT)
        with open(path, encoding="utf-8") as f:
            text = f.read()
        data = json.loads(text, object_pairs_hook=OrderedDict)
        checked += 1
        modified = False

        criteria = data.get("criteria", {})
        for crit_name, crit in criteria.items():
            if not isinstance(crit, dict):
                continue
            trig = crit.get("trigger")
            cond = crit.get("conditions")
            if not isinstance(cond, dict):
                continue
            if trig == "minecraft:recipe_unlocked":
                if "recipe" in cond:
                    val = cond.pop("recipe")
                    cond["recipes"] = val
                    modified = True
                r = cond.get("recipes")
                if isinstance(r, list):
                    cond["recipes"] = r[0] if len(r) == 1 else r
                    modified = True
            if trig == "minecraft:inventory_changed":
                items = cond.get("items")
                if not isinstance(items, list):
                    continue
                for it in items:
                    if not isinstance(it, dict):
                        continue
                    if "tag" in it:
                        tag = it.pop("tag", None)
                        inner = it.get("items")
                        if (inner is None or inner == []) and tag:
                            it["items"] = tag
                            modified = True
                        elif isinstance(inner, list) and tag:
                            # old semantics: items OR tag. 26.3 holderSet cannot express OR;
                            # keep the explicit items, drop the tag (tags here always cover
                            # the same set in this pack).
                            it["items"] = inner
                            modified = True
                    else:
                        inner = it.get("items")
                        if isinstance(inner, list) and len(inner) == 1:
                            it["items"] = inner[0]
                            modified = True

        if modified:
            changed += 1
            with open(path, "w", encoding="utf-8", newline="\n") as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
                f.write("\n")

print("checked:", checked, "changed:", changed)
