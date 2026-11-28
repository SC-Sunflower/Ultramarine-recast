# -*- coding: utf-8 -*-
"""Rewrite InventoryWrapper insert/extract bodies with correct transaction
snapshots, using indentation detected from the file itself."""
import os
import re

BASE = r"C:\Users\34324\Desktop"
targets = [os.path.join(BASE, d) for d in os.listdir(BASE) if re.match(r"^um-[\w.-]+-wt$", d)]
targets.append(r"C:\Users\34324\Desktop\Ultramarine-recast-fabric")

def make_bodies(ind):
    b0 = ind
    b1 = ind + "   "
    b2 = ind + "      "
    b3 = ind + "         "
    b4 = ind + "            "
    insert = "\n".join([
        b0 + "public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {",
        b1 + "ItemStack stack = InventoryWrapper.this.inventory.getItem(slot);",
        b1 + "if (stack.isEmpty()) {",
        b2 + "int toInsert = (int)Math.min(maxAmount, InventoryWrapper.this.inventory.getMaxStackSize());",
        b2 + "ItemStack original = stack.copy();",
        b2 + "InventoryWrapper.this.inventory.setItem(slot, resource.toStack(toInsert));",
        b2 + "transaction.addCloseCallback((context, result) -> {",
        b3 + "if (result.wasAborted()) {",
        b4 + "InventoryWrapper.this.inventory.setItem(slot, original);",
        b3 + "}",
        b2 + "});",
        b2 + "return toInsert;",
        b1 + "}",
        "",
        b1 + "if (ItemVariant.of(stack).equals(resource)) {",
        b2 + "int canInsert = Math.min((int)maxAmount, InventoryWrapper.this.inventory.getMaxStackSize() - stack.getCount());",
        b2 + "if (canInsert > 0) {",
        b3 + "ItemStack original = stack.copy();",
        b3 + "stack.grow(canInsert);",
        b3 + "transaction.addCloseCallback((context, result) -> {",
        b4 + "if (result.wasAborted()) {",
        b4 + "   InventoryWrapper.this.inventory.setItem(slot, original);",
        b4 + "}",
        b3 + "});",
        b3 + "return canInsert;",
        b2 + "}",
        b1 + "}",
        "",
        b1 + "return 0L;",
        b0 + "}",
    ]) + "\n"
    extract = "\n".join([
        b0 + "public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {",
        b1 + "ItemStack stack = InventoryWrapper.this.inventory.getItem(slot);",
        b1 + "if (!stack.isEmpty() && ItemVariant.of(stack).equals(resource)) {",
        b2 + "int toExtract = (int)Math.min(maxAmount, stack.getCount());",
        b2 + "ItemStack original = stack.copy();",
        b2 + "stack.shrink(toExtract);",
        b2 + "if (stack.isEmpty()) {",
        b3 + "InventoryWrapper.this.inventory.setItem(slot, ItemStack.EMPTY);",
        b2 + "}",
        "",
        b2 + "transaction.addCloseCallback((context, result) -> {",
        b3 + "if (result.wasAborted()) {",
        b4 + "InventoryWrapper.this.inventory.setItem(slot, original);",
        b3 + "}",
        b2 + "});",
        b2 + "return toExtract;",
        b1 + "} else {",
        b2 + "return 0L;",
        b1 + "}",
        b0 + "}",
    ]) + "\n"
    return insert, extract

insert_re = re.compile(r"(\s*)public long insert\(ItemVariant resource, long maxAmount, TransactionContext transaction\) \{.*?\n\1\}", re.S)
extract_re = re.compile(r"(\s*)public long extract\(ItemVariant resource, long maxAmount, TransactionContext transaction\) \{.*?\n\1\}", re.S)

for wt in targets:
    name = os.path.basename(wt)
    fixed = []
    for mf in ("BrickKilnMenu.java", "ChiselTableMenu.java"):
        mp = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                          "world", "block", "menu", mf)
        if not os.path.exists(mp):
            continue
        with open(mp, encoding="utf-8-sig") as f:
            s = f.read()
        changed = False
        m = insert_re.search(s)
        if m:
            ins, _ = make_bodies(m.group(1))
            s = insert_re.sub(ins, s, count=1)
            fixed.append(mf + "-insert")
            changed = True
        m = extract_re.search(s)
        if m:
            _, ext = make_bodies(m.group(1))
            s = extract_re.sub(ext, s, count=1)
            fixed.append(mf + "-extract")
            changed = True
        if changed:
            with open(mp, "w", encoding="utf-8", newline="\n") as f:
                f.write(s)
    print(name, "->", ", ".join(fixed) if fixed else "clean")
