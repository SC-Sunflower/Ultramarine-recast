$env:GIT_AUTHOR_NAME = 'Ultramarine Recast'
$env:GIT_AUTHOR_EMAIL = 'recast@localhost'
$env:GIT_COMMITTER_NAME = 'Ultramarine Recast'
$env:GIT_COMMITTER_EMAIL = 'recast@localhost'

$msg = @"
0.7.0.1: block entity NBT safety, transfer API rollback, seat/censer fixes

- BottleGourdBlockEntity: skip writing the Potion key when the gourd is empty
  (fixes NPE in saveAdditional for freshly placed gourds); mark charge changes
  with setChanged() so they persist; use the MAX_CHARGE constant.
- CenserBlockEntity: only tick while lit (unlit censers no longer flip state and
  dirty chunks every tick); mark remainingTime changes; restore the lit flag
  from the LIT block state on load.
- SeatEntity: actually assign the life timer in readAdditionalSaveData; use the
  MAX_LIFE constant; remove the unused LIFE synched-data accessor.
- BrickKilnMenu/ChiselTableMenu InventoryWrapper: register transaction abort
  callbacks so Fabric Transfer API rollbacks restore the slot contents
  (prevents duplication/loss with third-party storage integrations).
- BrickKilnBlockEntity: award experience for CompositeSmeltingRecipe via
  instanceof (no AbstractCookingRecipe cast) and fall back to vanilla cooking
  recipes; custom kiln recipes no longer crash on break and grant their XP.
- ChiselTableRecipe: reject recipes with more than 4 colors (codec validate /
  flatXmap for datafixerupper 6.x; explicit check in the Gson serializer).
- Bump version to 0.7.0.1.
"@

$wts = @(
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
