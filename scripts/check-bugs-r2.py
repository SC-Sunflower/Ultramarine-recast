# -*- coding: utf-8 -*-
"""Check which branches still contain each reported bug."""
import os
import re
import sys

WTS = []
for name in os.listdir(r"C:\Users\34324\Desktop"):
    if re.match(r"^um-[\w.-]+-wt$", name):
        WTS.append(os.path.join(r"C:\Users\34324\Desktop", name))
WTS.append(r"C:\Users\34324\Desktop\Ultramarine-recast-fabric")

def read(rel):
    p = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine", rel)
    if not os.path.exists(p):
        return None
    with open(p, encoding="utf-8-sig") as f:
        return f.read()

print("%-12s %-6s %-6s %-6s %-6s %-6s %-6s %-6s %-6s %-6s" % (
    "branch", "b1-npe", "b1-set", "b3-tx", "b5-seat", "b6-cen", "b8-sync", "b9-chi", "b10-scr", "b2-cast"))
for wt in WTS:
    name = os.path.basename(wt)
    # Bug1: BottleGourd save NPE (getKey without null check)
    bg = read(os.path.join("world", "block", "entity", "BottleGourdBlockEntity.java"))
    b1_npe = bg is not None and re.search(r"getKey\(this\.potion\)", bg) is not None
    b1_set = bg is not None and (
        ("addPotionCharge" in bg and "setChanged" not in bg.split("addPotionCharge")[1][:700])
        or ("takePotionCharge" in bg and "setChanged" not in bg.split("takePotionCharge")[1][:700])
    )
    # Bug3: transfer tx in BrickKilnMenu / ChiselTableMenu
    bk = read(os.path.join("world", "block", "menu", "BrickKilnMenu.java"))
    ch = read(os.path.join("world", "block", "menu", "ChiselTableMenu.java"))
    b3_tx = False
    for m in (bk, ch):
        if m and re.search(r"class InventoryWrapper", m) and "TransactionContext" not in m:
            b3_tx = True
    # Bug5: SeatEntity readAdditionalSaveData without assignment
    seat = read(os.path.join("world", "entity", "SeatEntity.java"))
    b5_seat = False
    if seat:
        m = re.search(r"readAdditionalSaveData\([^)]*\)\s*\{([^}]*)\}", seat)
        if m and "=" not in m.group(1):
            b5_seat = True
    # Bug6: Censer tick lacks lit guard
    cen = read(os.path.join("world", "block", "entity", "CenserBlockEntity.java"))
    b6_cen = cen is not None and (
        "public static void tick" in cen
        and not re.search(r"if\s*\(pBlockEntity\.lit\)", cen)
    )
    # Bug8: SeatEntity LIFE entityData unused
    b8_sync = seat is not None and "EntityDataAccessor" in seat and "LIFE" in seat
    # Bug9: ChiselTableRecipe colors count limit
    cr = read(os.path.join("data", "recipe", "ChiselTableRecipe.java"))
    b9_chi = cr is not None and (
        "colors" in cr and "4" not in cr.split("colors")[1][:400] if False else False
    )
    # Bug10: script hardcoded path
    scr = os.path.join(wt, "scripts", "fix-1.20-fatjar.ps1")
    b10_scr = os.path.exists(scr)
    if b10_scr:
        with open(scr, encoding="utf-8-sig", errors="replace") as f:
            b10_scr = "C:\\Users\\34324" in f.read() or "C:\\umtmp" in f.read()
    # Bug2: cast
    be = read(os.path.join("world", "block", "entity", "BrickKilnBlockEntity.java"))
    b2_cast = be is not None and re.search(r"\(AbstractCookingRecipe\)", be) is not None
    print("%-12s %-6s %-6s %-6s %-6s %-6s %-6s %-6s %-6s %-6s" % (
        name, b1_npe, b1_set, b3_tx, b5_seat, b6_cen, b8_sync, "?", b10_scr, b2_cast))
