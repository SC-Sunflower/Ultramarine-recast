# deploy-to-instances.ps1
# Copies the per-version jars from release/ into matching PCL game instances:
#   D:\PCL\.minecraft\versions\<instance>\mods\
#
# Selection rule: only instances whose mods/ already contains an ultramarine*.jar
# are updated (so other modpacks are never touched, and renaming instances is safe).
# The instance's real MC version is read from its json (clientVersion / inheritsFrom / id).
#
# Idempotent: deletes old ultramarine-*.jar in mods, then copies the matching new jar.
# Usage:
#   powershell -ExecutionPolicy Bypass -File scripts/deploy-to-instances.ps1
#   powershell -ExecutionPolicy Bypass -File scripts/deploy-to-instances.ps1 -DryRun

param(
    [switch]$DryRun
)

$Repo = "C:\Users\34324\Desktop\Ultramarine-recast-fabric"
$ReleaseDir = Join-Path $Repo "release"
$VersionsDir = "D:\PCL\.minecraft\versions"
$JarPrefix = "ultramarine-recast-fabric-"

if (-not (Test-Path $VersionsDir)) { Write-Output "ERROR: instance dir not found: $VersionsDir"; exit 1 }
if (-not (Test-Path $ReleaseDir)) { Write-Output "ERROR: release dir not found: $ReleaseDir"; exit 1 }

$candidates = @()
foreach ($dir in (Get-ChildItem $VersionsDir -Directory)) {
    $mods = Join-Path $dir.FullName "mods"
    if (-not (Test-Path $mods)) { continue }
    $modsJars = @(Get-ChildItem $mods -Filter "ultramarine*.jar" -ErrorAction SilentlyContinue)
    if ($modsJars.Count -eq 0) { continue }
    $candidates += $dir
}
$candidates = $candidates | Sort-Object Name
Write-Output ("instances with ultramarine: {0}" -f $candidates.Count)

$ok = 0; $skip = 0; $missing = @()
foreach ($inst in $candidates) {
    $jsonPath = Join-Path $inst.FullName ($inst.Name + ".json")
    if (-not (Test-Path $jsonPath)) { $jsonPath = Join-Path $inst.FullName "version.json" }
    if (-not (Test-Path $jsonPath)) {
        Write-Output ("[skip] {0} : no instance json" -f $inst.Name)
        $skip++
        continue
    }

    $mc = $null
    try {
        $j = Get-Content $jsonPath -Raw -Encoding UTF8 | ConvertFrom-Json
        foreach ($cand in @($j.clientVersion, $j.inheritsFrom, $j.id)) {
            if ($null -ne $cand) {
                $s = [string]$cand
                if ($s -match '^\d+\.\d+(\.\d+)?$') { $mc = $s; break }
            }
        }
    } catch {
        Write-Output ("[skip] {0} : json parse error" -f $inst.Name)
        $skip++
        continue
    }

    if (-not $mc) {
        Write-Output ("[skip] {0} : no MC version in json" -f $inst.Name)
        $skip++
        continue
    }

    $srcJar = Join-Path $ReleaseDir ($JarPrefix + $mc + "-0.7.0.jar")
    if (-not (Test-Path $srcJar)) {
        Write-Output ("[missing] {0} (MC {1}) : no release jar" -f $inst.Name, $mc)
        $missing += $mc
        continue
    }

    $modsDir = Join-Path $inst.FullName "mods"
    $old = @(Get-ChildItem $modsDir -Filter "ultramarine*.jar" -ErrorAction SilentlyContinue)
    $leaf = Split-Path $srcJar -Leaf
    $srcSize = [Math]::Round((Get-Item $srcJar).Length / 1MB, 2)

    if ($DryRun) {
        Write-Output ("[dry] {0} (MC {1}) : remove {2} old, copy {3} ({4} MB)" -f $inst.Name, $mc, $old.Count, $leaf, $srcSize)
        $ok++
        continue
    }

    foreach ($o in $old) { Remove-Item $o.FullName -Force }
    Copy-Item $srcJar (Join-Path $modsDir $leaf) -Force
    $stillOld = @(Get-ChildItem $modsDir -Filter "ultramarine*.jar" | Where-Object { $_.Name -ne $leaf })
    if ($stillOld.Count -gt 0) {
        Write-Output ("[warn] {0} : other ultramarine jars remain: {1}" -f $inst.Name, ($stillOld.Name -join ', '))
    }
    Write-Output ("[done] {0} (MC {1}) : {2} ({3} MB)" -f $inst.Name, $mc, $leaf, $srcSize)
    $ok++
}

Write-Output ""
Write-Output ("summary: ok {0}, skip {1}, missing {2}" -f $ok, $skip, $missing.Count)
if ($missing.Count -gt 0) { Write-Output ("missing versions: {0}" -f (($missing | Sort-Object -Unique) -join ', ')) }
if ($DryRun) { Write-Output "(dry run, nothing written)" }
