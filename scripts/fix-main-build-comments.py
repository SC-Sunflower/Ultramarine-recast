# -*- coding: utf-8 -*-
"""Rewrite the mojibake comments in main branch build.gradle and document the
Porting Lib fetch step in README."""
import os

REPO = r"C:\Users\34324\Desktop\um-main-wt"

p = os.path.join(REPO, "build.gradle")
with open(p, encoding="utf-8-sig") as f:
    s = f.read()

old_comment_start = "    // Porting Lib 2.3.8"
idx = s.find(old_comment_start)
end_marker = '    modImplementation fileTree(dir: \'libs/porting-lib\', include: \'*.jar\')'
end = s.find(end_marker)
if idx != -1 and end != -1:
    new_comment = (
        "    // Porting Lib 2.3.8 (see gradle.properties -> porting_lib_version).\n"
        "    // - Compile time: the module jars extracted by scripts/fetch-porting-lib.ps1 are\n"
        "    //   added from libs/porting-lib (the nested jars of the aggregate jar are not on\n"
        "    //   the compile classpath).\n"
        "    // - Packaging: the aggregate jar is included first (Loom only accepts a module\n"
        "    //   component), then scripts/fix-1.20-fatjar.ps1 flattens the 39 module jars into\n"
        "    //   META-INF/jars and rewrites the fabric.mod.json jars array.\n"
        "    //   Reason: with two-level nesting (fat jar -> aggregate jar -> module jar) Fabric\n"
        "    //   Loader cannot see porting_lib_common (IPlantable etc.) and crashes with\n"
        "    //   NoClassDefFoundError on startup.\n"
    )
    s = s[:idx] + new_comment + s[end:]
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(s)
    print("build.gradle comments rewritten")
else:
    print("pattern not found", idx, end)

# README: add build prerequisites
readme = os.path.join(REPO, "README.md")
with open(readme, encoding="utf-8-sig") as f:
    s = f.read()
if "fetch-porting-lib" not in s:
    marker = "\n"
    note = (
        "\n## Building from source\n"
        "\n"
        "The 1.20.1 (main) branch compiles against Porting Lib module jars that are not\n"
        "checked in (they are extracted from the aggregate jar). Before the first build run:\n"
        "\n"
        "```powershell\n"
        "powershell -ExecutionPolicy Bypass -File scripts/fetch-porting-lib.ps1\n"
        "```\n"
        "\n"
        "This downloads `porting_lib-<porting_lib_version>.jar` (version read from\n"
        "`gradle.properties`) and extracts its 39 module jars into `libs/porting-lib/`.\n"
        "The release build then runs `scripts/fix-1.20-fatjar.ps1` so the shipped jar carries\n"
        "flat nested modules (Fabric Loader cannot load two-level nested jars).\n"
    )
    # insert before the last line or append
    s = s.rstrip() + "\n" + note
    with open(readme, "w", encoding="utf-8", newline="\n") as f:
        f.write(s)
    print("README updated")
else:
    print("README already documents the fetch step")
