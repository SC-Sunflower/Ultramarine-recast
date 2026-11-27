# -*- coding: utf-8 -*-
"""Fix unqualified texture references in block models.

Vanilla model loader resolves unqualified refs against the model's own namespace
(ultramarine), where these textures do not exist. Prefix them with minecraft:
where the vanilla texture exists; otherwise qualify with ultramarine: when a
same-path texture exists in our namespace; otherwise report.
"""
import json
import os
import sys

ROOT = sys.argv[1]            # worktree root
VANILLA_TEX = sys.argv[2]     # file listing vanilla texture paths (one per line, no .png, no namespace)
NS = "ultramarine"
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", NS)

with open(VANILLA_TEX, encoding="utf-8") as f:
    vanilla = set(line.strip() for line in f if line.strip())

changed = 0
reports = []
files_touched = 0


def fix_value(ref, source):
    global changed
    if not isinstance(ref, str) or ref.startswith("#") or ":" in ref:
        return ref
    # unqualified reference
    if ref in vanilla:
        changed += 1
        return "minecraft:" + ref
    # our own texture with same path?
    local = os.path.join(ASSETS, "textures", ref + ".png")
    if os.path.exists(local):
        changed += 1
        return NS + ":" + ref
    reports.append((source, ref))
    return ref


for dirpath, _d, filenames in os.walk(os.path.join(ASSETS, "models")):
    for fn in filenames:
        if not fn.endswith(".json"):
            continue
        src = os.path.join(dirpath, fn)
        rel = os.path.relpath(src, ASSETS)
        with open(src, encoding="utf-8-sig") as f:
            data = json.load(f)
        touched = [False]

        def walk(node):
            if isinstance(node, dict):
                if "textures" in node:
                    t = node["textures"]
                    if isinstance(t, dict):
                        for k in list(t.keys()):
                            new = fix_value(t[k], rel)
                            if new != t[k]:
                                t[k] = new
                                touched[0] = True
                    elif isinstance(t, str):
                        new = fix_value(t, rel)
                        if new != t:
                            node["textures"] = new
                            touched[0] = True
                for v in node.values():
                    walk(v)
            elif isinstance(node, list):
                for it in node:
                    walk(it)

        walk(data)
        if touched[0]:
            files_touched += 1
            with open(src, "w", encoding="utf-8", newline="\n") as f:
                json.dump(data, f, indent="\t", ensure_ascii=False)
                f.write("\n")

print("changed refs:", changed, "files touched:", files_touched)
print("unresolvable refs:", len(reports))
for s, r in reports:
    print("  %s -> %s" % (s, r))
