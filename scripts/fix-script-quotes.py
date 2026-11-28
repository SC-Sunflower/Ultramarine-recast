# -*- coding: utf-8 -*-
"""Fix single-quoted paths that contain a literal $version in release scripts."""
import os
import re

REPOS = [r"C:\Users\34324\Desktop\Ultramarine-recast-fabric", r"C:\Users\34324\Desktop\um-main-wt"]

pat = re.compile(r"'([^']*?)-\$version\.jar'")

for repo in REPOS:
    for name in ("sync-release.ps1", "sync-release-wt.ps1", "process-120-branch.ps1", "fix-1.20-fatjar.ps1"):
        p = os.path.join(repo, "scripts", name)
        if not os.path.exists(p):
            continue
        with open(p, encoding="utf-8-sig") as f:
            s = f.read()
        s2 = pat.subn(lambda m: '"' + m.group(1) + '-$version.jar"', s)[0]
        if s2 != s:
            with open(p, "w", encoding="utf-8", newline="\n") as f:
                f.write(s2)
            print("fixed", os.path.basename(repo), name)
