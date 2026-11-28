# -*- coding: utf-8 -*-
"""Clean stray backslash-escaped quotes introduced by apply-round2-fixes in SeatEntity."""
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
    s = s.replace('getInt(\\"Life\\")', 'getInt("Life")')
    if s != orig:
        with open(p, "w", encoding="utf-8", newline="\n") as f:
            f.write(s)
        print("cleaned", d)
    else:
        print("clean", d)
