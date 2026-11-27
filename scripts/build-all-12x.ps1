$gradle = "$env:USERPROFILE\.gradle\wrapper\dists\gradle-9.4.1-bin\45w7kj7s8jzqenl33wrwa0aoj\gradle-9.4.1\bin\gradle.bat"
$wts = @(
  'C:\Users\34324\Desktop\um-main-wt',
  'C:\Users\34324\Desktop\um-1202-wt',
  'C:\Users\34324\Desktop\um-1203-wt',
  'C:\Users\34324\Desktop\um-1204-wt',
  'C:\Users\34324\Desktop\um-1205-wt',
  'C:\Users\34324\Desktop\um-1206-wt',
  'C:\Users\34324\Desktop\um-1211-wt',
  'C:\Users\34324\Desktop\um-1214-wt',
  'C:\Users\34324\Desktop\um-1215-wt',
  'C:\Users\34324\Desktop\um-1216-wt',
  'C:\Users\34324\Desktop\um-1217-wt',
  'C:\Users\34324\Desktop\um-1219-wt',
  'C:\Users\34324\Desktop\um-12110-wt',
  'C:\Users\34324\Desktop\um-12111-wt'
)
foreach ($wt in $wts) {
    $name = Split-Path $wt -Leaf
    Write-Host "=== building $name ==="
    Push-Location $wt
    & $gradle build --console=plain --no-daemon 2>&1 | Select-Object -Last 3
    Pop-Location
}
Write-Host '=== ALL BUILDS DONE ==='
