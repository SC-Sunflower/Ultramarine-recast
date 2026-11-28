# Copy each worktree's newest jar into the main repo's release/ directory.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$version = (Select-String -Path (Join-Path $root 'gradle.properties') -Pattern '^mod_version=(.+)$').Matches[0].Groups[1].Value.Trim()
$release = 'C:\Users\34324\Desktop\Ultramarine-recast-fabric\release'

$map = @{
    'um-main-wt'    = '1.20.1'
    'um-1202-wt'    = '1.20.2'
    'um-1203-wt'    = '1.20.3'
    'um-1204-wt'    = '1.20.4'
    'um-1205-wt'    = '1.20.5'
    'um-1206-wt'    = '1.20.6'
    'um-1211-wt'    = '1.21.1'
    'um-1214-wt'    = '1.21.4'
    'um-1215-wt'    = '1.21.5'
    'um-1216-wt'    = '1.21.6'
    'um-1217-wt'    = '1.21.7'
    'um-1219-wt'    = '1.21.9'
    'um-12110-wt'   = '1.21.10'
    'um-12111-wt'   = '1.21.11'
    'um-26.1-wt'    = '26.1'
    'um-26.2-wt'    = '26.2'
    'um-26.3-wt'    = '26.3'
}

foreach ($entry in $map.GetEnumerator() | Sort-Object Value) {
    $wt = Join-Path 'C:\Users\34324\Desktop' $entry.Key
    $jar = Get-ChildItem (Join-Path $wt 'build\libs') -Filter '*.jar' -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch 'sources' } |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $jar) { Write-Warning "no jar for $($entry.Key)"; continue }
    $dest = Join-Path $release ("ultramarine-recast-fabric-{0}-{1}.jar" -f $entry.Value, $version)
    Copy-Item $jar.FullName $dest -Force
    Write-Host ("{0} -> {1}" -f $entry.Value, $jar.Name)
}

# 1.20.1 jar also serves 1.20; 26.1 jar serves 26.1.1/26.1.2
Copy-Item (Join-Path $release "ultramarine-recast-fabric-1.20.1-$version.jar") (Join-Path $release "ultramarine-recast-fabric-1.20-$version.jar") -Force
Copy-Item (Join-Path $release "ultramarine-recast-fabric-26.1-$version.jar") (Join-Path $release "ultramarine-recast-fabric-26.1.1-$version.jar") -Force
Copy-Item (Join-Path $release "ultramarine-recast-fabric-26.1-$version.jar") (Join-Path $release "ultramarine-recast-fabric-26.1.2-$version.jar") -Force
Copy-Item (Join-Path $release "ultramarine-recast-fabric-1.21.1-$version.jar") (Join-Path $release "ultramarine-recast-fabric-1.21-$version.jar") -Force
Write-Host 'variants synced'
Write-Host 'NOTE: run scripts/fix-1.20-fatjar.ps1 afterwards so the 1.20/1.20.1 jar ships flat Porting Lib modules'
