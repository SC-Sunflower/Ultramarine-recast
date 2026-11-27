$env:GIT_AUTHOR_NAME = 'Ultramarine Recast'
$env:GIT_AUTHOR_EMAIL = 'recast@localhost'
$env:GIT_COMMITTER_NAME = 'Ultramarine Recast'
$env:GIT_COMMITTER_EMAIL = 'recast@localhost'

$msg = @"
Fix data/resource issues: namespaces, c:dyes tags, texture refs

- Delete stray data/minecraft recipe/advancement overrides (items belong in the
  ultramarine namespace; minecraft-ns copies pollute vanilla IDs).
- Replace forge:dyes/* advancement tags with c:dyes/* (Fabric convention tags;
  forge: tags do not exist on Fabric and blocked recipe unlocks).
- Qualify unqualified model texture references with minecraft:/ultramarine:
  prefixes so particles and faces no longer resolve to missing textures.
"@

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
    $n = (git -C $wt status --short | Measure-Object).Count
    if ($n -eq 0) { Write-Host "$name : clean, skipping"; continue }
    git -C $wt add -A
    git -C $wt commit -m $msg 2>&1 | Out-Null
    $head = git -C $wt log --oneline -1
    Write-Host "$name : committed -> $head"
}
