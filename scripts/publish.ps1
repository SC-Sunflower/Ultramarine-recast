# publish.ps1
# One-command release pipeline:
#   1. (optional, -RebuildAll) rebuild every release branch
#   2. generate per-version jars into release/
#   3. deploy them into the matching PCL game instances
#
# Default flow assumes the affected branches were already built (fast):
#   powershell -ExecutionPolicy Bypass -File scripts/publish.ps1
# Full rebuild of every branch first (slow, ~10 min):
#   powershell -ExecutionPolicy Bypass -File scripts/publish.ps1 -RebuildAll
# Show what deployment would do without writing:
#   powershell -ExecutionPolicy Bypass -File scripts/publish.ps1 -DryRun

param(
    [switch]$RebuildAll,
    [switch]$DryRun
)

$Repo = "C:\Users\34324\Desktop\Ultramarine-recast-fabric"

Write-Output "======================================"
Write-Output " Ultramarine: Recast - publish pipeline"
Write-Output "======================================"

if ($RebuildAll) {
    Write-Output ""
    Write-Output "[1/3] rebuild all branches (slow) ..."
    & powershell -ExecutionPolicy Bypass -File (Join-Path $Repo "scripts\rebuild-all-branches.ps1")
}
else {
    Write-Output "[1/3] rebuild skipped (use -RebuildAll to rebuild every branch)"
}

Write-Output ""
Write-Output "[2/3] create per-version jars ..."
& powershell -ExecutionPolicy Bypass -File (Join-Path $Repo "scripts\make-versioned-jars.ps1")

Write-Output ""
Write-Output "[3/3] deploy to game instances ..."
if ($DryRun) {
    & powershell -ExecutionPolicy Bypass -File (Join-Path $Repo "scripts\deploy-to-instances.ps1") -DryRun
}
else {
    & powershell -ExecutionPolicy Bypass -File (Join-Path $Repo "scripts\deploy-to-instances.ps1")
}

Write-Output ""
Write-Output "done."
