# Patches the built jar so it also loads on Minecraft 1.20.
#
# Why: 1.20 and 1.20.1 share identical intermediary mappings (verified: 8443 mapping
# entries, 0 differences), so the 1.20.1 build is binary-compatible with 1.20. The only
# blocker is version declarations: the bundled Porting Lib modules and the nested
# porting_lib jar declare `minecraft = 1.20.1`, which Fabric Loader treats as a hard
# dependency. This script rewrites those declarations to `>=1.20 <1.20.2` in the final
# jar (the top-level fabric.mod.json is already version-agnostic).
#
# Usage:  powershell -ExecutionPolicy Bypass -File scripts/patch-mc-version.ps1 [-JarPath <path>]
#         (defaults to build/libs/ultramarine-recast-fabric-1.20.1-*.jar without -sources)

param(
    [string]$JarPath = ""
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$root = Split-Path -Parent $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($JarPath)) {
    $JarPath = Get-ChildItem (Join-Path $root 'build/libs') -Filter '*.jar' |
        Where-Object { $_.Name -notmatch 'sources' } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1 -ExpandProperty FullName
}
if (-not $JarPath -or -not (Test-Path $JarPath)) {
    throw "Jar not found. Pass -JarPath explicitly."
}

Write-Host "Patching $JarPath"

# Short temp root on the drive root: nested Porting Lib package names are deep enough to
# exceed the 260-character Windows path limit inside %TEMP%.
$tempRoot = 'C:\umtmp'
New-Item -ItemType Directory -Force -Path $tempRoot | Out-Null
$temp = Join-Path $tempRoot ("um-" + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path $temp | Out-Null
[System.IO.Compression.ZipFile]::ExtractToDirectory($JarPath, $temp)

$patched = 0
# Gson escapes '=' as \u003d, so match both forms of the exact 1.20.1 declaration.
$pattern = '"minecraft"\s*:\s*"(?:\\u003d|=)1\.20\.1"'

function Patch-FabricModJson([string]$path) {
    if (-not (Test-Path $path)) { return }
    $text = [System.IO.File]::ReadAllText($path)
    $new = [regex]::Replace($text, $pattern, '"minecraft": ">=1.20 <1.20.2"')
    if ($new -ne $text) {
        [System.IO.File]::WriteAllText($path, $new)
        $script:patched++
    }
}

# Re-zip a directory into a jar using forward slashes, which jar/ZIP paths require.
# (ZipFile.CreateFromDirectory on Windows .NET Framework writes backslashes and corrupts jars.)
function New-JarFromDirectory([string]$dir, [string]$jarPath) {
    if (Test-Path $jarPath) { Remove-Item $jarPath -Force }
    $zip = [System.IO.Compression.ZipFile]::Open($jarPath, [System.IO.Compression.ZipArchiveMode]::Create)
    try {
        Get-ChildItem $dir -Recurse -File | ForEach-Object {
            $rel = $_.FullName.Substring($dir.Length + 1) -replace '\\', '/'
            [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, $rel) | Out-Null
        }
    } finally {
        $zip.Dispose()
    }
}

# 1. top-level fabric.mod.json (already >=1.20 <1.20.2, but keep idempotent)
Patch-FabricModJson (Join-Path $temp 'fabric.mod.json')

# 2. nested jars: META-INF/jars/*.jar -> fabric.mod.json inside each
$jarsDir = Join-Path $temp 'META-INF/jars'
if (Test-Path $jarsDir) {
    foreach ($jarFile in Get-ChildItem $jarsDir -Filter '*.jar') {
        $inner = Join-Path $temp ("inner-" + [guid]::NewGuid().ToString('N'))
        New-Item -ItemType Directory -Force -Path $inner | Out-Null
        [System.IO.Compression.ZipFile]::ExtractToDirectory($jarFile.FullName, $inner)

        Patch-FabricModJson (Join-Path $inner 'fabric.mod.json')

        # 3. one more nesting level (porting_lib_config bundles night-config, etc.)
        $innerJars = Join-Path $inner 'META-INF/jars'
        if (Test-Path $innerJars) {
            foreach ($deepJar in Get-ChildItem $innerJars -Filter '*.jar') {
                $deep = Join-Path $inner ("deep-" + [guid]::NewGuid().ToString('N'))
                New-Item -ItemType Directory -Force -Path $deep | Out-Null
                [System.IO.Compression.ZipFile]::ExtractToDirectory($deepJar.FullName, $deep)
                Patch-FabricModJson (Join-Path $deep 'fabric.mod.json')
                New-JarFromDirectory $deep $deepJar.FullName
                Remove-Item $deep -Recurse -Force
            }
        }

        New-JarFromDirectory $inner $jarFile.FullName
        Remove-Item $inner -Recurse -Force
    }
}

# Rebuild the outer jar
New-JarFromDirectory $temp $JarPath
Remove-Item $temp -Recurse -Force

Write-Host "Done. Patched fabric.mod.json files: $patched"
