# -*- coding: utf-8 -*-
"""For datafixerupper 6.0.8 branches (1.20.2-1.20.4) MapCodec has no validate;
use Codec.flatXmap before fieldOf instead."""
import os
import re

BASE = r"C:\Users\34324\Desktop"
names = ("um-1202-wt", "um-1203-wt", "um-1204-wt")

pat = re.compile(
    r'Ingredient\.CODEC(?:\.listOf\(\))?\.fieldOf\("colors"\)\.validate\(colors -> colors\.size\(\) <= 4[\s\S]*?\.forGetter\(recipe -> recipe\.colors\)'
)

for n in names:
    p = os.path.join(BASE, n, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                     "data", "recipe", "ChiselTableRecipe.java")
    with open(p, encoding="utf-8-sig") as f:
        s = f.read()
    m = pat.search(s)
    if not m:
        print(n, "no match")
        continue
    indent = re.match(r"\s*", m.group(0)).group(0)
    replacement = (
        indent + 'Ingredient.CODEC.listOf().flatXmap(colors -> colors.size() <= 4\n'
        + indent + "      ? DataResult.success(colors)\n"
        + indent + '      : DataResult.error(() -> "Chisel table recipes support at most 4 colors, got " + colors.size()),\n'
        + indent + "      DataResult::success)\n"
        + indent + '      .fieldOf("colors").forGetter(recipe -> recipe.colors)'
    )
    s = pat.sub(replacement, s, count=1)
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(s)
    print(n, "fixed")
