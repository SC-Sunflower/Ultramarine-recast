# fix-1.20-fatjar.ps1
# Rewrites the 1.20/1.20.1 fat jar so Porting Lib ships as flat nested mods
# (fat jar -> META-INF/jars/<module>.jar) instead of two-level nesting.
# Also patches each module's minecraft declaration "=1.20.1" -> ">=1.20 <1.20.2"
# so the jar loads on Minecraft 1.20 as well.
#
# Implementation: full extract -> patch files -> re-zip. In-place ZipArchive Update
# corrupted the jar for Java's strict ZipInputStream (invalid entry size).
#
# Usage: powershell -ExecutionPolicy Bypass -File scripts/fix-1.20-fatjar.ps1

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$Repo = Split-Path $PSScriptRoot -Parent
$FatJar = Join-Path $Repo "build\libs\ultramarine-recast-fabric-1.20.1-0.7.0.jar"
$LibDir = Join-Path $Repo "libs\porting-lib"

if (-not (Test-Path $FatJar)) { throw "fat jar not found: $FatJar" }

# Use the system temp directory instead of a hardcoded drive path so the
# script works on any machine / checkout location.
$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) 'umtmp'
New-Item -ItemType Directory -Force -Path $tempRoot | Out-Null
$work = Join-Path $tempRoot ("umfat-" + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path $work | Out-Null

# Re-zip a directory into a jar with forward slashes (required by zip spec).
function New-JarFromDirectory([string]$dir, [string]$jarPath) {
    if (Test-Path $jarPath) { Remove-Item $jarPath -Force }
    $zip = [System.IO.Compression.ZipFile]::Open($jarPath, [System.IO.Compression.ZipArchiveMode]::Create)
    try {
        Get-ChildItem $dir -Recurse -File | ForEach-Object {
            $rel = $_.FullName.Substring($dir.Length + 1) -replace '\\', '/'
            [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, $rel) | Out-Null
        }
    }
    finally { $zip.Dispose() }
}

try {
    # 1. extract the fat jar
    $fatDir = Join-Path $work 'fat'
    [System.IO.Compression.ZipFile]::ExtractToDirectory($FatJar, $fatDir)
    Write-Output "extracted fat jar"

    # 2. drop every existing nested jar
    $jarsDir = Join-Path $fatDir 'META-INF\jars'
    if (Test-Path $jarsDir) { Remove-Item $jarsDir -Recurse -Force }
    New-Item -ItemType Directory -Force -Path $jarsDir | Out-Null
    Write-Output "cleared META-INF/jars"

    # 3. for each module: extract, patch fabric.mod.json, re-zip into META-INF/jars
    $patchPattern = '"minecraft"\s*:\s*"(?:\\u003d|=|>=|>)[^"]*"'
    $fabricApiPattern = '"fabric-api"\s*:\s*"[^"]*"'
    $moduleJars = Get-ChildItem $LibDir -Filter *.jar | Sort-Object Name
    foreach ($m in $moduleJars) {
        $mdir = Join-Path $work ("m-" + [guid]::NewGuid().ToString('N'))
        [System.IO.Compression.ZipFile]::ExtractToDirectory($m.FullName, $mdir)
        $fmj = Join-Path $mdir 'fabric.mod.json'
        if (Test-Path $fmj) {
            $t = [System.IO.File]::ReadAllText($fmj)
            $n = [regex]::Replace($t, $patchPattern, '"minecraft": "*"')
            $n = [regex]::Replace($n, $fabricApiPattern, '"fabric-api": ">=0.83.0+1.20"')
            if ($n -ne $t) {
                [System.IO.File]::WriteAllText($fmj, $n, (New-Object System.Text.UTF8Encoding($false)))
            }
        }
        New-JarFromDirectory -dir $mdir -jarPath (Join-Path $jarsDir $m.Name)
        Remove-Item $mdir -Recurse -Force
    }
    Write-Output ("flattened {0} module jars (declarations patched)" -f $moduleJars.Count)

    # 4. rewrite the top-level fabric.mod.json "jars" array
    $topFmj = Join-Path $fatDir 'fabric.mod.json'
    $json = [System.IO.File]::ReadAllText($topFmj)
    $jarEntries = ($moduleJars | ForEach-Object { '      {"file": "META-INF/jars/' + $_.Name + '"}' }) -join ",`r`n"
    $jarsBlock = "  `"jars`": [`r`n" + $jarEntries + "`r`n  ],"
    if ($json -match '"jars"\s*:\s*\[') {
        $json = [regex]::Replace($json, '"jars"\s*:\s*\[[\s\S]*?\]', $jarsBlock.TrimEnd(','), 1)
    } else {
        $json = $json -replace '(\s*)(\}\s*$)', ("`r`n" + $jarsBlock + "`r`n" + '$2')
    }
    [System.IO.File]::WriteAllText($topFmj, $json, (New-Object System.Text.UTF8Encoding($false)))
    $declared = ([regex]::Matches($json, '"file": "META-INF/jars/')).Count
    Write-Output ("fabric.mod.json jars entries: {0}" -f $declared)

    # 5. re-zip the fat jar
    $outJar = Join-Path $work 'out.jar'
    New-JarFromDirectory -dir $fatDir -jarPath $outJar
    Copy-Item $outJar $FatJar -Force
    Write-Output ("fat jar rebuilt: {0} MB" -f [Math]::Round((Get-Item $FatJar).Length / 1MB, 2))
}
finally {
    Remove-Item $work -Recurse -Force -ErrorAction SilentlyContinue
}
