# 缇ら潚锛氶噸閾?(Ultramarine: Recast)

A Chinese Style Minecraft Mod. **Ultramarine: Recast** is a port of
[Ultramarine](https://github.com/LocusAzzurro/Ultramarine) **0.7** to
**Minecraft 1.20.1 鈥?26.2 / Fabric**, with the express permission of the original authors.

- Content baseline: Ultramarine 0.7 (NeoForge 1.21.1) 鈥?all blocks, items, recipes,
  loot tables, advancements, world generation, villager professions and trades are
  carried over.
- Mod id stays `ultramarine` so existing worlds, data packs and integrations keep working.

## Supported versions

| Minecraft | Fabric API | Branch | Notes |
| --- | --- | --- | --- |
| 1.20 | 0.83.0+1.20 | `main` (1.20.1 jar variant) | Runs from the 1.20.1 jar; `scripts/fix-1.20-fatjar.ps1` flattens the bundled Porting Lib modules |
| 1.20.1 | 浠绘剰杩戞湡鐗堟湰 | `main` | Baseline, content = Ultramarine 0.7 |
| 1.20.2 鈥?1.20.6 | 瀵瑰簲鐗堟湰 | `fabric/1.20.x` (5 branches) | |
| 1.21 鈥?1.21.11 | 瀵瑰簲鐗堟湰 | `fabric/1.21.x` | 1.21/1.21.3 are jar variants; 1.21.8 is a downgraded build of the 1.21.9 branch |
| 26.1 鈥?26.1.2 | 瀵瑰簲鐗堟湰 | `fabric/26.1` | 26.1.1/26.1.2 are jar variants; 26.x branches are local-only until pushed |
| 26.2 | 瀵瑰簲鐗堟湰 | `fabric/26.2` | Work in progress |
| 26.3 | 0.166.2+26.3 | `fabric/26.3` | Done (Jade 26.3 + REI 26.3; pack 121.0) |
| 1.19.2 | 鈥?| 鈥?| Planned (ROADMAP phase 4), not started |

> Minecraft 1.20 is still supported through the 1.20.1 jar variant; the jar declares
> `minecraft: ">=1.20 <1.20.2"` and the bundled Porting Lib modules are flattened by
> `scripts/fix-1.20-fatjar.ps1`.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for your Minecraft version.
2. Install the matching **Fabric API** build.
3. Put the matching `ultramarine-recast-fabric-<mc>-<version>.jar` into `mods/`.

Optional integrations: **REI** (recipe viewer), **Jade** (block tooltips), **Patchouli**
(guide book, "Tiangong Kaiwu"). JEI integration was dropped in favour of REI.

## Building

1. Fetch the Porting Lib module jars (1.20.1/1.20 only): `scripts/fetch-porting-lib.ps1`
2. `gradlew.bat build` on the matching branch.
3. 1.20.1/1.20: run `scripts/fix-1.20-fatjar.ps1` on the built jar (flattens the bundled
   Porting Lib modules so Fabric Loader can see them).
4. All versions: `scripts/sync-release.ps1` builds every branch and copies one jar
   per Minecraft version into `release/` (jars are published as GitHub Releases,
   not committed to the repository).

## Credits

Ultramarine is created by **LocusAzzurro** (programming) and **Soul_Shadow** (art),
with art contributions from NekoGabriel, SKinsark and 76823999, and community
maintenance by 鍖楁枟浣欐櫀 and cnlimiter. See [`CREDITS.md`](CREDITS.md) and
`src/main/resources/ultramarine_credits.txt` for the full list.

## License

- **Source code and binaries**: BSD 3-Clause License
- **Assets** (textures, models, sounds, texts): Creative Commons
  Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)

See [LICENSE](LICENSE) for details.
