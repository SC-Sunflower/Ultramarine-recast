# -*- coding: utf-8 -*-
"""Fix imports and the 26.x isClientSide() call in the generated tryInsertIngredient."""
import os
import re

BASE = r"C:\Users\34324\Desktop"
GROUP_A = ["um-main-wt"]
GROUP_B = ["um-1202-wt", "um-1203-wt", "um-1204-wt", "um-1205-wt", "um-1206-wt"]
GROUP_26 = ["um-26.1-wt", "um-26.2-wt", "um-26.3-wt"]

def read(p):
    with open(p, encoding="utf-8-sig") as f:
        return f.read()

def write(p, s):
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(s)

for d in GROUP_A:
    p = os.path.join(BASE, d, "src", "main", "java", "com", "voxelutopia", "ultramarine", "world", "block", "menu", "BrickKilnMenu.java")
    s = read(p)
    if "import net.minecraft.world.item.crafting.Recipe;" not in s:
        s = s.replace("import net.minecraft.world.item.ItemStack;",
                      "import net.minecraft.world.item.ItemStack;\nimport net.minecraft.world.item.crafting.Recipe;", 1)
        write(p, s)
        print(d, "added Recipe import")
    else:
        print(d, "has import")

for d in GROUP_B:
    p = os.path.join(BASE, d, "src", "main", "java", "com", "voxelutopia", "ultramarine", "world", "block", "menu", "BrickKilnMenu.java")
    s = read(p)
    if "import net.minecraft.world.item.crafting.RecipeHolder;" not in s:
        s = s.replace("import net.minecraft.world.item.ItemStack;",
                      "import net.minecraft.world.item.ItemStack;\nimport net.minecraft.world.item.crafting.RecipeHolder;", 1)
        write(p, s)
        print(d, "added RecipeHolder import")
    else:
        print(d, "has import")

for d in GROUP_26:
    p = os.path.join(BASE, d, "src", "main", "java", "com", "voxelutopia", "ultramarine", "world", "block", "menu", "BrickKilnMenu.java")
    s = read(p)
    # only fix the generated method, not other (already valid) usages
    s = s.replace("!this.blockEntity.getLevel().isClientSide)", "!this.blockEntity.getLevel().isClientSide())", 1)
    write(p, s)
    print(d, "isClientSide() fixed")
