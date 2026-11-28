# Ultramarine-recast-fabric
Ultramarine-recast-fabric is a Chinese-style building mod that adds furniture and decorative blocks. This mod is an unofficial port of https://github.com/LocusAzzurro/Ultramarine?tab=readme-ov-file.

## Building from source

The 1.20.1 (main) branch compiles against Porting Lib module jars that are not
checked in (they are extracted from the aggregate jar). Before the first build run:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/fetch-porting-lib.ps1
```

This downloads `porting_lib-<porting_lib_version>.jar` (version read from
`gradle.properties`) and extracts its 39 module jars into `libs/porting-lib/`.
The release build then runs `scripts/fix-1.20-fatjar.ps1` so the shipped jar carries
flat nested modules (Fabric Loader cannot load two-level nested jars).
