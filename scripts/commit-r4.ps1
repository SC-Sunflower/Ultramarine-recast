$env:GIT_AUTHOR_NAME = 'Ultramarine Recast'
$env:GIT_AUTHOR_EMAIL = 'recast@localhost'
$env:GIT_COMMITTER_NAME = 'Ultramarine Recast'
$env:GIT_COMMITTER_EMAIL = 'recast@localhost'

$msg = @"
Fix block-entity/transfer/fuel/shift-click issues (round 4)

- BrickKilnBlockEntity.burn: write the grown result stack back with
  setStackInSlot instead of mutating a possibly-copied stack (result stacks no
  longer stall at one item / vanish).
- ChiselTableMenu.onTake: consume ingredients by explicit write-back; the old
  "set then shrink" order only worked when the handler returned live references.
- 1.20.1 BrickKilnBlockEntity: safe Porting Lib fuel lookup (no Integer
  unboxing NPE for non-fuel items, hoppers or stale saves); tick uses it.
- 1.20.x BottleGourdBlockEntity.takePotionCharge: call setChanged() so consumed
  charges are persisted.
- 1.20.1 TravellingMerchant.copyOffer: deep-copy every stack (cost B and result
  were still shared between merchants).
- Transfer API InventoryWrapper (brick kiln / chisel table): cap inserts by the
  item's own max stack size, not just the inventory limit.
- SeatDecorativeBlock: return a consistent client prediction, discard the seat
  entity when mounting fails.
- CenserBlockEntity: persist remaining burn time about once per second instead
  of every tick.
- VillagerTradingsRegister: merge into the live trade tables instead of
  replacing them with the static snapshot (other mods' trades survive); rewritten
  per API group (1.20.x ItemStack costs, 1.20.5+ ItemCost, 1.21.5+ ResourceKey
  keys and wandering-trader pair list).
- Build scripts: read mod_version from gradle.properties, qualify versioned
  paths, document the Porting Lib fetch step, drop the obsolete
  libs/porting-lib-1202 dump.
"@

$wts = @(
  'C:\Users\34324\Desktop\Ultramarine-recast-fabric',
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
  'C:\Users\34324\Desktop\um-12111-wt',
  'C:\Users\34324\Desktop\um-26.1-wt',
  'C:\Users\34324\Desktop\um-26.2-wt',
  'C:\Users\34324\Desktop\um-26.3-wt'
)
foreach ($wt in $wts) {
    $name = Split-Path $wt -Leaf
    $n = (git -C $wt status --short | Measure-Object).Count
    if ($n -eq 0) { Write-Host "$name : clean, skipping"; continue }
    git -C $wt add -A 2>&1 | Out-Null
    git -C $wt commit -m $msg 2>&1 | Out-Null
    $head = git -C $wt log --oneline -1
    Write-Host "$name : committed -> $head"
}
