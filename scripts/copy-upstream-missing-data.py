# -*- coding: utf-8 -*-
"""Copy the 31 missing loot tables and 53 missing advancements from the
released upstream 0.7 jar into the recast port (fabric/1.21.2 layout)."""
import json
import zipfile
from pathlib import Path

UP_JAR = Path(r"C:\Users\34324\AppData\Local\Temp\ultramarine-0.7.jar")
PORT = Path(r"C:\Users\34324\Desktop\Ultramarine-recast-fabric")

LOOT_PREFIX = "data/ultramarine/loot_table/"
ADV_PREFIX = "data/ultramarine/advancement/"

loot_dst = PORT / "src/main/resources/data/ultramarine/loot_table"
adv_dst = PORT / "src/main/resources/data/ultramarine/advancement"

loot_copied = 0
adv_copied = 0

with zipfile.ZipFile(UP_JAR) as z:
    for n in z.namelist():
        if n.startswith(LOOT_PREFIX) and n.endswith(".json"):
            rel = n[len(LOOT_PREFIX):]
            dst = loot_dst / rel
            if dst.exists():
                continue
            dst.parent.mkdir(parents=True, exist_ok=True)
            data = json.loads(z.read(n).decode("utf-8"))
            dst.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
            loot_copied += 1
        elif n.startswith(ADV_PREFIX) and n.endswith(".json"):
            rel = n[len(ADV_PREFIX):]
            dst = adv_dst / rel
            if dst.exists():
                continue
            dst.parent.mkdir(parents=True, exist_ok=True)
            data = json.loads(z.read(n).decode("utf-8"))
            dst.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
            adv_copied += 1

print(f"loot_tables_copied={loot_copied} advancements_copied={adv_copied}")
