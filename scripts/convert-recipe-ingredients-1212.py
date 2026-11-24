# -*- coding: utf-8 -*-
"""Convert NeoForge 1.21.1 ingredient map format to Minecraft 1.21.2 holder-set format.

Rules (applied recursively to every JSON object):
  {"item": "x"}            ->  "x"
  {"item": ["a","b"]}      ->  ["a","b"]
  {"tag": "#t"}            ->  "#t"
  {"tag": ["#a","#b"]}     ->  ["#a","#b"]

Everything else (result objects, advancement conditions, ...) is left untouched.
"""
import json
import sys
from pathlib import Path

BASE = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(
    r"C:\Users\34324\Desktop\Ultramarine-recast-fabric\src\main\resources\data")
DIRS = [
    BASE / "ultramarine" / "recipe",
    BASE / "minecraft" / "recipe",
]

changed_files = 0
changed_values = 0


def convert(node):
    global changed_values
    if isinstance(node, dict):
        if set(node.keys()) == {"item"}:
            v = node["item"]
            if isinstance(v, str):
                changed_values += 1
                return v
            if isinstance(v, list) and all(isinstance(x, str) for x in v):
                changed_values += 1
                return v
        if set(node.keys()) == {"tag"}:
            v = node["tag"]
            if isinstance(v, str):
                changed_values += 1
                return v
            if isinstance(v, list) and all(isinstance(x, str) for x in v):
                changed_values += 1
                return v
        return {k: convert(v) for k, v in node.items()}
    if isinstance(node, list):
        return [convert(x) for x in node]
    return node


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
            converted = convert(data)
            after = json.dumps(converted, sort_keys=True)
            if before != after:
                p.write_text(json.dumps(converted, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
                changed_files += 1
    print(f"changed_files={changed_files} changed_values={changed_values}")


if __name__ == "__main__":
    main()
