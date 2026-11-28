$env:GIT_AUTHOR_NAME = 'Ultramarine Recast'
$env:GIT_AUTHOR_EMAIL = 'recast@localhost'
$env:GIT_COMMITTER_NAME = 'Ultramarine Recast'
$env:GIT_COMMITTER_EMAIL = 'recast@localhost'

$msg = 'Fix block-entity/transfer/fuel/shift-click issues (round 4): safe fuel lookup, result-stack write-back, onTake consumption, transfer stack caps, seat interaction, censer save interval, live trade-table merge, portable versioned scripts'

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
    $out = git -C $wt commit -m $msg 2>&1
    $head = git -C $wt log --oneline -1
    Write-Host "$name : $head"
}
