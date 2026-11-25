# -*- coding: utf-8 -*-
"""Compute which files each 1.20.x branch is missing vs fabric/1.21.2 (0.7 content)."""
import subprocess
import sys

REPO = str(Path(__file__).resolve().parents[1])
BRANCHES = ["main", "fabric/1.20.2", "fabric/1.20.3", "fabric/1.20.4", "fabric/1.20.5", "fabric/1.20.6"]

PAIRS = [
    ("advancements", "advancement/recipes/", "advancements/recipes/"),
    ("loot_tables", "loot_table/blocks/", "loot_tables/blocks/"),
]


def git_files(branch, prefix):
    out = subprocess.run(
        ["git", "-C", REPO, "ls-tree", "-r", "--name-only", branch, "--",
         "src/main/resources/data/ultramarine/" + prefix],
        capture_output=True, text=True, encoding="utf-8", errors="replace")
    files = set()
    for line in out.stdout.splitlines():
        line = line.strip()
        if not line:
            continue
        files.add(line.replace("src/main/resources/data/ultramarine/" + prefix, ""))
    return files


base_adv = git_files("fabric/1.21.2", "advancement/recipes/")
base_loot = git_files("fabric/1.21.2", "loot_table/blocks/")

print("base advancements:", len(base_adv))
print("base loot tables:", len(base_loot))
print()

for b in BRANCHES:
    adv = git_files(b, "advancements/recipes/")
    loot = git_files(b, "loot_tables/blocks/")
    miss_adv = sorted(base_adv - adv)
    miss_loot = sorted(base_loot - loot)
    print(f"=== {b} ===")
    print(f"  missing advancements: {len(miss_adv)}")
    print(f"  missing loot tables:  {len(miss_loot)}")
    print("  adv:", " ".join(miss_adv) if miss_adv else "(none)")
    print("  loot:", " ".join(miss_loot) if miss_loot else "(none)")
    print()
