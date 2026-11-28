# -*- coding: utf-8 -*-
"""Apply the temporary 1.21.8 downgrade edits per scripts/notes-1.21.8-build.md."""
import json
import os
import re

ROOT = r"C:\Users\34324\AppData\Local\Temp\um-1218"

# 1. gradle.properties
gp = os.path.join(ROOT, "gradle.properties")
with open(gp, encoding="utf-8-sig") as f:
    s = f.read()
s = s.replace("minecraft_version=1.21.9", "minecraft_version=1.21.8")
s = s.replace("fabric_api_version=0.134.1+1.21.9", "fabric_api_version=0.134.0+1.21.8")
s = s.replace("parchment_minecraft_version=1.21.9", "parchment_minecraft_version=1.21.8")
s = s.replace("parchment_mappings_version=2025.10.05", "parchment_mappings_version=2025.09.14")
with open(gp, "w", encoding="utf-8", newline="\n") as f:
    f.write(s)
print("gradle.properties patched")

# 2. fabric.mod.json minecraft range
fmj = os.path.join(ROOT, "src", "main", "resources", "fabric.mod.json")
with open(fmj, encoding="utf-8-sig") as f:
    s = f.read()
s = re.sub(r'"minecraft"\s*:\s*">=1\.21\.9 <1\.21\.10"', '"minecraft": ">=1.21.8 <1.21.9"', s)
with open(fmj, "w", encoding="utf-8", newline="\n") as f:
    f.write(s)
print("fabric.mod.json patched")

# 3. pack.mcmeta
pm = os.path.join(ROOT, "src", "main", "resources", "pack.mcmeta")
pack = {
    "pack": {
        "description": "Ultramarine: Recast",
        "pack_format": 81,
        "supported_formats": [64, 81],
    }
}
with open(pm, "w", encoding="utf-8", newline="\n") as f:
    json.dump(pack, f, indent=2)
    f.write("\n")
print("pack.mcmeta patched")

# 4. wind_chime recipe
wc = os.path.join(ROOT, "src", "main", "resources", "data", "ultramarine", "recipe", "wind_chime.json")
if os.path.exists(wc):
    with open(wc, encoding="utf-8-sig") as f:
        s = f.read()
    if "minecraft:iron_chain" in s:
        s = s.replace("minecraft:iron_chain", "minecraft:chain")
        with open(wc, "w", encoding="utf-8", newline="\n") as f:
            f.write(s)
        print("wind_chime.json patched")
    else:
        print("wind_chime.json already uses chain")
else:
    print("wind_chime.json not found")
