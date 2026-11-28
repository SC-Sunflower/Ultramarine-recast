# -*- coding: utf-8 -*-
"""Fix shift-click ingredient routing and order-insensitive matching in the
brick kiln for every branch.

Group A (1.20.1):        Container-based matches, Recipe list, no RecipeHolder.
Group B (1.20.2-1.20.6): Container-based matches, RecipeHolder via getAllRecipesFor.
Group C (1.21.1+, 26.x): CraftingInput matches, RecipeHolder via RecipeManagerAccess.
"""
import os
import re

BASE = r"C:\Users\34324\Desktop"
GROUP_A = ["um-main-wt"]
GROUP_B = ["um-1202-wt", "um-1203-wt", "um-1204-wt", "um-1205-wt", "um-1206-wt"]
GROUP_C = ["um-1211-wt", "um-1214-wt", "um-1215-wt", "um-1216-wt", "um-1217-wt",
           "um-1219-wt", "um-12110-wt", "um-12111-wt", "um-26.1-wt", "um-26.2-wt", "um-26.3-wt"]
MAIN = r"C:\Users\34324\Desktop\Ultramarine-recast-fabric"


def read(p):
    with open(p, encoding="utf-8-sig") as f:
        return f.read()


def write(p, s):
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(s)


def fix_matches_container(s):
    old = (
        "public boolean matches(Container pContainer, Level pLevel) {\n"
        "        return this.primaryIngredient.test(pContainer.getItem(0)) && this.secondaryIngredient.test(pContainer.getItem(1));\n"
        "    }"
    )
    new = (
        "public boolean matches(Container pContainer, Level pLevel) {\n"
        "        // Order-insensitive: players may drop materials into either ingredient\n"
        "        // slot (e.g. via shift-click), so accept both arrangements.\n"
        "        return (this.primaryIngredient.test(pContainer.getItem(0)) && this.secondaryIngredient.test(pContainer.getItem(1)))\n"
        "            || (this.primaryIngredient.test(pContainer.getItem(1)) && this.secondaryIngredient.test(pContainer.getItem(0)));\n"
        "    }"
    )
    if old in s:
        return s.replace(old, new), True
    return s, False


def fix_matches_crafting(s):
    # matches with CraftingInput (any variable names / indentation)
    pat = re.compile(
        r'public boolean matches\(CraftingInput input, Level (\w+)\) \{\n(\s*)return this\.primaryIngredient\.test\(input\.getItem\(0\)\) && this\.secondaryIngredient\.test\(input\.getItem\(1\)\);\n\s*\}'
    )
    m = pat.search(s)
    if not m:
        return s, False
    lvl = m.group(1)
    ind = m.group(2)
    close = ind[:-3] if ind.endswith("   ") else ""
    new = (
        "public boolean matches(CraftingInput input, Level %s) {\n" % lvl
        + ind + "// Order-insensitive: players may drop materials into either ingredient\n"
        + ind + "// slot (e.g. via shift-click), so accept both arrangements.\n"
        + ind + "return (this.primaryIngredient.test(input.getItem(0)) && this.secondaryIngredient.test(input.getItem(1)))\n"
        + ind + "|| (this.primaryIngredient.test(input.getItem(1)) && this.secondaryIngredient.test(input.getItem(0)));\n"
        + close + "}"
    )
    return pat.sub(new, s, count=1), True


def add_try_insert(s, group):
    """Insert tryInsertIngredient after canProcess and swap the quickMoveStack call."""
    if "tryInsertIngredient" in s:
        return s, False
    call_old = "if (!this.moveItemStackTo(slotItem, 0, 2, false)) {"
    call_new = "if (!this.tryInsertIngredient(slotItem)) {"
    if call_old not in s:
        return s, False
    s = s.replace(call_old, call_new, 1)
    if group == "A":
        method = (
            "   private boolean tryInsertIngredient(ItemStack item) {\n"
            "      if (this.blockEntity != null && this.blockEntity.getLevel() != null && !this.blockEntity.getLevel().isClientSide) {\n"
            "         for (Recipe<?> raw : this.blockEntity.getLevel().getRecipeManager().getAllRecipesFor(RecipeTypeRegistry.COMPOSITE_SMELTING)) {\n"
            "            if (!(raw instanceof CompositeSmeltingRecipe recipe)) {\n"
            "               continue;\n"
            "            }\n"
            "            boolean asPrimary = recipe.getPrimaryIngredient().test(item);\n"
            "            boolean asSecondary = recipe.getSecondaryIngredient().test(item);\n"
            "            if (asPrimary && !asSecondary) {\n"
            "               if (this.moveItemStackTo(item, 0, 1, false)) {\n"
            "                  return true;\n"
            "               }\n"
            "               return this.moveItemStackTo(item, 1, 2, false);\n"
            "            }\n"
            "            if (asSecondary && !asPrimary) {\n"
            "               if (this.moveItemStackTo(item, 1, 2, false)) {\n"
            "                  return true;\n"
            "               }\n"
            "               return this.moveItemStackTo(item, 0, 1, false);\n"
            "            }\n"
            "         }\n"
            "      }\n"
            "      return this.moveItemStackTo(item, 0, 2, false);\n"
            "   }\n"
        )
    elif group == "B":
        method = (
            "   private boolean tryInsertIngredient(ItemStack item) {\n"
            "      if (this.blockEntity != null && this.blockEntity.getLevel() != null && !this.blockEntity.getLevel().isClientSide) {\n"
            "         for (RecipeHolder<?> holder : this.blockEntity.getLevel().getRecipeManager().getAllRecipesFor(RecipeTypeRegistry.COMPOSITE_SMELTING)) {\n"
            "            if (!(holder.value() instanceof CompositeSmeltingRecipe recipe)) {\n"
            "               continue;\n"
            "            }\n"
            "            boolean asPrimary = recipe.getPrimaryIngredient().test(item);\n"
            "            boolean asSecondary = recipe.getSecondaryIngredient().test(item);\n"
            "            if (asPrimary && !asSecondary) {\n"
            "               if (this.moveItemStackTo(item, 0, 1, false)) {\n"
            "                  return true;\n"
            "               }\n"
            "               return this.moveItemStackTo(item, 1, 2, false);\n"
            "            }\n"
            "            if (asSecondary && !asPrimary) {\n"
            "               if (this.moveItemStackTo(item, 1, 2, false)) {\n"
            "                  return true;\n"
            "               }\n"
            "               return this.moveItemStackTo(item, 0, 1, false);\n"
            "            }\n"
            "         }\n"
            "      }\n"
            "      return this.moveItemStackTo(item, 0, 2, false);\n"
            "   }\n"
        )
    else:
        method = (
            "   private boolean tryInsertIngredient(ItemStack item) {\n"
            "      if (this.blockEntity != null && this.blockEntity.getLevel() != null && !this.blockEntity.getLevel().isClientSide) {\n"
            "         RecipeManager recipeManager = RecipeManagerAccess.get(this.blockEntity.getLevel());\n"
            "         if (recipeManager != null) {\n"
            "            for (RecipeHolder<?> holder : recipeManager.getRecipes()) {\n"
            "               if (!(holder.value() instanceof CompositeSmeltingRecipe recipe)) {\n"
            "                  continue;\n"
            "               }\n"
            "               boolean asPrimary = recipe.getPrimaryIngredient().test(item);\n"
            "               boolean asSecondary = recipe.getSecondaryIngredient().test(item);\n"
            "               if (asPrimary && !asSecondary) {\n"
            "                  if (this.moveItemStackTo(item, 0, 1, false)) {\n"
            "                     return true;\n"
            "                  }\n"
            "                  return this.moveItemStackTo(item, 1, 2, false);\n"
            "               }\n"
            "               if (asSecondary && !asPrimary) {\n"
            "                  if (this.moveItemStackTo(item, 1, 2, false)) {\n"
            "                     return true;\n"
            "                  }\n"
            "                  return this.moveItemStackTo(item, 0, 1, false);\n"
            "               }\n"
            "            }\n"
            "         }\n"
            "      }\n"
            "      return this.moveItemStackTo(item, 0, 2, false);\n"
            "   }\n"
        )
    # anchor: insert before "public boolean stillValid"
    anchor = "public boolean stillValid(Player pPlayer) {"
    if anchor in s:
        s = s.replace(anchor, method + "\n   " + anchor, 1)
    else:
        return s, False
    return s, True


def ensure_import(s, name):
    if name in s:
        return s
    return s.replace("import net.minecraft.world.item.crafting.RecipeManager;",
                     "import net.minecraft.world.item.crafting.RecipeManager;\n" + name, 1)


for wt in [os.path.join(BASE, d) for d in GROUP_A]:
    r = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine", "data", "recipe", "CompositeSmeltingRecipe.java")
    m = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine", "world", "block", "menu", "BrickKilnMenu.java")
    s = read(r); s2, c1 = fix_matches_container(s)
    if c1:
        write(r, s2)
    sm = read(m)
    sm2, c2 = add_try_insert(sm, "A")
    if c2:
        if "import net.minecraft.world.item.crafting.Recipe;" not in sm2:
            sm2 = sm2.replace("import net.minecraft.world.item.crafting.RecipeManager;",
                              "import net.minecraft.world.item.crafting.Recipe;\nimport net.minecraft.world.item.crafting.RecipeManager;", 1)
        write(m, sm2)
    print(os.path.basename(wt), "matches=%s insert=%s" % (c1, c2))

for wt in [os.path.join(BASE, d) for d in GROUP_B]:
    r = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine", "data", "recipe", "CompositeSmeltingRecipe.java")
    m = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine", "world", "block", "menu", "BrickKilnMenu.java")
    s = read(r); s2, c1 = fix_matches_container(s)
    if c1:
        write(r, s2)
    sm = read(m)
    sm2, c2 = add_try_insert(sm, "B")
    if c2:
        if "import net.minecraft.world.item.crafting.RecipeHolder;" not in sm2:
            sm2 = sm2.replace("import net.minecraft.world.item.crafting.RecipeManager;",
                              "import net.minecraft.world.item.crafting.RecipeHolder;\nimport net.minecraft.world.item.crafting.RecipeManager;", 1)
        write(m, sm2)
    print(os.path.basename(wt), "matches=%s insert=%s" % (c1, c2))

for wt in [os.path.join(BASE, d) for d in GROUP_C]:
    r = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine", "data", "recipe", "CompositeSmeltingRecipe.java")
    m = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine", "world", "block", "menu", "BrickKilnMenu.java")
    s = read(r); s2, c1 = fix_matches_crafting(s)
    if c1:
        write(r, s2)
    sm = read(m)
    sm2, c2 = add_try_insert(sm, "C")
    if c2:
        if "import net.minecraft.world.item.crafting.RecipeHolder;" not in sm2:
            sm2 = sm2.replace("import net.minecraft.world.item.crafting.RecipeManager;",
                              "import net.minecraft.world.item.crafting.RecipeHolder;\nimport net.minecraft.world.item.crafting.RecipeManager;", 1)
        write(m, sm2)
    print(os.path.basename(wt), "matches=%s insert=%s" % (c1, c2))
