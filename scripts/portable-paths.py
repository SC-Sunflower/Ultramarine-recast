# -*- coding: utf-8 -*-
"""Make dev helper scripts portable: replace personal absolute paths with
script-relative paths / environment variables so the public repo has no
machine-specific paths."""
import re
from pathlib import Path

BASE = Path(__file__).resolve().parents[1]
SCRIPTS = BASE / "scripts"

ROOT = re.escape(r"C:\Users\34324\Desktop\Ultramarine-recast-fabric")
UPSTREAM = re.escape(r"C:\Users\34324\Desktop\Ultramarine-upstream")
GRADLE = re.escape(r"C:\Users\34324\.gradle")
TEMP = re.escape(r"C:\Users\34324\AppData\Local\Temp")
DESKTOP_UM = re.escape(r"C:\Users\34324\Desktop\um-")  # um-tabs / um-sync / um-1218
USER = re.escape(r"C:\Users\34324")


def patch_py(text):
    # PORT / BASE / REPO roots -> repo root relative to the script
    text = re.sub(r'Path\(r"' + ROOT + r'"\)', 'Path(__file__).resolve().parents[1]', text)
    text = re.sub(r'Path\(r"' + ROOT + r'\\src\\main\\resources\\data"\)',
                  'Path(__file__).resolve().parents[1] / "src/main/resources/data"', text)
    text = re.sub(r'r"' + ROOT + r'\\src\\main\\resources\\data"',
                  'str(Path(__file__).resolve().parents[1] / "src/main/resources/data")', text)
    text = re.sub(r'repr.*', lambda m: m.group(0), text)  # no-op guard
    # upstream repo (sibling of the recast checkout)
    text = text.replace(
        'Path(r"' + UPSTREAM + r'")',
        'Path(__file__).resolve().parents[1].parent / "Ultramarine-upstream"')
    text = text.replace(
        'r"' + UPSTREAM + r'\\src\\main\\java\\com\\voxelutopia\\ultramarine\\data\\registry\\ItemRegistry.java"',
        'str(Path(__file__).resolve().parents[1].parent / "Ultramarine-upstream/src/main/java/com/voxelutopia/ultramarine/data/registry/ItemRegistry.java")')
    # temp files
    text = text.replace('r"' + TEMP + r'\\ultramarine-0.7.jar"', 'str(Path(__file__).resolve().parents[1].parent / "Ultramarine-upstream-build" / "ultramarine-0.7.jar")')
    # desktop um-* work trees -> temp dir
    text = text.replace('r"' + DESKTOP_UM, 'r"${TEMP_UM}')
    return text


def patch_ps1(text):
    text = text.replace('$Repo = "' + r"C:\Users\34324\Desktop\Ultramarine-recast-fabric" + '"',
                        '$Repo = Split-Path $PSScriptRoot -Parent')
    text = text.replace("'" + r"C:\Users\34324\.gradle\wrapper\dists\gradle-9.4.1-bin\45w7kj7s8jzqenl33wrwa0aoj\gradle-9.4.1\bin\gradle.bat" + "'",
                        'Join-Path $env:USERPROFILE ".gradle\\wrapper\\dists\\gradle-9.4.1-bin\\45w7kj7s8jzqenl33wrwa0aoj\\gradle-9.4.1\\bin\\gradle.bat"')
    text = text.replace('"' + r"C:\Users\34324\Desktop\Ultramarine-recast-fabric\release" + '"',
                        'Join-Path (Split-Path $PSScriptRoot -Parent) "release"')
    return text


def patch_md(text):
    text = text.replace(r"C:\Users\34324\Desktop\um-1218", "$env:TEMP\\um-1218")
    return text


for p in sorted(SCRIPTS.iterdir()):
    if p.suffix not in (".py", ".ps1", ".md"):
        continue
    text = p.read_text(encoding="utf-8", errors="replace")
    orig = text
    if p.suffix == ".py":
        text = patch_py(text)
    elif p.suffix == ".ps1":
        text = patch_ps1(text)
    else:
        text = patch_md(text)
    if text != orig:
        p.write_text(text, encoding="utf-8")
        print(f"patched {p.name}")
print("done")
