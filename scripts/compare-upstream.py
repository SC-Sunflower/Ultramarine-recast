# -*- coding: utf-8 -*-
"""Compare the released upstream Ultramarine 0.7 jar with the recast port."""
import json
import re
import zipfile
from pathlib import Path

UP_JAR = Path(r"C:\Users\34324\AppData\Local\Temp\ultramarine-0.7.jar")
PORT = Path(r"C:\Users\34324\Desktop\Ultramarine-recast-fabric")
REGISTRY = "src/main/java/com/voxelutopia/ultramarine/data/registry"


def jar_names(jar, prefix):
    with zipfile.ZipFile(jar) as z:
        return {n.split("/")[-1] for n in z.namelist() if n.startswith(prefix) and n.endswith(".json") and not n.endswith("/")}


def jar_file_names(jar, prefix, suffix):
    with zipfile.ZipFile(jar) as z:
        return {n[len(prefix):] for n in z.namelist() if n.startswith(prefix) and n.endswith(suffix)}


def jar_lang_keys(jar, lang):
    with zipfile.ZipFile(jar) as z:
        names = [n for n in z.namelist() if n.endswith(f"lang/{lang}")]
        if not names:
            return set()
        return set(json.loads(z.read(names[0]).decode("utf-8", errors="replace")).keys())


def registry_names(path):
    text = path.read_text(encoding="utf-8", errors="replace")
    return {m.group(1) for m in re.finditer(r"public\s+static\s+final\s+[^=\n]*?\b([A-Z][A-Z0-9_]+)\s*=", text) if "_" in m.group(1)}


def port_files(base, rel):
    d = base / rel
    return {str(p.relative_to(d)).replace("\\", "/") for p in d.rglob("*") if p.is_file()} if d.is_dir() else set()


def port_lang_keys(base, lang):
    p = base / "src/main/resources/assets/ultramarine/lang" / lang
    return set(json.loads(p.read_text(encoding="utf-8", errors="replace")).keys()) if p.is_file() else set()


def report(title, up_set, port_set):
    only_up = sorted(up_set - port_set)
    only_port = sorted(port_set - up_set)
    print(f"\n=== {title} ===")
    print(f"upstream={len(up_set)}  port={len(port_set)}  only_upstream={len(only_up)}  only_port={len(only_port)}")
    for x in only_up:
        print(f"  - {x}")
    for x in only_port:
        print(f"  + {x}")


def registry_strings(path):
    """Registered id strings from register(...) calls: registerBlock(\"x\") / registerItem(\"x\")."""
    text = path.read_text(encoding="utf-8", errors="replace")
    return set(re.findall(r'register(?:Block|Item)\(\s*"([a-z0-9_/]+)"', text))


def upstream_registry_strings(base):
    """Upstream: BLOCKS.register(\"x\", ...), ITEMS.register(\"x\", ...) and toolItem(\"x\", ...)."""
    out = set()
    for f in ["BlockRegistry.java", "ItemRegistry.java"]:
        p = base / "src/main/java/com/voxelutopia/ultramarine/data/registry" / f
        if p.is_file():
            text = p.read_text(encoding="utf-8", errors="replace")
            out |= set(re.findall(r'\.register\(\s*"([a-z0-9_/]+)"', text))
            out |= set(re.findall(r'toolItem\(\s*"([a-z0-9_/]+)"', text))
    return out


UP_SRC = Path(r"C:\Users\34324\Desktop\Ultramarine-upstream")
up_items = upstream_registry_strings(UP_SRC)
port_items = registry_strings(PORT / REGISTRY / "ItemRegistry.java") | registry_strings(PORT / REGISTRY / "BlockRegistry.java")
report("注册名字符串(正版 BLOCKS/ITEMS.register vs 移植 registerBlock/registerItem)", up_items, port_items)

# recipes
up_recipes = jar_names(UP_JAR, "data/ultramarine/recipe/")
port_recipes = {p.name for p in (PORT / "src/main/resources/data/ultramarine/recipe").glob("*.json")} if (PORT / "src/main/resources/data/ultramarine/recipe").is_dir() else set()
report("配方文件名", up_recipes, port_recipes)

up_models_item = jar_file_names(UP_JAR, "assets/ultramarine/models/item/", ".json")
port_models_item = port_files(PORT, "src/main/resources/assets/ultramarine/models/item")
report("物品模型", up_models_item, port_models_item)

up_models_block = jar_file_names(UP_JAR, "assets/ultramarine/models/block/", ".json")
port_models_block = port_files(PORT, "src/main/resources/assets/ultramarine/models/block")
report("方块模型", up_models_block, port_models_block)

up_textures_item = jar_file_names(UP_JAR, "assets/ultramarine/textures/item/", ".png")
port_textures_item = port_files(PORT, "src/main/resources/assets/ultramarine/textures/item")
report("物品纹理", up_textures_item, port_textures_item)

up_textures_block = jar_file_names(UP_JAR, "assets/ultramarine/textures/block/", ".png")
port_textures_block = port_files(PORT, "src/main/resources/assets/ultramarine/textures/block")
report("方块纹理", up_textures_block, port_textures_block)

up_blockstates = jar_file_names(UP_JAR, "assets/ultramarine/blockstates/", ".json")
port_blockstates = port_files(PORT, "src/main/resources/assets/ultramarine/blockstates")
report("blockstates", up_blockstates, port_blockstates)

up_lang = jar_lang_keys(UP_JAR, "en_us.json")
port_lang = port_lang_keys(PORT, "en_us.json")
report("en_us 语言键", up_lang, port_lang)

up_lt = jar_file_names(UP_JAR, "data/ultramarine/loot_table/", ".json")
port_lt = port_files(PORT, "src/main/resources/data/ultramarine/loot_table")
report("战利品表", up_lt, port_lt)

up_tags = jar_file_names(UP_JAR, "data/ultramarine/tags/", ".json")
port_tags = port_files(PORT, "src/main/resources/data/ultramarine/tags")
report("标签", up_tags, port_tags)

up_adv = jar_file_names(UP_JAR, "data/ultramarine/advancement/", ".json")
port_adv = port_files(PORT, "src/main/resources/data/ultramarine/advancement")
report("成就", up_adv, port_adv)

up_sounds = jar_file_names(UP_JAR, "assets/ultramarine/sounds/", ".ogg")
port_sounds = port_files(PORT, "src/main/resources/assets/ultramarine/sounds")
report("声音文件", up_sounds, port_sounds)

print("\n[compare done]")
