# git-push-all.ps1
# Pushes every local branch to GitHub (origin). First push sets upstream tracking.
#
# Usage (run from repo root):
#   powershell -ExecutionPolicy Bypass -File scripts/git-push-all.ps1
#   powershell -ExecutionPolicy Bypass -File scripts/git-push-all.ps1 -Force   # overwrite remote history
#   powershell -ExecutionPolicy Bypass -File scripts/git-push-all.ps1 -Branch fabric/1.21.11  # single branch

param(
    [switch]$Force,
    [string]$Branch = ""
)

$Repo = "C:\Users\34324\Desktop\Ultramarine-recast-fabric"

if ($Branch) {
    $branches = @($Branch)
}
else {
    $branches = git -C $Repo branch --format "%(refname:short)"
}

$ok = 0; $fail = 0
foreach ($b in $branches) {
    if ([string]::IsNullOrWhiteSpace($b)) { continue }
    Write-Output ("pushing {0} ..." -f $b)
    $args = @("push")
    if ($Force) { $args += "--force" }
    $args += @("-u", "origin", $b)
    $out = & git -C $Repo @args 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Output ("  ok: {0}" -f $b)
        $ok++
    }
    else {
        Write-Output ("  FAILED: {0}" -f $b)
        $out | Select-Object -First 3 | ForEach-Object { Write-Output ("    " + $_) }
        $fail++
    }
}
Write-Output ""
Write-Output ("summary: ok {0}, failed {1}" -f $ok, $fail)
