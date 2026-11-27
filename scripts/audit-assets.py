# -*- coding: utf-8 -*-
"""Audit textures and lang coverage for a mod worktree.

Checks:
  1. Every "textures" reference inside assets/<ns>/models/**/*.json resolves to an
     existing file under assets/<ns>/textures/.
  2. Every registered item/block (from the registry code or from models/item + blockstates)
     has a lang entry (item.<ns>.<id> / block.<ns>.<id>) in each lang file.
"""
import json
import os
import re
import sys

ROOT = sys.argv[1]
NS = "ultramarine"
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", NS)

missing_textures = []
texture_refs = 0

def check_texture_ref(ref, source):
    global texture_refs
    if not isinstance(ref, str):
        return
    texture_refs += 1
    if ref.startswith("#"):
        return  # reference to another variable in same model
    if ":" in ref:
        other_ns, path = ref.split(":", 1)
        if other_ns != NS and other_ns != "minecraft":
            return  # other mod's texture, not our concern here
        # only check our own namespace
        if other_ns != NS:
            return
    else:
        path = ref
    p = os.path.join(ASSETS, "textures", path + ".png")
    if not os.path.exists(p):
        missing_textures.append((source, ref))

for dirpath, _d, filenames in os.walk(os.path.join(ASSETS, "models")):
    for fn in filenames:
        if not fn.endswith(".json"):
            continue
        src = os.path.join(dirpath, fn)
        rel = os.path.relpath(src, ASSETS)
        try:
            with open(src, encoding="utf-8-sig") as f:
                data = json.load(f)
        except Exception as e:
            print("BAD JSON model:", rel, e)
            continue
        def walk_textures(node):
            if isinstance(node, dict):
                if "textures" in node:
                    t = node["textures"]
                    if isinstance(t, dict):
                        for k, v in t.items():
                            check_texture_ref(v, rel)
                    elif isinstance(t, str):
                        check_texture_ref(t, rel)
                for v in node.values():
                    walk_textures(v)
            elif isinstance(node, list):
                for it in node:
                    walk_textures(it)
        walk_textures(data)

print("texture refs checked:", texture_refs)
print("missing textures:", len(missing_textures))
for src, ref in missing_textures[:60]:
    print("  %s -> %s" % (src, ref))

# ---- lang coverage ----
lang_dir = os.path.join(ASSETS, "lang")
langs = {}
for fn in os.listdir(lang_dir):
    if fn.endswith(".json"):
        with open(os.path.join(lang_dir, fn), encoding="utf-8-sig") as f:
            langs[fn] = json.load(f)

# collect ids from item models and blockstates (assets-driven set)
ids = set()
for sub in ("models/item", "blockstates"):
    d = os.path.join(ASSETS, sub)
    if os.path.isdir(d):
        for fn in os.listdir(d):
            if fn.endswith(".json"):
                ids.add(fn[:-5])

missing_lang = {}
for lang_name, table in langs.items():
    miss = []
    for i in sorted(ids):
        key_item = "item.%s.%s" % (NS, i)
        key_block = "block.%s.%s" % (NS, i)
        if key_item not in table and key_block not in table:
            miss.append(i)
    missing_lang[lang_name] = miss

print("asset ids:", len(ids))
for lang_name, miss in missing_lang.items():
    print("missing lang in %s: %d" % (lang_name, len(miss)))
    if miss:
        print("   " + ", ".join(miss[:80]))
