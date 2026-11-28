# -*- coding: utf-8 -*-
"""Add the 0..4 color-count limit to ChiselTableRecipe in every branch.

codec-based serializers get a .validate(...) on the colors field;
gson-based serializers (1.20.1 main) get an explicit size check in fromJson.
"""
import os
import re

BASE = r"C:\Users\34324\Desktop"
targets = [os.path.join(BASE, d) for d in os.listdir(BASE) if re.match(r"^um-[\w.-]+-wt$", d)]
targets.append(r"C:\Users\34324\Desktop\Ultramarine-recast-fabric")

for wt in targets:
    p = os.path.join(wt, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                     "data", "recipe", "ChiselTableRecipe.java")
    if not os.path.exists(p):
        print("no file", os.path.basename(wt))
        continue
    with open(p, encoding="utf-8-sig") as f:
        s = f.read()
    orig = s
    name = os.path.basename(wt)

    if "validate" in s and "at most 4 colors" in s:
        print(name, "already")
        continue

    if "RecordCodecBuilder" in s:
        # codec style: find the colors field line and add a validate
        m = re.search(r'(Ingredient\.CODEC(?:\.listOf\(\))?\.fieldOf\("colors")(\))', s)
        if m:
            indent = m.group(0).split("\n")[-1].split("Ingredient")[0]
            s = s.replace(
                m.group(0),
                m.group(1) + '\n' + indent + '      .validate(colors -> colors.size() <= 4\n'
                + indent + '              ? DataResult.success(colors)\n'
                + indent + '              : DataResult.error(() -> "Chisel table recipes support at most 4 colors, got " + colors.size()))' + m.group(2),
                1,
            )
        # ensure DataResult import
        if "import com.mojang.serialization.DataResult;" not in s and "DataResult" in s:
            s = s.replace(
                "import com.mojang.serialization.Codec;",
                "import com.mojang.serialization.Codec;\nimport com.mojang.serialization.DataResult;",
                1,
            )
        if s == orig:
            print(name, "codec pattern NOT matched")
            continue
    else:
        # gson style
        s = s.replace(
            'JsonArray colorsJson = GsonHelper.getAsJsonArray(pJson, "colors");',
            'JsonArray colorsJson = GsonHelper.getAsJsonArray(pJson, "colors");\n         if (colorsJson.size() > 4) {\n            throw new JsonSyntaxException("Chisel table recipes support at most 4 colors, got " + colorsJson.size());\n         }',
            1,
        )
        if s == orig:
            print(name, "gson pattern NOT matched")
            continue

    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(s)
    print(name, "fixed")
