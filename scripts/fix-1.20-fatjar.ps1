# fix-1.20-fatjar.ps1
# Rewrites the 1.20/1.20.1 fat jar so Porting Lib ships as flat nested mods
# (fat jar -> META-INF/jars/<module>.jar) instead of two-level nesting
# (fat jar -> aggregate jar -> module jars), which Fabric Loader fails to load.
#
# Usage: powershell -ExecutionPolicy Bypass -File scripts/fix-1.20-fatjar.ps1
#        (run after `gradle build` on the main branch)

$ErrorActionPreference = 'Stop'
$Repo = "C:\Users\34324\Desktop\Ultramarine-recast-fabric"
$FatJar = Join-Path $Repo "build\libs\ultramarine-recast-fabric-1.20.1-0.7.0.jar"
$LibDir = Join-Path $Repo "libs\porting-lib"

if (-not (Test-Path $FatJar)) { throw "fat jar not found: $FatJar" }

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$moduleJars = Get-ChildItem $LibDir -Filter *.jar | Sort-Object Name
Write-Output ("modules: {0}" -f $moduleJars.Count)

$zip = [System.IO.Compression.ZipFile]::Open($FatJar, [System.IO.Compression.ZipArchiveMode]::Update)
try {
    # 1. drop every existing nested jar (aggregate jar and anything else)
    $existing = @($zip.Entries | Where-Object { $_.FullName -like 'META-INF/jars/*' })
    foreach ($e in $existing) { $e.Delete() }
    Write-Output ("removed nested jars: {0}" -f $existing.Count)

    # 2. add each module jar flat
    # 2. add each module jar flat, patching its minecraft declaration so it also
    #    loads on 1.20 (modules declare "=1.20.1"; 1.20 and 1.20.1 share identical
    #    intermediary mappings, so the patch is safe).
    $patchPattern = '"minecraft"\s*:\s*"(?:\\u003d|=)1\.20\.1"'
    foreach ($m in $moduleJars) {
        $tmpJar = Join-Path $env:TEMP ("um-pl-" + [guid]::NewGuid().ToString('N') + ".jar")
        Copy-Item $m.FullName $tmpJar -Force
        $z = [System.IO.Compression.ZipFile]::Open($tmpJar, [System.IO.Compression.ZipArchiveMode]::Update)
        try {
            $fe = $z.GetEntry("fabric.mod.json")
            if ($fe) {
                $fsr = New-Object System.IO.StreamReader($fe.Open())
                $fjson = $fsr.ReadToEnd()
                $fsr.Close()
                $fnew = [regex]::Replace($fjson, $patchPattern, '"minecraft": ">=1.20 <1.20.2"')
                if ($fnew -ne $fjson) {
                    $fe.Delete()
                    $fne = $z.CreateEntry("fabric.mod.json")
                    $fsw = New-Object System.IO.StreamWriter($fne.Open())
                    $fsw.Write($fnew)
                    $fsw.Close()
                }
            }
        }
        finally { $z.Dispose() }
        $target = "META-INF/jars/" + $m.Name
        [void][System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $tmpJar, $target, [System.IO.Compression.CompressionLevel]::Optimal)
        Remove-Item $tmpJar -Force
    }
    Write-Output ("added module jars: {0} (minecraft declaration patched)" -f $moduleJars.Count)

    # 3. rewrite fabric.mod.json "jars" array
    $entry = $zip.GetEntry("fabric.mod.json")
    $sr = New-Object System.IO.StreamReader($entry.Open())
    $json = $sr.ReadToEnd()
    $sr.Close()

    $jarEntries = ($moduleJars | ForEach-Object { '      {"file": "META-INF/jars/' + $_.Name + '"}' }) -join ",`r`n"
    $jarsBlock = "  `"jars`": [`r`n" + $jarEntries + "`r`n  ],"

    if ($json -match '"jars"\s*:\s*\[') {
        $json = [regex]::Replace($json, '"jars"\s*:\s*\[[\s\S]*?\]', $jarsBlock.TrimEnd(','), 1)
    } elseif ($json -match '"license"\s*:') {
        # no jars array yet: insert before "license"
        $json = [regex]::Replace($json, '(\s*)("license"\s*:)', ("`r`n" + $jarsBlock + "`r`n  `$2"), 1)
    }

    $entry.Delete()
    $newEntry = $zip.CreateEntry("fabric.mod.json")
    $sw = New-Object System.IO.StreamWriter($newEntry.Open())
    $sw.Write($json)
    $sw.Close()
}
finally {
    $zip.Dispose()
}

# verify
$zip2 = [System.IO.Compression.ZipFile]::OpenRead($FatJar)
$nested = @($zip2.Entries | Where-Object { $_.FullName -like 'META-INF/jars/*' })
$e2 = $zip2.GetEntry("fabric.mod.json")
$sr2 = New-Object System.IO.StreamReader($e2.Open())
$check = $sr2.ReadToEnd()
$sr2.Close()
$zip2.Dispose()

$common = @($nested | Where-Object { $_.FullName -like '*porting_lib_common*' })
Write-Output ("nested jars now: {0}" -f $nested.Count)
Write-Output ("porting_lib_common present: {0}" -f ($common.Count -gt 0))
Write-Output ("fabric.mod.json jars entries: {0}" -f ([regex]::Matches($check, '"file": "META-INF/jars/')).Count)
Write-Output ("fat jar size: {0} MB" -f [Math]::Round((Get-Item $FatJar).Length / 1MB, 2))
