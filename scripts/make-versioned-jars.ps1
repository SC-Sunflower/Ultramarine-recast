# make-versioned-jars.ps1
# Produces one jar per Minecraft game version (independent file names) from the jars
# collected in build/collected, and rewrites the "minecraft" dependency inside
# fabric.mod.json to that exact version.
# Usage (from repo root): powershell -ExecutionPolicy Bypass -File scripts/make-versioned-jars.ps1

$ErrorActionPreference = 'Stop'
$Repo = "C:\Users\34324\Desktop\Ultramarine-recast-fabric"
$OutDir = Join-Path $Repo "release"
$BaseDir = Join-Path $Repo "build\collected"
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

# base jar name in build/collected -> target game versions
$plan = [ordered]@{
    "ultramarine-recast-fabric-1.20.1-0.7.0.jar"  = @("1.20", "1.20.1")
    "ultramarine-recast-fabric-1.20.2-0.7.0.jar"  = @("1.20.2")
    "ultramarine-recast-fabric-1.20.3-0.7.0.jar"  = @("1.20.3")
    "ultramarine-recast-fabric-1.20.4-0.7.0.jar"  = @("1.20.4")
    "ultramarine-recast-fabric-1.20.5-0.7.0.jar"  = @("1.20.5")
    "ultramarine-recast-fabric-1.20.6-0.7.0.jar"  = @("1.20.6")
    "ultramarine-recast-fabric-1.21.1-0.7.0.jar"  = @("1.21", "1.21.1")
    "ultramarine-recast-fabric-1.21.2-0.7.0.jar"  = @("1.21.2", "1.21.3")
    "ultramarine-recast-fabric-1.21.4-0.7.0.jar"  = @("1.21.4")
    "ultramarine-recast-fabric-1.21.5-0.7.0.jar"  = @("1.21.5")
    "ultramarine-recast-fabric-1.21.6-0.7.0.jar"  = @("1.21.6")
    "ultramarine-recast-fabric-1.21.7-0.7.0.jar"  = @("1.21.7", "1.21.8")
    "ultramarine-recast-fabric-1.21.9-0.7.0.jar"  = @("1.21.9")
    "ultramarine-recast-fabric-1.21.10-0.7.0.jar" = @("1.21.10")
    "ultramarine-recast-fabric-1.21.11-0.7.0.jar" = @("1.21.11")
}

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

function Set-MinecraftVersionInJar {
    param([string]$JarPath, [string]$Version)
    $zip = [System.IO.Compression.ZipFile]::Open($JarPath, [System.IO.Compression.ZipArchiveMode]::Update)
    try {
        $entry = $zip.GetEntry("fabric.mod.json")
        $sr = New-Object System.IO.StreamReader($entry.Open())
        $json = $sr.ReadToEnd()
        $sr.Close()
        $original = $json
        $json = [regex]::Replace($json, '("minecraft"\s*:\s*")[^"]*(")', ('${1}' + $Version + '${2}'))
        if ($json -ne $original) {
            $entry.Delete()
            $newEntry = $zip.CreateEntry("fabric.mod.json")
            $sw = New-Object System.IO.StreamWriter($newEntry.Open())
            $sw.Write($json)
            $sw.Close()
            return $true
        }
        return $false
    }
    finally {
        $zip.Dispose()
    }
}

$report = @()
foreach ($baseName in $plan.Keys) {
    $versions = $plan[$baseName]
    $baseJar = Join-Path $BaseDir $baseName
    if (-not (Test-Path $baseJar)) {
        Write-Warning ("base jar missing: {0}" -f $baseName)
        continue
    }
    foreach ($v in $versions) {
        $outJar = Join-Path $OutDir ("ultramarine-recast-fabric-" + $v + "-0.7.0.jar")
        Copy-Item $baseJar $outJar -Force
        $patched = Set-MinecraftVersionInJar -JarPath $outJar -Version $v
        $zip2 = [System.IO.Compression.ZipFile]::OpenRead($outJar)
        $e2 = $zip2.GetEntry("fabric.mod.json")
        $sr2 = New-Object System.IO.StreamReader($e2.Open())
        $check = $sr2.ReadToEnd()
        $sr2.Close()
        $zip2.Dispose()
        $minecraftLine = ($check -split "`n") | Where-Object { $_ -match '"minecraft"' } | Select-Object -First 1
        $size = [Math]::Round((Get-Item $outJar).Length / 1MB, 2)
        $declared = if ($minecraftLine) { $minecraftLine.Trim() } else { "?" }
        $report += ("{0} : {1} MB, {2}, patched={3}" -f $v, $size, $declared, $patched)
        Write-Output ("produced {0}: {1} MB ({2})" -f $v, $size, $declared)
    }
}

Write-Output ""
Write-Output "== summary =="
$report | ForEach-Object { Write-Output $_ }
Write-Output ""
$count = (Get-ChildItem $OutDir -Filter *.jar | Measure-Object).Count
Write-Output ("release dir: {0}, {1} jars" -f $OutDir, $count)
