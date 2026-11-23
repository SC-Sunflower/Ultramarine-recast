# rebuild-all-branches.ps1
# Rebuilds every release branch (dependencies are cached, so each build is fast)
# and collects their jars into build/collected for the versioning script.

$ErrorActionPreference = 'Continue'
$Repo = "C:\Users\34324\Desktop\Ultramarine-recast-fabric"
$Gradle = "C:\Users\34324\.gradle\wrapper\dists\gradle-9.4.1-bin\45w7kj7s8jzqenl33wrwa0aoj\gradle-9.4.1\bin\gradle.bat"
$Collect = Join-Path $Repo "build\collected"
New-Item -ItemType Directory -Force -Path $Collect | Out-Null

$branches = @(
    "main",
    "fabric/1.20.2",
    "fabric/1.20.3",
    "fabric/1.20.4",
    "fabric/1.20.5",
    "fabric/1.20.6",
    "fabric/1.21.1",
    "fabric/1.21.2",
    "fabric/1.21.4",
    "fabric/1.21.5",
    "fabric/1.21.6",
    "fabric/1.21.7",
    "fabric/1.21.9",
    "fabric/1.21.10"
)

foreach ($branch in $branches) {
    git -C $Repo checkout --quiet $branch 2>$null
    Write-Output ("=== {0} ===" -f $branch)
    & $Gradle -p $Repo build --console=plain --no-build-cache 2>&1 | Select-String -Pattern "BUILD SUCCESSFUL|BUILD FAILED|error:" | Select-Object -First 3 | ForEach-Object { $_.Line }
    if ($branch -eq "main") {
        # 1.20 support: patch bundled Porting Lib minecraft declarations
        powershell -ExecutionPolicy Bypass -File "$Repo\scripts\patch-mc-version.ps1" 2>&1 | Select-Object -Last 1
    }
    Get-ChildItem "$Repo\build\libs" -Filter "*.jar" | Where-Object { $_.Name -notmatch 'sources' } | ForEach-Object {
        Copy-Item $_.FullName (Join-Path $Collect $_.Name) -Force
        Write-Output ("  collected {0}" -f $_.Name)
    }
}

Write-Output "=== done ==="
Get-ChildItem $Collect -Filter *.jar | ForEach-Object { Write-Output ("  {0}" -f $_.Name) }
