# -*- coding: utf-8 -*-
"""Convert mod loot tables to MC 26.3 format.

26.3 changes:
  entry/pool "conditions": [...]            -> "condition" (str shortcut or object; multi -> all_of)
  entry/pool "functions": [...]             -> "modifier"
  match_tool+silk_touch                     -> "minecraft:tool/can_silk_touch"
  block_state_property (same-item branches) -> single entry + conditional set_count(2) on double
  set_count count floats                    -> ints; number providers n/max/min -> ints
  apply_bonus / explosion_decay             -> {"type": ...} forms
"""
import json
import os
from collections import OrderedDict, Counter

ROOT = r"C:\Users\34324\Desktop\um-26.3-wt\src\main\resources\data\ultramarine\loot_table"
stats = Counter()
report = []


def to_int_or_keep(x):
    if isinstance(x, float) and x == int(x):
        return int(x)
    return x


def conv_number_provider(p):
    if not isinstance(p, dict):
        return to_int_or_keep(p)
    t = p.get("type")
    out = OrderedDict()
    if t == "minecraft:binomial":
        out["type"] = "minecraft:binomial"
        out["n"] = to_int_or_keep(p.get("n"))
        out["p"] = p.get("p")
        return out
    if t == "minecraft:uniform":
        out["type"] = "minecraft:uniform"
        out["max"] = to_int_or_keep(p.get("max"))
        out["min"] = to_int_or_keep(p.get("min"))
        return out
    # constant passthrough of other providers
    return p


def conv_condition(c):
    """Single old condition dict -> new condition value (str or dict)."""
    cond = c.get("condition")
    if cond == "minecraft:match_tool":
        pred = c.get("predicate", {})
        ench = pred.get("enchantments", [])
        if len(ench) == 1 and ench[0].get("enchantment") == "minecraft:silk_touch":
            return "minecraft:tool/can_silk_touch"
        report.append("match_tool not silk_touch: %s" % json.dumps(c))
        return None
    if cond == "minecraft:block_state_property":
        return OrderedDict([
            ("type", "minecraft:match_block"),
            ("blocks", c.get("block")),
            ("state", c.get("properties", {})),
        ])
    if cond == "minecraft:random_chance":
        return OrderedDict([
            ("type", "minecraft:random_chance"),
            ("chance", c.get("chance")),
        ])
    if cond == "minecraft:inverted":
        term = c.get("term")
        term_new = conv_conditions([term]) if not isinstance(term, str) else term
        if term_new is None:
            report.append("inverted term failed: %s" % json.dumps(c))
            return None
        return OrderedDict([("type", "minecraft:inverted"), ("term", term_new)])
    report.append("unknown condition: %s" % cond)
    return None


def conv_conditions(conds):
    """Old conditions list -> new condition value (str/dict/None)."""
    if not conds:
        return None
    mapped = []
    for c in conds:
        if isinstance(c, str):
            mapped.append(c)
        elif isinstance(c, dict):
            m = conv_condition(c)
            if m is not None:
                mapped.append(m)
    if not mapped:
        return None
    if len(mapped) == 1:
        return mapped[0]
    return OrderedDict([("type", "minecraft:all_of"), ("terms", mapped)])


def conv_function(f):
    fn = f.get("function")
    if fn == "minecraft:set_count":
        out = OrderedDict([("type", "minecraft:set_count")])
        if not f.get("add", False):
            cnt = f.get("count")
            if isinstance(cnt, dict):
                out["count"] = conv_number_provider(cnt)
            else:
                out["count"] = to_int_or_keep(cnt)
            return out
        report.append("set_count add=true: %s" % json.dumps(f))
        return None
    if fn == "minecraft:apply_bonus":
        out = OrderedDict([("type", "minecraft:apply_bonus")])
        out["enchantment"] = f.get("enchantment")
        out["formula"] = f.get("formula")
        params = f.get("parameters")
        if params:
            out["parameters"] = OrderedDict((k, v) for k, v in params.items())
        return out
    if fn == "minecraft:explosion_decay":
        return OrderedDict([("type", "minecraft:explosion_decay")])
    report.append("unknown function: %s" % fn)
    return None


def conv_functions(funcs):
    if not funcs:
        return None
    mapped = [m for m in (conv_function(f) for f in funcs if isinstance(f, dict)) if m is not None]
    return mapped or None


def block_state_branches(entries):
    """Detect the same-item multi-branch block_state pattern; returns (name, set of states) or None."""
    branches = []
    name = None
    for e in entries:
        if not isinstance(e, dict):
            return None
        if e.get("type") != "minecraft:item":
            return None
        conds = e.get("conditions", [])
        if len(conds) != 1:
            return None
        c = conds[0]
        if not isinstance(c, dict) or c.get("condition") != "minecraft:block_state_property":
            return None
        n = e.get("name")
        if name is None:
            name = n
        elif name != n:
            return None
        branches.append(c.get("properties", {}).get("type"))
    if name is None or len(branches) < 2:
        return None
    return name, set(branches)


def conv_entry(entry):
    if not isinstance(entry, dict):
        return entry
    t = entry.get("type")
    new = OrderedDict()
    # preserve key order roughly: type, condition, modifier, name, children...
    if "type" in entry:
        new["type"] = entry["type"]
    cond_new = None
    if "conditions" in entry:
        cond_new = conv_conditions(entry["conditions"])
    if cond_new is not None:
        new["condition"] = cond_new
    mod_new = None
    if "functions" in entry:
        mod_new = conv_functions(entry["functions"])
    if mod_new is not None:
        new["modifier"] = mod_new
    # copy remaining known keys
    for k, v in entry.items():
        if k in ("type", "conditions", "functions", "children", "entries", "pools"):
            continue
        new[k] = v
    if t in ("minecraft:alternatives", "minecraft:group"):
        kids = entry.get("children", [])
        new["children"] = [conv_entry(k) for k in kids]
    return new


def convert_file(path):
    with open(path, encoding="utf-8-sig") as f:
        data = json.load(f, object_pairs_hook=OrderedDict)
    out = OrderedDict()
    for k, v in data.items():
        if k == "pools":
            pools = []
            for pool in v:
                npool = OrderedDict()
                entries = pool.get("entries", [])
                # slab/basket same-item state branches -> single entry + conditional set_count
                merged = block_state_branches(entries)
                if merged is not None:
                    name, states = merged
                    mods = []
                    if "double" in states:
                        mods.append(OrderedDict([
                            ("type", "minecraft:set_count"),
                            ("condition", OrderedDict([
                                ("type", "minecraft:match_block"),
                                ("blocks", name),
                                ("state", OrderedDict([("type", "double")])),
                            ])),
                            ("count", 2),
                        ]))
                    nentry = OrderedDict([("type", "minecraft:item")])
                    if mods:
                        nentry["modifier"] = mods
                    nentry["name"] = name
                    stats["block_state merged"] += 1
                    npool["entries"] = [nentry]
                else:
                    new_entries = [conv_entry(e) for e in entries]
                    npool["entries"] = new_entries
                # pool-level conditions/functions
                if "conditions" in pool:
                    c = conv_conditions(pool["conditions"])
                    if c is not None:
                        npool["condition"] = c
                if "functions" in pool:
                    m = conv_functions(pool["functions"])
                    if m is not None:
                        npool["modifier"] = m
                for pk, pv in pool.items():
                    if pk in ("entries", "conditions", "functions"):
                        continue
                    if pk in ("rolls", "bonus_rolls") and isinstance(pv, float) and pv == int(pv):
                        npool[pk] = int(pv)
                    else:
                        npool[pk] = pv
                pools.append(npool)
            out["pools"] = pools
        else:
            out[k] = v
    return out


converted = 0
unchanged = 0
for dirpath, _dirnames, filenames in os.walk(ROOT):
    for fn in filenames:
        if not fn.endswith(".json"):
            continue
        path = os.path.join(dirpath, fn)
        before = open(path, encoding="utf-8-sig").read()
        out = convert_file(path)
        after = json.dumps(out, indent=2, ensure_ascii=False) + "\n"
        if before != after:
            with open(path, "w", encoding="utf-8", newline="\n") as f:
                f.write(after)
            converted += 1
            stats["converted"] += 1
        else:
            unchanged += 1

print("converted:", converted, "unchanged:", unchanged)
print("stats:", dict(stats))
print("--- reports (%d) ---" % len(report))
for r in report[:30]:
    print(r[:200])
