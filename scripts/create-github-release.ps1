$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$version = (Select-String -Path (Join-Path $root 'gradle.properties') -Pattern '^mod_version=(.+)$').Matches[0].Groups[1].Value.Trim()
$cred = ("protocol=https`nhost=github.com`n`n" | git credential fill 2>$null) | Out-String
$token = ($cred | Select-String 'password=(.+)' | ForEach-Object { $_.Matches[0].Groups[1].Value })
if (-not $token) { throw 'no github token' }

$headers = @{ Authorization = "Bearer $token"; 'User-Agent' = 'dsh-release' }

# create the new release (does not touch v0.7.0)
$body = @{
    tag_name = "v$version"
    name = "Ultramarine: Recast $version"
    body = @"
Second-generation release (0.7.0.1).

Fixes since 0.7.0:
- Villager trading: data-driven cook trades restored on 26.1/26.2/26.3, vanilla
  profession extras and wandering trader rares via tag merges (the trade screen
  opens again on all 26.x versions).
- 26.3 world creation: advancement/datapack format migrated to the 26.3 codecs,
  stray minecraft-namespace overrides removed, forge:dyes -> c:dyes, ore
  generation restored with the 26.3 worldgen format, loot tables converted.
- Block entities: BottleGourd save NPE + missing setChanged, Censer idle ticking,
  Seat life timer persistence.
- Transfer API transaction rollbacks in the brick kiln and chisel table menus.
- Brick kiln: shift-clicked ingredients are routed to the slot matching their
  recipe role and matching is order-insensitive; experience now granted for
  CompositeSmeltingRecipe without ClassCastException.
- Travelling merchant offers are deep-copied per entity.
- Chisel table recipes limited to 4 colors; texture references qualified with
  proper namespaces; build scripts made portable.
- Jars: 24 files covering 1.20 - 1.21.11 and 26.1 - 26.3.
"@
} | ConvertTo-Json

$rel = Invoke-RestMethod -Uri 'https://api.github.com/repos/SC-Sunflower/Ultramarine-recast/releases' -Headers $headers -Method Post -Body $body -ContentType 'application/json'
Write-Host "created release: $($rel.tag_name) id=$($rel.id) upload=$($rel.upload_url)"

$releaseId = $rel.id
$jarDir = 'C:\Users\34324\Desktop\Ultramarine-recast-fabric\release'
$jars = Get-ChildItem $jarDir -Filter '*0.7.0.1.jar' | Sort-Object Name
$failed = @()
foreach ($jar in $jars) {
    $uploadUrl = "https://uploads.github.com/repos/SC-Sunflower/Ultramarine-recast/releases/$releaseId/assets?name=$([uri]::EscapeDataString($jar.Name))"
    try {
        $r = Invoke-RestMethod -Uri $uploadUrl -Headers $headers -Method Post -InFile $jar.FullName -ContentType 'application/java-archive'
        Write-Host "uploaded $($jar.Name) -> $($r.browser_download_url)"
    } catch {
        $failed += $jar.Name
        Write-Host "FAILED $($jar.Name) : $($_.Exception.Message)"
    }
}
Write-Host ''
Write-Host "uploaded $($jars.Count - $failed.Count)/$($jars.Count) assets"
if ($failed.Count) { Write-Host "failed: $($failed -join ', ')" }
