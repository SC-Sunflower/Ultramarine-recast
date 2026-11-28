# -*- coding: utf-8 -*-
"""Rewrite the chisel colors validate chain with exact bracket counts."""
import os
import re

BASE = r"C:\Users\34324\Desktop"
targets = [os.path.join(BASE, d) for d in os.listdir(BASE) if re.match(r"^um-[\w.-]+-wt$", d)]
targets.append(r"C:\Users\34324\Desktop\Ultramarine-recast-fabric")

# match any current (possibly damaged) form of the colors field in the codec
pat = re.compile(
    r'Ingredient\.CODEC(?:\.listOf\(\))?\.fieldOf\("colors"[\s\S]*?\.forGetter\(recipe -> recipe\.colors\)'
)

for wt in targets:
    p = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                     "data", "recipe", "ChiselTableRecipe.java")
    if not os.path.exists(p):
        continue
    with open(p, encoding="utf-8-sig") as f:
        s = f.read()
    m = pat.search(s)
    if not m:
        print(os.path.basename(wt), "no match")
        continue
    indent = re.match(r"\s*", m.group(0)).group(0)
    replacement = (
        indent + 'Ingredient.CODEC.listOf().fieldOf("colors").validate(colors -> colors.size() <= 4\n'
        + indent + "      ? DataResult.success(colors)\n"
        + indent + '      : DataResult.error(() -> "Chisel table recipes support at most 4 colors, got " + colors.size()))\n'
        + indent + "      .forGetter(recipe -> recipe.colors)"
    )
    new_s = pat.sub(replacement, s, count=1)
    # ensure DataResult import
    if "import com.mojang.serialization.DataResult;" not in new_s and "DataResult" in new_s:
        new_s = new_s.replace(
            "import com.mojang.serialization.Codec;",
            "import com.mojang.serialization.Codec;\nimport com.mojang.serialization.DataResult;",
            1,
        )
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(new_s)
    print(os.path.basename(wt), "rebuilt")
