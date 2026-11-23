# Fetches Porting Lib 2.3.8 and unpacks its nested module jars into libs/porting-lib.
#
# Why: the Modrinth/CurseForge distribution of Porting Lib is a "parent" jar whose
# module jars live under META-INF/jars/. Fabric Loom only puts the parent jar itself
# on the compile classpath, so the nested modules are extracted to libs/ and included
# via `fileTree(dir: 'libs/porting-lib')` in build.gradle.
#
# Usage:  powershell -File scripts/fetch-porting-lib.ps1

$ErrorActionPreference = 'Stop'

$version = '2.3.8+1.20.1'
$url = 'https://cdn.modrinth.com/data/tEJPA4Ty/versions/c8mvvgoR/porting_lib-2.3.8%2B1.20.1.jar'
$root = Split-Path -Parent $PSScriptRoot
$out = Join-Path $root 'libs/porting-lib'
$tmp = Join-Path $env:TEMP "porting_lib-$version.jar"

Write-Host "Downloading Porting Lib $version ..."
Invoke-WebRequest -Uri $url -OutFile $tmp

New-Item -ItemType Directory -Force -Path $out | Out-Null

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($tmp)
$count = 0
foreach ($entry in $zip.Entries) {
    if ($entry.FullName -like 'META-INF/jars/*.jar') {
        $name = [System.IO.Path]::GetFileName($entry.FullName)
        $dest = Join-Path $out $name
        $src = $entry.Open()
        $fs = [System.IO.File]::Create($dest)
        $src.CopyTo($fs)
        $fs.Close()
        $src.Close()
        $count++
    }
}
$zip.Dispose()
Remove-Item $tmp -Force

Write-Host "Extracted $count module jars to $out"
