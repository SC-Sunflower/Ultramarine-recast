# -*- coding: utf-8 -*-
"""Patch bundled Porting Lib module jars: set minecraft dependency to '*' so the
same modules load on both 1.20 and 1.20.1 (mirrors fix-1.20-fatjar.ps1)."""
import json
import shutil
import zipfile
from pathlib import Path

libs = Path(r"C:\Users\34324\Desktop\um-120\libs\porting-lib")
patched = 0
for jar in sorted(libs.glob("*.jar")):
    with zipfile.ZipFile(jar) as z:
        names = z.namelist()
        if "fabric.mod.json" not in names:
            continue
        data = json.loads(z.read("fabric.mod.json").decode("utf-8"))
        dep = data.get("depends", {})
        if dep.get("minecraft") == "*":
            continue
        dep["minecraft"] = "*"
        data["depends"] = dep
        tmp = jar.with_suffix(".jar.tmp")
        with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as out:
            for n in names:
                if n == "fabric.mod.json":
                    out.writestr(n, json.dumps(data, indent=2, ensure_ascii=False))
                else:
                    out.writestr(n, z.read(n))
        tmp.replace(jar)
        patched += 1
        print(f"patched {jar.name}")
print(f"total patched: {patched}")
