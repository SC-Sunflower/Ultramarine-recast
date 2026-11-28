# -*- coding: utf-8 -*-
"""Flatten accidentally double-nested potion null checks in BottleGourdBlockEntity."""
import os
import re

BASE = r"C:\Users\34324\Desktop"
for d in os.listdir(BASE):
    if not re.match(r"^um-[\w.-]+-wt$", d):
        continue
    p = os.path.join(BASE, d, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                     "world", "block", "entity", "BottleGourdBlockEntity.java")
    if not os.path.exists(p):
        continue
    with open(p, encoding="utf-8-sig") as f:
        s = f.read()
    # collapse any run of nested identical null checks down to a single one
    pattern = re.compile(r"(if \(this\.potion != null\) \{\n\s*)+")
    s2, n = pattern.subn("if (this.potion != null) {\n         ", s)
    # the closing braces are harder: after collapsing, there may be surplus '}' lines.
    # Normalize: replace the sequence of putString+closing braces with a canonical block.
    s2 = re.sub(
        r'if \(this\.potion != null\) \{\n\s*pTag\.putString\("Potion", BuiltInRegistries\.POTION\.getKey\(this\.potion\)\.toString\(\)\);\n(\s*\}\n)+',
        'if (this.potion != null) {\n         pTag.putString("Potion", BuiltInRegistries.POTION.getKey(this.potion).toString());\n      }\n',
        s2,
    )
    if s2 != s:
        with open(p, "w", encoding="utf-8", newline="\n") as f:
            f.write(s2)
        print("flattened", d, "(%d runs)" % n)
    else:
        print("unchanged", d)
