# -*- coding: utf-8 -*-
"""Apply the round-2 bug fixes to every worktree branch (idempotent).

Fixes:
  1. BottleGourdBlockEntity: NPE on null potion in saveAdditional; setChanged()
     after charge changes; use MAX_CHARGE constant.
  2. CenserBlockEntity: only tick while lit; mark remainingTime changes;
     restore `lit` from block state on load.
  3. SeatEntity: assign life in readAdditionalSaveData; use MAX_LIFE;
     drop the unused LIFE synched-data accessor.
  4. BrickKilnMenu / ChiselTableMenu InventoryWrapper: register abort
     callbacks so Transfer API transactions roll back cleanly.
  5. Version bump 0.7.0 -> 0.7.0.1 in gradle.properties.
"""
import os
import re
import sys

BASE = r"C:\Users\34324\Desktop"
WTS = [os.path.join(BASE, d) for d in os.listdir(BASE)
       if re.match(r"^um-[\w.-]+-wt$", d)]
WTS.append(r"C:\Users\34324\Desktop\Ultramarine-recast-fabric")

def read(path):
    with open(path, encoding="utf-8-sig") as f:
        return f.read()

def write(path, text):
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)

def jp(wt, rel):
    return os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine", rel)

report = []

def fix_bottle_gourd(wt):
    p = jp(wt, os.path.join("world", "block", "entity", "BottleGourdBlockEntity.java"))
    if not os.path.exists(p):
        return "no file"
    s = read(p)
    orig = s
    # 1a. null-safe save (CompoundTag form)
    s = s.replace(
        'pTag.putString("Potion", BuiltInRegistries.POTION.getKey(this.potion).toString());',
        'if (this.potion != null) {\n         pTag.putString("Potion", BuiltInRegistries.POTION.getKey(this.potion).toString());\n      }'
    )
    # 1a'. null-safe save (ValueOutput form)
    s = s.replace(
        'pTag.putString("Potion", BuiltInRegistries.POTION.getKey(this.potion).toString());',
        'if (this.potion != null) {\n         pTag.putString("Potion", BuiltInRegistries.POTION.getKey(this.potion).toString());\n      }'
    )
    # 1b. setChanged in addPotionCharge (both branches)
    s = s.replace(
        "this.filled = true;\n         return true;\n      } else if (potion.equals(this.potion) && this.charges < 6) {\n         this.charges++;\n         return true;",
        "this.filled = true;\n         this.setChanged();\n         return true;\n      } else if (potion.equals(this.potion) && this.charges < MAX_CHARGE) {\n         this.charges++;\n         this.setChanged();\n         return true;"
    )
    s = s.replace(
        "this.filled = true;\n         return true;\n      } else if (potion.equals(this.potion) && this.charges < MAX_CHARGE) {\n         this.charges++;\n         return true;",
        "this.filled = true;\n         this.setChanged();\n         return true;\n      } else if (potion.equals(this.potion) && this.charges < MAX_CHARGE) {\n         this.charges++;\n         this.setChanged();\n         return true;"
    )
    # 1c. setChanged in takePotionCharge
    s = s.replace(
        "this.potion = null;\n         }\n\n         return Optional.of(charge);",
        "this.potion = null;\n         }\n\n         this.setChanged();\n         return Optional.of(charge);"
    )
    if s != orig:
        write(p, s)
        return "fixed"
    return "unchanged"

def fix_censer(wt):
    p = jp(wt, os.path.join("world", "block", "entity", "CenserBlockEntity.java"))
    if not os.path.exists(p):
        return "no file"
    s = read(p)
    orig = s
    # 2a. lit guard at start of tick
    if "public static void tick(Level pLevel, BlockPos pPos, BlockState pState, CenserBlockEntity pBlockEntity) {" in s:
        if "if (!pBlockEntity.lit) {" not in s:
            s = s.replace(
                "public static void tick(Level pLevel, BlockPos pPos, BlockState pState, CenserBlockEntity pBlockEntity) {\n      if (pBlockEntity.remainingTime <= 0) {",
                "public static void tick(Level pLevel, BlockPos pPos, BlockState pState, CenserBlockEntity pBlockEntity) {\n      // Only burn while lit; an unlit censer must not keep flipping state,\n      // calling setChanged() and dirtying chunks every tick.\n      if (!pBlockEntity.lit) {\n         return;\n      }\n      if (pBlockEntity.remainingTime <= 0) {"
            )
    # 2b. setChanged after decrement (only if not already present)
    s = re.sub(
        r"pBlockEntity\.remainingTime--;\n(?!\s*pBlockEntity\.setChanged\(\);)\n?",
        "pBlockEntity.remainingTime--;\n         pBlockEntity.setChanged();\n",
        s
    )
    # 2c. restore lit on load (CompoundTag form, idempotent)
    if 'this.remainingTime = pTag.getInt("BurnTime");' in s and "BlockState state = this.getBlockState();" not in s:
        s = s.replace(
            'this.remainingTime = pTag.getInt("BurnTime");',
            'this.remainingTime = pTag.getInt("BurnTime");\n      BlockState state = this.getBlockState();\n      this.lit = state.hasProperty(DecorativeBlock.LIT) && state.getValue(DecorativeBlock.LIT);'
        )
    # 2c'. restore lit on load (ValueInput form, idempotent)
    if 'this.remainingTime = pTag.getIntOr("BurnTime", 0);' in s and "BlockState state = this.getBlockState();" not in s:
        s = s.replace(
            'this.remainingTime = pTag.getIntOr("BurnTime", 0);',
            'this.remainingTime = pTag.getIntOr("BurnTime", 0);\n      BlockState state = this.getBlockState();\n      this.lit = state.hasProperty(DecorativeBlock.LIT) && state.getValue(DecorativeBlock.LIT);'
        )
    if s != orig:
        write(p, s)
        return "fixed"
    return "unchanged"

def fix_seat(wt):
    p = jp(wt, os.path.join("world", "entity", "SeatEntity.java"))
    if not os.path.exists(p):
        return "no file"
    s = read(p)
    orig = s
    # 3a. use MAX_LIFE
    s = s.replace("if (this.life > 20) {", "if (this.life > MAX_LIFE) {")
    # 3b. assign on read (CompoundTag)
    s = re.sub(r"(\{\s*\n\s*)pCompound\.getInt\(\"Life\"\);", r"\1this.life = pCompound.getInt(\"Life\");", s)
    # 3c. remove unused LIFE accessor
    s = re.sub(r"\s*private static final EntityDataAccessor<Integer> LIFE = SynchedEntityData\.defineId\(SeatEntity\.class, EntityDataSerializers\.INT\);\n", "\n", s)
    s = re.sub(r"\s*builder\.define\(LIFE, 0\);", "", s)
    # 3d. drop now-unused imports
    if "EntityDataAccessor" not in s.replace("import net.minecraft.network.syncher.EntityDataAccessor;", ""):
        s = s.replace("import net.minecraft.network.syncher.EntityDataAccessor;\n", "")
    if "EntityDataSerializers" not in s.replace("import net.minecraft.network.syncher.EntityDataSerializers;", ""):
        s = s.replace("import net.minecraft.network.syncher.EntityDataSerializers;\n", "")
    if s != orig:
        write(p, s)
        return "fixed"
    return "unchanged"

def fix_wrapper(wt, cls):
    p = jp(wt, os.path.join("world", "block", "menu", cls))
    if not os.path.exists(p):
        return "no file"
    s = read(p)
    orig = s
    if "addCloseCallback" in s:
        return "already"
    # insert snapshot in the empty-slot branch
    s = s.replace(
        "InventoryWrapper.this.inventory.setItem(slot, resource.toStack(toInsert));\n                  return toInsert;",
        "ItemStack original = stack.copy();\n                  InventoryWrapper.this.inventory.setItem(slot, resource.toStack(toInsert));\n                  transaction.addCloseCallback((context, result) -> {\n                     if (result.wasAborted()) {\n                        InventoryWrapper.this.inventory.setItem(slot, original);\n                     }\n                  });\n                  return toInsert;"
    )
    # grow branch
    s = s.replace(
        "stack.grow(canInsert);\n                      return canInsert;",
        "ItemStack original = stack.copy();\n                      stack.grow(canInsert);\n                      transaction.addCloseCallback((context, result) -> {\n                         if (result.wasAborted()) {\n                            InventoryWrapper.this.inventory.setItem(slot, original);\n                         }\n                      });\n                      return canInsert;"
    )
    # extract branch
    s = s.replace(
        "int toExtract = (int)Math.min(maxAmount, stack.getCount());\n                   stack.shrink(toExtract);",
        "int toExtract = (int)Math.min(maxAmount, stack.getCount());\n                   ItemStack original = stack.copy();\n                   stack.shrink(toExtract);"
    )
    s = s.replace(
        "return toExtract;\n               } else {",
        "transaction.addCloseCallback((context, result) -> {\n                     if (result.wasAborted()) {\n                        InventoryWrapper.this.inventory.setItem(slot, original);\n                     }\n                  });\n                  return toExtract;\n               } else {"
    )
    if s != orig:
        write(p, s)
        return "fixed"
    return "unchanged"

def bump_version(wt):
    p = os.path.join(wt, "gradle.properties")
    if not os.path.exists(p):
        return "no file"
    s = read(p)
    if "mod_version=0.7.0.1" in s:
        return "already"
    if "mod_version=0.7.0" in s:
        s = s.replace("mod_version=0.7.0", "mod_version=0.7.0.1", 1)
        write(p, s)
        return "fixed"
    return "no 0.7.0"

for wt in WTS:
    name = os.path.basename(wt)
    r = (
        fix_bottle_gourd(wt), fix_censer(wt), fix_seat(wt),
        fix_wrapper(wt, "BrickKilnMenu.java"), fix_wrapper(wt, "ChiselTableMenu.java"),
        bump_version(wt),
    )
    print("%-28s gourd=%-9s censer=%-9s seat=%-9s kiln=%-9s chisel=%-9s ver=%-9s" % (name, *r))
