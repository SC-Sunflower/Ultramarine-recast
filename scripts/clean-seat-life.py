# -*- coding: utf-8 -*-
"""Remove leftover LIFE references in 1.20.x SeatEntity defineSynchedData."""
import os
import re

BASE = r"C:\Users\34324\Desktop"
for d in os.listdir(BASE):
    if not re.match(r"^um-[\w.-]+-wt$", d):
        continue
    p = os.path.join(BASE, d, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                     "world", "entity", "SeatEntity.java")
    if not os.path.exists(p):
        continue
    with open(p, encoding="utf-8-sig") as f:
        s = f.read()
    orig = s
    s = re.sub(r"\s*this\.entityData\.define\(LIFE, 0\);", "", s)
    s = re.sub(r"\s*builder\.define\(LIFE, 0\);", "", s)
    if s != orig:
        with open(p, "w", encoding="utf-8", newline="\n") as f:
            f.write(s)
        print("cleaned", d)
    else:
        print("clean", d)
