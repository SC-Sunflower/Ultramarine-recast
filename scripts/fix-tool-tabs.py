# -*- coding: utf-8 -*-
"""Move the 4 blue-and-white porcelain tools into the TOOLS creative tab.

- All branches: registerItem( -> toolItem( for the 4 tool fields.
- Supplier-signature branches (1.20.1-1.21.1): wrap `new XxxItem(...)` in `() -> ...`.
"""
import os
import re
from pathlib import Path

BASE = Path(os.environ.get("TEMP", "/tmp")) / "um-tabs"

SUPPLIER_BRANCHES = ["um-1201", "um-1202", "um-1203", "um-1204", "um-1205", "um-1206", "um-1211"]
FUNCTION_BRANCHES = ["um-1214", "um-1215", "um-1216", "um-1217", "um-1219", "um-12110", "um-12111"]

FIELD_RE = re.compile(r"(BLUE_AND_WHITE_PORCELAIN_(?:SWORD|SHOVEL|PICKAXE|AXE)\s*=\s*)registerItem\(")


def process(worktree, supplier):
    p = BASE / worktree / "src/main/java/com/voxelutopia/ultramarine/data/registry/ItemRegistry.java"
    text = p.read_text(encoding="utf-8", errors="replace")
    text, n1 = FIELD_RE.subn(r"\1toolItem(", text)
    n2 = 0
    if supplier:
        def wrap(m):
            return f'"{m.group(1)}", () -> new {m.group(2)}'
        text, n2 = re.subn(
            r'"(blue_and_white_porcelain_(?:sword|shovel|pickaxe|axe))",\s*new (SwordItem|ShovelItem|PickaxeItem|AxeItem)',
            wrap, text)
    p.write_text(text, encoding="utf-8")
    print(f"{worktree}: registerItem->toolItem x{n1}, supplier-wrap x{n2}")


for w in SUPPLIER_BRANCHES:
    process(w, supplier=True)
for w in FUNCTION_BRANCHES:
    process(w, supplier=False)
