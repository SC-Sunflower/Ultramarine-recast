# Process one 1.20.x branch: build, runServer-verify, sync release, commit.
param(
    [Parameter(Mandatory = $true)][string]$Wt,
    [Parameter(Mandatory = $true)][string]$Mc,
    [Parameter(Mandatory = $true)][int]$Port,
    [Parameter(Mandatory = $true)][string]$Msg
)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$version = (Select-String -Path (Join-Path $root 'gradle.properties') -Pattern '^mod_version=(.+)$').Matches[0].Groups[1].Value.Trim()
$gradle = Join-Path $env:USERPROFILE '.gradle\wrapper\dists\gradle-9.4.1-bin\45w7kj7s8jzqenl33wrwa0aoj\gradle-9.4.1\bin\gradle.bat'
$release = Join-Path (Split-Path $PSScriptRoot -Parent) 'release'

Write-Host "=== [$Mc] build ==="
Push-Location $Wt
& $gradle build --console=plain --no-daemon 2>&1 | Select-Object -Last 2
if ($LASTEXITCODE -ne 0) { Pop-Location; throw "[$Mc] build failed" }

Write-Host "=== [$Mc] runServer check ==="
New-Item -ItemType Directory -Force -Path (Join-Path $Wt 'run') | Out-Null
Set-Content -Path (Join-Path $Wt 'run\eula.txt') -Value 'eula=true' -Encoding ascii
Set-Content -Path (Join-Path $Wt 'run\server.properties') -Value "server-port=$Port`neula=true`nonline-mode=true" -Encoding ascii
$log = Join-Path $Wt 'run-check.log'
if (Test-Path $log) { Remove-Item $log -Force }
$proc = Start-Process -FilePath $gradle -ArgumentList 'runServer', '--console=plain', '--no-daemon' `
    -WorkingDirectory $Wt -RedirectStandardOutput $log -RedirectStandardError "$log.err" -PassThru -WindowStyle Hidden
$done = $false
for ($i = 0; $i -lt 80; $i++) {
    Start-Sleep 3
    if (-not $proc.HasExited) { } # gradle wrapper process
    if (Test-Path $log) {
        $t = Get-Content $log -Raw
        if ($t -match 'Done \(') { $done = $true; break }
        if ($t -match 'BUILD FAILED|FAILURE:') { break }
    }
}
# kill the server java processes for this worktree
Get-CimInstance Win32_Process -Filter "Name='java.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -and $_.CommandLine.Contains($Wt) } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }
Start-Sleep 2

if (Test-Path $log) {
    $text = Get-Content $log -Raw
    $errs = ([regex]::Matches($text, 'Parsing error|Couldn''t parse')).Count
    $adv = [regex]::Match($text, 'Loaded (\d+) advancements')
    $rec = [regex]::Match($text, 'Loaded (\d+) recipes')
    Write-Host "[$Mc] errors=$errs advancements=$($adv.Groups[1].Value) recipes=$($rec.Groups[1].Value) done=$done"
    if ($errs -gt 0 -or -not $done) { Pop-Location; throw "[$Mc] runServer check failed (errors=$errs done=$done)" }
} else {
    Pop-Location; throw "[$Mc] no run-check log"
}

Write-Host "=== [$Mc] release + commit ==="
$jar = Get-ChildItem (Join-Path $Wt 'build\libs') -Filter '*.jar' |
    Where-Object { $_.Name -notmatch 'sources' } | Select-Object -First 1
Copy-Item $jar.FullName (Join-Path $release "ultramarine-recast-fabric-$Mc-$version.jar") -Force
git add -A
git -c user.name='Ultramarine Recast' -c user.email='recast@localhost' commit -m $Msg 2>&1 | Select-Object -First 2
Pop-Location
Write-Host "[$Mc] done"
