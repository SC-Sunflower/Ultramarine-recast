$gradle = "$env:USERPROFILE\.gradle\wrapper\dists\gradle-9.4.1-bin\45w7kj7s8jzqenl33wrwa0aoj\gradle-9.4.1\bin\gradle.bat"
$wts = @(
  'C:\Users\34324\Desktop\um-26.1-wt',
  'C:\Users\34324\Desktop\um-26.2-wt',
  'C:\Users\34324\Desktop\um-26.3-wt'
)
foreach ($wt in $wts) {
    $name = Split-Path $wt -Leaf
    Write-Host "=== building $name ==="
    Push-Location $wt
    & $gradle build --console=plain --no-daemon 2>&1 | Select-Object -Last 3
    Pop-Location
}
Write-Host '=== 26.x BUILDS DONE ==='
