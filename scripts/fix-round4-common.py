# -*- coding: utf-8 -*-
"""Round 4 common fixes across all branches:

  1. BrickKilnBlockEntity.burn: write grown result back to the handler instead of
     mutating a possibly-copied stack.
  2. BottleGourdBlockEntity.takePotionCharge (1.20.x Potions.EMPTY form): call
     setChanged() so charge consumption persists.
  3. BrickKilnMenu/ChiselTableMenu InventoryWrapper: respect the item's own max
     stack size, not just the inventory cap.
  4. SeatDecorativeBlock: consistent client/server interaction result and discard
     the seat entity when mounting fails.
  5. CenserBlockEntity: persist remaining time about once per second instead of
     every tick.
"""
import json
import os
import re

BASE = r"C:\Users\34324\Desktop"
WTS = [os.path.join(BASE, d) for d in os.listdir(BASE) if re.match(r"^um-[\w.-]+-wt$", d)]
WTS.append(r"C:\Users\34324\Desktop\Ultramarine-recast-fabric")


def read(p):
    with open(p, encoding="utf-8-sig") as f:
        return f.read()


def write(p, s):
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(s)


def fix_burn(s):
    pat = re.compile(
        r'(\s*)\} else if \(resultPrev\.is\(newResult\.getItem\(\)\)\) \{\n\s*resultPrev\.grow\(newResult\.getCount\(\)\);\n(\s*)\}'
    )
    def repl(m):
        ind = m.group(1)
        return (
            ind + "} else if (resultPrev.is(newResult.getItem())) {\n"
            + ind + "   // Write the merged stack back explicitly: handler getters may return a copy.\n"
            + ind + "   entity.resultHandler.setStackInSlot(0, resultPrev.copyWithCount(resultPrev.getCount() + newResult.getCount()));\n"
            + m.group(2) + "}"
        )
    return pat.subn(repl, s)


def fix_gourd(s):
    old = (
        "         this.potion = Potions.EMPTY;\n"
        "         }\n\n"
        "         return Optional.of(charge);"
    )
    new = (
        "         this.potion = Potions.EMPTY;\n"
        "         }\n\n"
        "         this.setChanged();\n"
        "         return Optional.of(charge);"
    )
    if old in s:
        return s.replace(old, new), True
    return s, False


def fix_wrapper_stack(s):
    n = 0
    old1 = "int toInsert = (int)Math.min(maxAmount, InventoryWrapper.this.inventory.getMaxStackSize());"
    new1 = (
        "int toInsert = (int)Math.min(maxAmount, Math.min(InventoryWrapper.this.inventory.getMaxStackSize(), "
        "resource.toStack(1).getMaxStackSize()));"
    )
    if old1 in s:
        s = s.replace(old1, new1)
        n += 1
    old2 = "int canInsert = Math.min((int)maxAmount, InventoryWrapper.this.inventory.getMaxStackSize() - stack.getCount());"
    new2 = (
        "int canInsert = Math.min((int)maxAmount, "
        "Math.min(InventoryWrapper.this.inventory.getMaxStackSize(), stack.getMaxStackSize()) - stack.getCount());"
    )
    if old2 in s:
        s = s.replace(old2, new2)
        n += 1
    return s, n


def fix_seat_body(s):
    """Rewrite the use body for InteractionResult forms."""
    old = (
        "         boolean ridingSuccess = false;\n"
        "         if (!pLevel.isClientSide()) {\n"
        "            SeatEntity seat = new SeatEntity(pLevel, Vec3.atCenterOf(pPos).add(this.seatOffset));\n"
        "            pLevel.addFreshEntity(seat);\n"
        "            ridingSuccess = pPlayer.startRiding(seat);\n"
        "         }\n"
        "\n"
        "         return ridingSuccess ? InteractionResult.SUCCESS : InteractionResult.PASS;"
    )
    new = (
        "         if (pLevel.isClientSide()) {\n"
        "            // Client prediction: the seat entity is only created server-side, so\n"
        "            // report success here to keep both sides' interaction results consistent.\n"
        "            return InteractionResult.SUCCESS;\n"
        "         }\n"
        "         SeatEntity seat = new SeatEntity(pLevel, Vec3.atCenterOf(pPos).add(this.seatOffset));\n"
        "         pLevel.addFreshEntity(seat);\n"
        "         if (!pPlayer.startRiding(seat)) {\n"
        "            // Mounting failed; remove the freshly created entity so it does not linger.\n"
        "            seat.discard();\n"
        "            return InteractionResult.PASS;\n"
        "         }\n"
        "         return InteractionResult.SUCCESS;"
    )
    if old in s:
        return s.replace(old, new), True
    # 26.1 direct-return form
    old2 = (
        "         SeatEntity seat = new SeatEntity(pLevel, Vec3.atCenterOf(pPos).add(this.seatOffset));\n"
        "         pLevel.addFreshEntity(seat);\n"
        "         return pPlayer.startRiding(seat) ? InteractionResult.SUCCESS : InteractionResult.PASS;"
    )
    new2 = (
        "         SeatEntity seat = new SeatEntity(pLevel, Vec3.atCenterOf(pPos).add(this.seatOffset));\n"
        "         pLevel.addFreshEntity(seat);\n"
        "         if (!pPlayer.startRiding(seat)) {\n"
        "            // Mounting failed; remove the freshly created entity so it does not linger.\n"
        "            seat.discard();\n"
        "            return InteractionResult.PASS;\n"
        "         }\n"
        "         return InteractionResult.SUCCESS;"
    )
    if old2 in s:
        return s.replace(old2, new2), True
    return s, False


def fix_seat_item_result(s):
    """1.21.1 ItemInteractionResult form."""
    old = (
        "         boolean ridingSuccess = false;\n"
        "         if (!pLevel.isClientSide()) {\n"
        "            SeatEntity seat = new SeatEntity(pLevel, Vec3.atCenterOf(pPos).add(this.seatOffset));\n"
        "            pLevel.addFreshEntity(seat);\n"
        "            ridingSuccess = pPlayer.startRiding(seat);\n"
        "         }\n"
        "\n"
        "         return ridingSuccess ? ItemInteractionResult.sidedSuccess(pLevel.isClientSide()) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;"
    )
    new = (
        "         if (pLevel.isClientSide()) {\n"
        "            // Client prediction: the seat entity is only created server-side, so\n"
        "            // report success here to keep both sides' interaction results consistent.\n"
        "            return ItemInteractionResult.SUCCESS;\n"
        "         }\n"
        "         SeatEntity seat = new SeatEntity(pLevel, Vec3.atCenterOf(pPos).add(this.seatOffset));\n"
        "         pLevel.addFreshEntity(seat);\n"
        "         if (!pPlayer.startRiding(seat)) {\n"
        "            // Mounting failed; remove the freshly created entity so it does not linger.\n"
        "            seat.discard();\n"
        "            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;\n"
        "         }\n"
        "         return ItemInteractionResult.SUCCESS;"
    )
    if old in s:
        return s.replace(old, new), True
    return s, False


def fix_censer(s):
    old = (
        "         pBlockEntity.remainingTime--;\n"
        "         pBlockEntity.setChanged();"
    )
    new = (
        "         pBlockEntity.remainingTime--;\n"
        "         // Persist about once per second instead of every tick: 1200 ticks of\n"
        "         // burning would otherwise dirty the chunk 1200 times.\n"
        "         if (pBlockEntity.remainingTime % 20 == 0) {\n"
        "            pBlockEntity.setChanged();\n"
        "         }"
    )
    if old in s:
        return s.replace(old, new), True
    return s, False


for wt in WTS:
    name = os.path.basename(wt)
    results = []
    # 1. burn
    p = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                     "world", "block", "entity", "BrickKilnBlockEntity.java")
    s = read(p)
    s2, n = fix_burn(s)
    if n:
        write(p, s2)
        results.append("burn")
    # 2. gourd
    p = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                     "world", "block", "entity", "BottleGourdBlockEntity.java")
    s = read(p)
    s2, ok = fix_gourd(s)
    if ok:
        write(p, s2)
        results.append("gourd")
    # 3. wrappers
    for mf in ("BrickKilnMenu.java", "ChiselTableMenu.java"):
        p = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                         "world", "block", "menu", mf)
        s = read(p)
        s2, n = fix_wrapper_stack(s)
        if n:
            write(p, s2)
            results.append(mf.replace(".java", "-stack"))
    # 4. seat
    p = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                     "world", "block", "SeatDecorativeBlock.java")
    s = read(p)
    if "ItemInteractionResult" in s:
        s2, ok = fix_seat_item_result(s)
    else:
        s2, ok = fix_seat_body(s)
    if ok:
        write(p, s2)
        results.append("seat")
    # 5. censer
    p = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                     "world", "block", "entity", "CenserBlockEntity.java")
    s = read(p)
    s2, ok = fix_censer(s)
    if ok:
        write(p, s2)
        results.append("censer")
    print(name, "->", ", ".join(results) if results else "no change")
