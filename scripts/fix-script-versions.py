# -*- coding: utf-8 -*-
"""Unify version handling in build/release scripts and add the missing fatjar step.

All scripts read the version from gradle.properties instead of hardcoding it.
"""
import os

BASE = r"C:\Users\34324\Desktop"
REPOS = [r"C:\Users\34324\Desktop\Ultramarine-recast-fabric", r"C:\Users\34324\Desktop\um-main-wt"]

VERSION_READ = (
    "$root = Split-Path -Parent $PSScriptRoot\n"
    "$version = (Select-String -Path (Join-Path $root 'gradle.properties') -Pattern '^mod_version=(.+)$').Matches[0].Groups[1].Value.Trim()\n"
)

for repo in REPOS:
    scripts = os.path.join(repo, "scripts")
    if not os.path.isdir(scripts):
        continue

    # ---------- sync-release-wt.ps1 ----------
    p = os.path.join(scripts, "sync-release-wt.ps1")
    if os.path.exists(p):
        with open(p, encoding="utf-8") as f:
            s = f.read()
        if "gradle.properties" not in s:
            # insert version read after $ErrorActionPreference block
            s = s.replace(
                "$ErrorActionPreference = 'Stop'\n",
                "$ErrorActionPreference = 'Stop'\n" + VERSION_READ,
                1,
            )
        s = s.replace('("ultramarine-recast-fabric-{0}-0.7.0.1.jar" -f $entry.Value)',
                      '("ultramarine-recast-fabric-{0}-{1}.jar" -f $entry.Value, $version)')
        s = s.replace("'ultramarine-recast-fabric-1.20.1-0.7.0.jar'", "'ultramarine-recast-fabric-1.20.1-$version.jar'")
        s = s.replace("'ultramarine-recast-fabric-1.20-0.7.0.jar'", "'ultramarine-recast-fabric-1.20-$version.jar'")
        s = s.replace("'ultramarine-recast-fabric-26.1-0.7.0.jar'", "'ultramarine-recast-fabric-26.1-$version.jar'")
        s = s.replace("'ultramarine-recast-fabric-26.1.1-0.7.0.jar'", "'ultramarine-recast-fabric-26.1.1-$version.jar'")
        s = s.replace("'ultramarine-recast-fabric-26.1.2-0.7.0.jar'", "'ultramarine-recast-fabric-26.1.2-$version.jar'")
        # add the 1.21.8 variant copy + fatjar note after the variants block
        if "1.21.8" not in s:
            s = s.replace(
                "Write-Host 'variants synced'",
                "Copy-Item (Join-Path $release 'ultramarine-recast-fabric-1.21.1-$version.jar') (Join-Path $release 'ultramarine-recast-fabric-1.21-$version.jar') -Force\n"
                "Write-Host 'variants synced'\n"
                "Write-Host 'NOTE: run scripts/fix-1.20-fatjar.ps1 afterwards so the 1.20/1.20.1 jar ships flat Porting Lib modules'",
                1,
            )
        with open(p, "w", encoding="utf-8", newline="\n") as f:
            f.write(s)
        print("patched", os.path.relpath(p, BASE))

    # ---------- fix-1.20-fatjar.ps1 ----------
    p = os.path.join(scripts, "fix-1.20-fatjar.ps1")
    if os.path.exists(p):
        with open(p, encoding="utf-8") as f:
            s = f.read()
        if "gradle.properties" not in s:
            s = s.replace(
                "$Repo = Split-Path $PSScriptRoot -Parent\n",
                "$Repo = Split-Path $PSScriptRoot -Parent\n" + VERSION_READ,
                1,
            )
        s = s.replace('$FatJar = Join-Path $Repo "build\\libs\\ultramarine-recast-fabric-1.20.1-0.7.0.jar"',
                      '$FatJar = Join-Path $Repo "build\\libs\\ultramarine-recast-fabric-1.20.1-$version.jar"')
        with open(p, "w", encoding="utf-8", newline="\n") as f:
            f.write(s)
        print("patched", os.path.relpath(p, BASE))

    # ---------- fetch-porting-lib.ps1 ----------
    p = os.path.join(scripts, "fetch-porting-lib.ps1")
    if os.path.exists(p):
        with open(p, encoding="utf-8") as f:
            s = f.read()
        if "gradle.properties" not in s:
            s = s.replace(
                "$version = '2.3.8+1.20.1'",
                "$version = (Select-String -Path (Join-Path $root 'gradle.properties') -Pattern '^porting_lib_version=(.+)$').Matches[0].Groups[1].Value.Trim()",
            )
            # $root must be defined before $version: reorder
            s = s.replace(
                "$root = Split-Path -Parent $PSScriptRoot\n$version = (Select-String",
                "$root = Split-Path -Parent $PSScriptRoot\n$version = (Select-String",
            )
            # move $root above if still below
            if s.index("$root = Split-Path") > s.index("$version = (Select-String"):
                s = s.replace("$root = Split-Path -Parent $PSScriptRoot\n", "")
                s = s.replace(
                    "$version = (Select-String -Path (Join-Path $root 'gradle.properties')",
                    "$root = Split-Path -Parent $PSScriptRoot\n$version = (Select-String -Path (Join-Path $root 'gradle.properties')",
                    1,
                )
        with open(p, "w", encoding="utf-8", newline="\n") as f:
            f.write(s)
        print("patched", os.path.relpath(p, BASE))

    # ---------- sync-release.ps1 ----------
    p = os.path.join(scripts, "sync-release.ps1")
    if os.path.exists(p):
        with open(p, encoding="utf-8") as f:
            s = f.read()
        if "$version" not in s:
            s = s.replace(
                "$ErrorActionPreference = 'Stop'\n",
                "$ErrorActionPreference = 'Stop'\n" + VERSION_READ,
                1,
            )
        s = s.replace("-0.7.0.jar", "-$version.jar")
        s = s.replace("-0.7.0-sources.jar", "-$version-sources.jar")
        with open(p, "w", encoding="utf-8", newline="\n") as f:
            f.write(s)
        print("patched", os.path.relpath(p, BASE))

    # ---------- process-120-branch.ps1 ----------
    p = os.path.join(scripts, "process-120-branch.ps1")
    if os.path.exists(p):
        with open(p, encoding="utf-8") as f:
            s = f.read()
        if "$version" not in s:
            s = s.replace(
                "$ErrorActionPreference = 'Stop'\n",
                "$ErrorActionPreference = 'Stop'\n" + VERSION_READ,
                1,
            )
        s = s.replace('-0.7.0.jar"', '-$version.jar"')
        with open(p, "w", encoding="utf-8", newline="\n") as f:
            f.write(s)
        print("patched", os.path.relpath(p, BASE))

    # ---------- create-github-release.ps1 ----------
    p = os.path.join(scripts, "create-github-release.ps1")
    if os.path.exists(p):
        with open(p, encoding="utf-8") as f:
            s = f.read()
        if "$version" not in s:
            s = s.replace(
                "$ErrorActionPreference = 'Stop'\n",
                "$ErrorActionPreference = 'Stop'\n" + VERSION_READ,
                1,
            )
        s = s.replace("tag_name = 'v0.7.0.1'", "tag_name = \"v$version\"")
        s = s.replace("name = 'Ultramarine: Recast 0.7.0.1'", "name = \"Ultramarine: Recast $version\"")
        with open(p, "w", encoding="utf-8", newline="\n") as f:
            f.write(s)
        print("patched", os.path.relpath(p, BASE))
