# -*- coding: utf-8 -*-
"""In BrickKilnBlockEntity, prefer CompositeSmeltingRecipe for experience
(fallback to AbstractCookingRecipe) so custom recipes grant XP too."""
import os
import re

BASE = r"C:\Users\34324\Desktop"
targets = [os.path.join(BASE, d) for d in os.listdir(BASE) if re.match(r"^um-[\w.-]+-wt$", d)]
targets.append(r"C:\Users\34324\Desktop\Ultramarine-recast-fabric")

old = """            if (holder.value() instanceof AbstractCookingRecipe cookingRecipe) {
               createExperience(pLevel, pos, entry.getIntValue(), cookingRecipe.getExperience());
            }"""
new = """            if (holder.value() instanceof CompositeSmeltingRecipe smeltingRecipe) {
               createExperience(pLevel, pos, entry.getIntValue(), smeltingRecipe.getExp());
            } else if (holder.value() instanceof AbstractCookingRecipe cookingRecipe) {
               createExperience(pLevel, pos, entry.getIntValue(), cookingRecipe.getExperience());
            }"""

for wt in targets:
    p = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                     "world", "block", "entity", "BrickKilnBlockEntity.java")
    if not os.path.exists(p):
        continue
    with open(p, encoding="utf-8-sig") as f:
        s = f.read()
    if "instanceof CompositeSmeltingRecipe" in s:
        print(os.path.basename(wt), "already")
        continue
    if old in s:
        s = s.replace(old, new)
        with open(p, "w", encoding="utf-8", newline="\n") as f:
            f.write(s)
        print(os.path.basename(wt), "fixed")
    else:
        print(os.path.basename(wt), "pattern NOT matched")
