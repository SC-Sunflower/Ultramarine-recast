# -*- coding: utf-8 -*-
"""Fix recipe `result` field formats for Minecraft 1.21.2.

- crafting / smithing: {"item": "x", "count": n} -> {"id": "x", "count": n}
- smelting/blasting/smoking/campfire/stonecutting: "result": "x" -> {"id": "x"}
- ultramarine custom types: {"id": "x", "count": n} -> "result": "x" + top-level "count"
- patchouli recipes: add fabric load condition (patchouli optional)
"""
import json
import sys
from pathlib import Path

BASE = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[1] / "src/main/resources/data"
DIRS = [BASE / "ultramarine" / "recipe", BASE / "minecraft" / "recipe"]

OBJECT_RESULT = {"minecraft:crafting_shaped", "minecraft:crafting_shapeless",
                 "minecraft:smelting", "minecraft:blasting", "minecraft:smoking",
                 "minecraft:campfire_cooking", "minecraft:stonecutting",
                 "minecraft:smithing_transform", "minecraft:smithing_trim"}
STRING_RESULT = {"ultramarine:woodworking", "ultramarine:chisel_table", "ultramarine:composite_smelting"}

changed_files = 0
changed_values = 0


def patch(data):
    global changed_values
    t = data.get("type")
    result = data.get("result")
    if t in OBJECT_RESULT:
        if isinstance(result, str):
            data["result"] = {"id": result}
            changed_values += 1
        elif isinstance(result, dict) and "item" in result:
            rid = result.pop("item")
            data["result"] = {"id": rid, **result}
            changed_values += 1
    elif t in STRING_RESULT and isinstance(result, dict):
        rid = result.get("id")
        if rid:
            cnt = result.get("count", 1)
            data["result"] = rid
            if cnt != 1 and "count" not in data:
                data["count"] = cnt
            changed_values += 1
    if isinstance(t, str) and t.startswith("patchouli:"):
        conds = data.get("fabric:load_conditions")
        if not conds:
            data["fabric:load_conditions"] = [{"condition": "fabric:all_mods_loaded", "values": ["patchouli"]}]
            changed_values += 1
    return data


def main():
    global changed_files
    for d in DIRS:
        for p in sorted(d.rglob("*.json")):
            raw = p.read_text(encoding="utf-8")
            try:
                data = json.loads(raw)
            except json.JSONDecodeError as e:
                print(f"JSON ERROR {p}: {e}", file=sys.stderr)
                continue
            before = json.dumps(data, sort_keys=True)
            patched = patch(data)
            after = json.dumps(patched, sort_keys=True)
            if before != after:
                p.write_text(json.dumps(patched, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
                changed_files += 1
    print(f"changed_files={changed_files} changed_values={changed_values}")


if __name__ == "__main__":
    main()
