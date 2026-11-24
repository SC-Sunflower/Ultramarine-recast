# Builds every Minecraft version branch and syncs the jars into release/,
# one jar file per Minecraft version (strict per-version files, even where two
# versions share a jar: 1.20/1.20.1 and 1.21/1.21.1 are still two files).
#
# Usage:
#   powershell -ExecutionPolicy Bypass -File scripts/sync-release.ps1            # full build + sync
#   powershell -ExecutionPolicy Bypass -File scripts/sync-release.ps1 -OnlySync  # sync existing build/libs jars only
#
# Variant jars:
#   1.20   <- same jar as 1.20.1 after fix-1.20-fatjar.ps1 (flat modules, minecraft "*")
#   1.21   <- copy of the 1.21.1 jar (fabric.mod.json range already covers 1.21.0)
#   1.21.3 <- copy of the 1.21.2 jar (same datapack formats, range covers 1.21.3)
#   1.21.8 <- NOT automated: fabric/1.21.9 code uses 1.21.9-only APIs. See
#             scripts/notes-1.21.8-build.md for the manual steps.
$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$release = Join-Path $root 'release'
$buildsRoot = Join-Path $root '.release-builds'
$gradle = 'C:\Users\34324\.gradle\wrapper\dists\gradle-9.4.1-bin\45w7kj7s8jzqenl33wrwa0aoj\gradle-9.4.1\bin\gradle.bat'
if (-not (Test-Path $gradle)) {
    throw "Cached Gradle 9.4.1 not found at $gradle"
}

$matrix = @(
    @{ Branch = 'main';           MC = '1.20.1' },
    @{ Branch = 'fabric/1.20.2'; MC = '1.20.2' },
    @{ Branch = 'fabric/1.20.3'; MC = '1.20.3' },
    @{ Branch = 'fabric/1.20.4'; MC = '1.20.4' },
    @{ Branch = 'fabric/1.20.5'; MC = '1.20.5' },
    @{ Branch = 'fabric/1.20.6'; MC = '1.20.6' },
    @{ Branch = 'fabric/1.21.1'; MC = '1.21.1' },
    @{ Branch = 'fabric/1.21.2'; MC = '1.21.2' },
    @{ Branch = 'fabric/1.21.4'; MC = '1.21.4' },
    @{ Branch = 'fabric/1.21.5'; MC = '1.21.5' },
    @{ Branch = 'fabric/1.21.6'; MC = '1.21.6' },
    @{ Branch = 'fabric/1.21.7'; MC = '1.21.7' },
    @{ Branch = 'fabric/1.21.9'; MC = '1.21.9' },
    @{ Branch = 'fabric/1.21.10'; MC = '1.21.10' },
    @{ Branch = 'fabric/1.21.11'; MC = '1.21.11' }
)

New-Item -ItemType Directory -Force -Path $buildsRoot, $release | Out-Null
Push-Location $root

foreach ($entry in $matrix) {
    $branch = $entry.Branch
    $mc = $entry.MC
    $dirName = $branch -replace '/', '_'
    $wt = Join-Path $buildsRoot $dirName

    # Register the worktree if missing (resolves missing-worktree errors after a
    # previous run was interrupted). Never touches the main checkout's branch.
    $registered = git worktree list --porcelain | Select-String -SimpleMatch $wt
    if (-not $registered) {
        git worktree add $wt $branch 2>$null | Out-Null
        if ($LASTEXITCODE -ne 0) {
            Write-Warning "worktree add failed for $branch (maybe already exists on disk)"
        }
    }
    if (-not (Test-Path (Join-Path $wt 'gradlew.bat'))) {
        Write-Warning "Worktree missing for $branch, skipping"
        continue
    }

    Push-Location $wt
    try {
        # main bundles Porting Lib; fetch the module jars once if absent.
        if ($branch -eq 'main') {
            $pl = Join-Path $wt 'libs/porting-lib'
            if (-not (Test-Path $pl) -or (Get-ChildItem $pl -Filter '*.jar' -ErrorAction SilentlyContinue | Measure-Object).Count -lt 10) {
                Write-Host "[$mc] fetching Porting Lib ..."
                & powershell -ExecutionPolicy Bypass -File (Join-Path $wt 'scripts/fetch-porting-lib.ps1')
            }
        }

        if ($OnlySync -ne $true) {
            Write-Host "[$mc] building $branch ..."
            & $gradle build --console=plain --no-daemon
            if ($LASTEXITCODE -ne 0) { throw "[$mc] build failed" }
        }

        $jar = Get-ChildItem (Join-Path $wt 'build/libs') -Filter '*.jar' |
            Where-Object { $_.Name -notmatch 'sources' } |
            Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if (-not $jar) { throw "[$mc] no jar produced" }

        $dest = Join-Path $release ("ultramarine-recast-fabric-$mc-0.7.0.jar")
        Copy-Item $jar.FullName $dest -Force
        Write-Host "[$mc] synced -> $dest"
    } finally {
        Pop-Location
    }
}

# --- variants ---
# 1.20/1.20.1: the main-branch jar must be flattened by fix-1.20-fatjar.ps1 (Loader
# cannot see classes two nesting levels deep). The script rewrites the 39 bundled
# module jars flat into META-INF/jars and patches each module's minecraft declaration
# to "*", so the SAME jar loads on both 1.20 and 1.20.1.
$r1201 = Join-Path $release 'ultramarine-recast-fabric-1.20.1-0.7.0.jar'
if (Test-Path $r1201) {
    $libDir = Join-Path $root 'libs/porting-lib'
    if (-not (Test-Path $libDir) -or (Get-ChildItem $libDir -Filter '*.jar' -ErrorAction SilentlyContinue | Measure-Object).Count -lt 39) {
        & powershell -ExecutionPolicy Bypass -File (Join-Path $root 'scripts/fetch-porting-lib.ps1')
    }
    $buildLibs = Join-Path $root 'build/libs'
    New-Item -ItemType Directory -Force -Path $buildLibs | Out-Null
    Copy-Item $r1201 (Join-Path $buildLibs 'ultramarine-recast-fabric-1.20.1-0.7.0.jar') -Force
    & powershell -ExecutionPolicy Bypass -File (Join-Path $root 'scripts/fix-1.20-fatjar.ps1')
    Copy-Item (Join-Path $buildLibs 'ultramarine-recast-fabric-1.20.1-0.7.0.jar') $r1201 -Force
    Copy-Item $r1201 (Join-Path $release 'ultramarine-recast-fabric-1.20-0.7.0.jar') -Force
    Write-Host '[1.20/1.20.1] fatjar flattened (39 modules, minecraft declaration "*")'
}

$r1211 = Join-Path $release 'ultramarine-recast-fabric-1.21.1-0.7.0.jar'
if (Test-Path $r1211) {
    Copy-Item $r1211 (Join-Path $release 'ultramarine-recast-fabric-1.21-0.7.0.jar') -Force
    Write-Host '[1.21] copied from 1.21.1 jar'
}

$r1212 = Join-Path $release 'ultramarine-recast-fabric-1.21.2-0.7.0.jar'
if (Test-Path $r1212) {
    Copy-Item $r1212 (Join-Path $release 'ultramarine-recast-fabric-1.21.3-0.7.0.jar') -Force
    Write-Host '[1.21.3] copied from 1.21.2 jar'
}

$r1218 = Join-Path $release 'ultramarine-recast-fabric-1.21.8-0.7.0.jar'
if (-not (Test-Path $r1218)) {
    Write-Warning '[1.21.8] jar missing: build it manually per scripts/notes-1.21.8-build.md'
}

Pop-Location
Write-Host ''
Write-Host 'Release dir now contains:'
Get-ChildItem $release -Filter '*.jar' | Sort-Object Name | ForEach-Object {
    Write-Host ("  {0}  {1,10:N0} bytes  {2}" -f $_.Name, $_.Length, $_.LastWriteTime.ToString('yyyy-MM-dd HH:mm:ss'))
}
