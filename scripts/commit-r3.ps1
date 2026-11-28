$env:GIT_AUTHOR_NAME = 'Ultramarine Recast'
$env:GIT_AUTHOR_EMAIL = 'recast@localhost'
$env:GIT_COMMITTER_NAME = 'Ultramarine Recast'
$env:GIT_COMMITTER_EMAIL = 'recast@localhost'

$msg = @"
Fix shift-click ingredient placement and 26.3 villager trades

- BrickKilnMenu: route shift-clicked ingredients to the slot matching their
  recipe role (primary vs secondary) instead of always filling slot 0, which
  broke recipe matching until players rearranged the materials manually.
- CompositeSmeltingRecipe.matches: accept both ingredient orders so kiln
  recipes still match when materials sit in swapped slots.
- 26.3: restore the data-driven cook villager trade sets (trade_set,
  villager_trade entries and tag merges for vanilla professions plus the
  wandering trader) and the cook profession trade map, so the trade screen
  opens on 26.3; add the entity.ultramarine.villager.cook lang entries.
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
