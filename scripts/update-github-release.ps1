$ErrorActionPreference = 'Stop'
$cred = ("protocol=https`nhost=github.com`n`n" | git credential fill 2>$null) | Out-String
$token = ($cred | Select-String 'password=(.+)' | ForEach-Object { $_.Matches[0].Groups[1].Value })
if (-not $token) { throw 'no github token' }

$repo = 'C:\Users\34324\Desktop\Ultramarine-recast-fabric'
$root = $repo
$version = (Select-String -Path (Join-Path $root 'gradle.properties') -Pattern '^mod_version=(.+)$').Matches[0].Groups[1].Value.Trim()
$tag = "v$version"
$headers = @{ Authorization = "Bearer $token"; 'User-Agent' = 'dsh-release' }

# find the existing release for this tag
$releases = Invoke-RestMethod -Uri 'https://api.github.com/repos/SC-Sunflower/Ultramarine-recast/releases' -Headers $headers -Method Get
$rel = $releases | Where-Object { $_.tag_name -eq $tag } | Select-Object -First 1
if (-not $rel) { throw "release $tag not found" }
Write-Host "updating release $($rel.tag_name) id=$($rel.id) with $($rel.assets.Count) existing assets"

# 1. delete existing assets of this release
foreach ($a in $rel.assets) {
    Invoke-RestMethod -Uri "https://api.github.com/repos/SC-Sunflower/Ultramarine-recast/releases/assets/$($a.id)" -Headers $headers -Method Delete | Out-Null
    Write-Host "deleted asset $($a.name)"
}

# 2. refresh the release notes with the round-4 fixes appended
$bodyFile = Join-Path $root 'release/RELEASE_NOTES_0.7.0.1.md'
$bodyText = if (Test-Path $bodyFile) { Get-Content $bodyFile -Raw } else { $rel.body }
$bodyText = $bodyText + "`n" + @"

### 0.7.0.1 rebuild (round 4)

- Brick kiln: safe Porting Lib fuel lookup (no unboxing NPE); result stacks are
  written back explicitly instead of relying on handler reference semantics;
  shift-clicked ingredients still route by recipe role and match order-insensitively.
- Chisel table: onTake consumes ingredients with an explicit write-back.
- 1.20.x bottle gourd: consumed potion charges now mark the block entity dirty.
- 1.20.1: porting merchants deep-copy every trade stack; 1.20/1.20.1 jars now ship
  the flattened 39 Porting Lib modules again.
- Transfer API: brick kiln / chisel table storage caps inserts by the item's own
  max stack size.
- Seat blocks: consistent client prediction and failed-mount cleanup.
- Censer: saves remaining burn time about once per second instead of every tick.
- Villager trades: live-table merge (other mods' trades are no longer overwritten).
- Build scripts read the version from gradle.properties; the obsolete
  libs/porting-lib-1202 dump was removed and the README documents the Porting Lib
  fetch step.
"@
$bodyJson = @{ body = $bodyText } | ConvertTo-Json -Depth 2
Invoke-RestMethod -Uri "https://api.github.com/repos/SC-Sunflower/Ultramarine-recast/releases/$($rel.id)" -Headers $headers -Method Patch -Body $bodyJson -ContentType 'application/json' | Out-Null
Write-Host 'release notes updated'

# 3. upload the new jars
$jarDir = Join-Path $root 'release'
$jars = Get-ChildItem $jarDir -Filter "*$version.jar" | Sort-Object Name
$failed = @()
foreach ($jar in $jars) {
    $uploadUrl = "https://uploads.github.com/repos/SC-Sunflower/Ultramarine-recast/releases/$($rel.id)/assets?name=$([uri]::EscapeDataString($jar.Name))"
    try {
        $r = Invoke-RestMethod -Uri $uploadUrl -Headers $headers -Method Post -InFile $jar.FullName -ContentType 'application/java-archive'
        Write-Host "uploaded $($jar.Name)"
    } catch {
        $failed += $jar.Name
        Write-Host "FAILED $($jar.Name) : $($_.Exception.Message)"
    }
}
Write-Host "uploaded $($jars.Count - $failed.Count)/$($jars.Count) assets"
if ($failed.Count) { Write-Host "failed: $($failed -join ', ')" }
