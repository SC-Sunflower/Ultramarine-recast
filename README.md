# 群青：重铸 (Ultramarine: Recast)

A Chinese Style Minecraft Mod. **Ultramarine: Recast** is a port of
[Ultramarine](https://github.com/LocusAzzurro/Ultramarine) **0.7** to **Minecraft
1.20.1 – 1.21.11 / Fabric**, with the express permission of the original authors.

- Content baseline: Ultramarine 0.7 (NeoForge 1.21.1) — all blocks, items, recipes,
  loot tables, advancements, world generation, villager professions and trades are
  carried over.
- Mod id stays `ultramarine` so existing worlds, data packs and integrations keep working.

## Supported versions

| Minecraft | Fabric API | Branch | Notes |
| --- | --- | --- | --- |
| 1.20.1 | 1.20.1 任意近期版本 | `main` | Porting Lib bundled (flattened) |
| 1.20.2 – 1.20.6 | 对应版本 | `fabric/1.20.x` | |
| 1.21 – 1.21.1 | 0.100+ / 0.102+ | `fabric/1.21.1` | |
| 1.21.2 – 1.21.3 | 0.106.1+1.21.2 | `fabric/1.21.2` | |
| 1.21.4 – 1.21.7 | 对应版本 | `fabric/1.21.x` | |
| 1.21.8 – 1.21.11 | 对应版本 | `fabric/1.21.x` | 1.21.8 由 1.21.9 分支降级构建 |

> **Minecraft 1.20 已放弃支持**（1.20.1 起）。

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for your Minecraft version.
2. Install the matching **Fabric API** build.
3. Put the matching `ultramarine-recast-fabric-<mc>-<version>.jar` into `mods/`.

Optional integrations: REI (recipe viewer), JEI, Jade, Patchouli.

## Building

1. Fetch the Porting Lib module jars (1.20.1 only): `scripts/fetch-porting-lib.ps1`
2. `gradlew.bat build` on the matching branch.
3. 1.20.1: run `scripts/fix-1.20-fatjar.ps1` on the built jar (flattens the bundled
   Porting Lib modules so Fabric Loader can see them).
4. All versions: `scripts/sync-release.ps1` builds every branch and copies one jar
   per Minecraft version into `release/` (jars are published as GitHub Releases,
   not committed to the repository).

## Credits

Ultramarine is created by **LocusAzzurro** (programming) and **Soul_Shadow** (art),
with art contributions from NekoGabriel, SKinsark and 76823999, and community
maintenance by 北斗余晖 and cnlimiter. See [`CREDITS.md`](CREDITS.md) and
`src/main/resources/ultramarine_credits.txt` for the full list.

## License

- **Source code and binaries**: BSD 3-Clause License
- **Assets** (textures, models, sounds, texts): Creative Commons
  Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)

See [LICENSE](LICENSE) for details.
