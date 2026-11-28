# -*- coding: utf-8 -*-
"""Repair the two script-induced compile errors across branches:

1. ChiselTableRecipe: `fieldOf("colors"` got a misplaced `)` — rewrite the whole
   damaged validate chain as `fieldOf("colors").validate(...)`.
2. BrickKilnMenu/ChiselTableMenu: the extract branch gained an abort callback
   that references `original` without declaring it — add the declaration.
"""
import os
import re

BASE = r"C:\Users\34324\Desktop"
targets = [os.path.join(BASE, d) for d in os.listdir(BASE) if re.match(r"^um-[\w.-]+-wt$", d)]
targets.append(r"C:\Users\34324\Desktop\Ultramarine-recast-fabric")

# 1) damaged chisel validate chain
chisel_pat = re.compile(
    r'Ingredient\.CODEC(?:\.listOf\(\))?\.fieldOf\("colors"\n\s*\.validate\(colors -> colors\.size\(\) <= 4\n\s*\? DataResult\.success\(colors\)\n\s*: DataResult\.error\(\(\) -> "Chisel table recipes support at most 4 colors, got " \+ colors\.size\(\)\)\)'
)
chisel_ok = (
    'Ingredient.CODEC.listOf().fieldOf("colors").validate(colors -> colors.size() <= 4\n'
    '      ? DataResult.success(colors)\n'
    '      : DataResult.error(() -> "Chisel table recipes support at most 4 colors, got " + colors.size()))'
)

# 2) extract branch missing the original snapshot
extract_bad = (
    "int toExtract = (int)Math.min(maxAmount, stack.getCount());\n"
    "                   stack.shrink(toExtract);"
)
extract_good = (
    "int toExtract = (int)Math.min(maxAmount, stack.getCount());\n"
    "                   ItemStack original = stack.copy();\n"
    "                   stack.shrink(toExtract);"
)

for wt in targets:
    name = os.path.basename(wt)
    fixed = []
    # chisel
    cp = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                      "data", "recipe", "ChiselTableRecipe.java")
    if os.path.exists(cp):
        with open(cp, encoding="utf-8-sig") as f:
            s = f.read()
        s2, n = chisel_pat.subn(chisel_ok, s)
        if n:
            with open(cp, "w", encoding="utf-8", newline="\n") as f:
                f.write(s2)
            fixed.append("chisel")
    # menus
    for mf in ("BrickKilnMenu.java", "ChiselTableMenu.java"):
        mp = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                          "world", "block", "menu", mf)
        if not os.path.exists(mp):
            continue
        with open(mp, encoding="utf-8-sig") as f:
            s = f.read()
        if "addCloseCallback" in s and extract_bad in s and "ItemStack original = stack.copy();\n                   stack.shrink" not in s:
            s = s.replace(extract_bad, extract_good)
            with open(mp, "w", encoding="utf-8", newline="\n") as f:
                f.write(s)
            fixed.append(mf)
    print(name, "->", ", ".join(fixed) if fixed else "clean")
