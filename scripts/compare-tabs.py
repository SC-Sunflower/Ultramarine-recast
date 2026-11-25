# -*- coding: utf-8 -*-
"""Compare creative-tab membership: upstream vs port. Prints items that are in a
creative tab upstream but missing from the port's tabs."""
import re
from pathlib import Path

UP = Path(__file__).resolve().parents[1].parent / "Ultramarine-upstream/src/main/java/com/voxelutopia/ultramarine/data/registry/ItemRegistry.java"
PORT = Path(__file__).resolve().parents[1] / "src/main/java/com/voxelutopia/ultramarine/data/registry/ItemRegistry.java"

TABS = {"MATERIALS", "TOOLS", "BUILDING_BLOCKS", "DECORATIVE_BLOCKS", "DECORATIONS",
        "FURNITURE", "WINDOWS_AND_DOORS", "PLANTS", "LAMPS"}


def snake(name):
    return re.sub(r"(?<!^)(?=[A-Z])", "_", name).lower()


def parse(path):
    """Return set of (registered_name, tab) for every item put into a creative tab."""
    text = path.read_text(encoding="utf-8", errors="replace")
    out = set()

    # 1. direct calls: putItemInSet(ItemRegistry.X / field, ModCreativeTab.TAB)
    for m in re.finditer(r"putItemInSet\(\s*([A-Za-z0-9_.]+)\s*,\s*ModCreativeTab\.([A-Z_]+)\s*\)", text):
        field, tab = m.group(1).split(".")[-1], m.group(2)
        if tab in TABS:
            out.add((snake(field), tab))

    # 2. helper functions: find `private static ... name(...) {` blocks containing putItemInSet,
    #    then map their call sites with a quoted registry name to the helper's fixed/param tab.
    helpers = {}  # helper_name -> tab (fixed) or param name
    for m in re.finditer(r"private\s+static\s+[^(){]*?\b([a-zA-Z]+)\(([^)]*)\)\s*\{(.*?)\n\s*\}", text, re.S):
        name, params, body = m.group(1), m.group(2), m.group(3)
        pm = re.search(r"putItemInSet\(\s*[^,]+,\s*(?:ModCreativeTab\.)?([A-Za-z_]+)\s*\)", body)
        if pm:
            t = pm.group(1)
            helpers[name] = t if t in TABS else None  # None = tab passed as parameter

    # helper call sites: helperName("registered_name", ...) -> tab
    for hname, tab in helpers.items():
        if tab is None:
            # tab passed as parameter: helperName("x", ModCreativeTab.TAB)
            for m in re.finditer(re.escape(hname) + r'\(\s*"([a-z0-9_]+)"\s*,\s*ModCreativeTab\.([A-Z_]+)\s*\)', text):
                if m.group(2) in TABS:
                    out.add((m.group(1), m.group(2)))
        else:
            for m in re.finditer(re.escape(hname) + r'\(\s*"([a-z0-9_]+)"', text):
                out.add((m.group(1), tab))

    # 3. fromBlock calls: fromBlock(BlockRegistry.X, ModCreativeTab.TAB) / fromBlock("name", TAB)
    for m in re.finditer(r"fromBlock\(\s*BlockRegistry\.([A-Z0-9_]+)\s*,\s*ModCreativeTab\.([A-Z_]+)\s*\)", text):
        if m.group(2) in TABS:
            out.add((snake(m.group(1)), m.group(2)))
    for m in re.finditer(r"fromBlock\(\s*\"([a-z0-9_]+)\"\s*,\s*ModCreativeTab\.([A-Z_]+)\s*\)", text):
        if m.group(2) in TABS:
            out.add((m.group(1), m.group(2)))

    return out


up = parse(UP)
port = parse(PORT)

up_names = {n for n, _ in up}
port_names = {n for n, _ in port}

print(f"upstream tab items: {len(up)}  port tab items: {len(port)}")
print("\n=== 正版在创造栏、移植版缺失的物品 ===")
missing = sorted(up - port)
for n, t in missing:
    print(f"  - {n}  [{t}]")
print(f"\nmissing count: {len(missing)}")

print("\n=== 移植版在创造栏、正版没有的 ===")
for n, t in sorted(port - up):
    print(f"  + {n}  [{t}]")

print("\n=== 仅注册名维度（跨 tab 合并）===")
missing_names = sorted(up_names - port_names)
print(f"upstream tab names: {len(up_names)}  port tab names: {len(port_names)}  missing: {len(missing_names)}")
for n in missing_names:
    print(f"  - {n}")
