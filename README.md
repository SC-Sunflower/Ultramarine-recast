# 群青：重铸 (Ultramarine: Recast)

A Chinese Style Minecraft Mod. **Ultramarine: Recast** is a port of
[Ultramarine](https://github.com/LocusAzzurro/Ultramarine) **0.7** (originally for
NeoForge 1.21.1) to **Minecraft 1.20.1 / Fabric**, with the express permission of the
original authors.

## About this port

- Content baseline: Ultramarine 0.7 (NeoForge 1.21.1) — all blocks, items, recipes,
  loot tables, world generation, villager professions and advancements are carried over.
- Code baseline: the original authors' Fabric 0.5.1 codebase, updated where necessary
  for Minecraft 1.20.1 APIs.
- Mod id stays `ultramarine` so existing worlds, data packs and integrations keep working.

### What 0.7 adds compared to the older Fabric builds

- 18 wooden rafters and rafter ends (oak, spruce, birch, jungle, acacia, crimson,
  warped, mangrove, cherry)
- Red floor tile family (block / slab / stairs / wall)
- Red and blue carved carpets
- Pattern blocks: glazed central wall pattern, grass lucky motifs 1–3, loong tuanhua,
  lotus leaves bowl, yellow-green glazed top layer eaves
- Bamboo tea baskets (filled and empty)
- Carved wool chisel template and the blue-and-white porcelain smithing upgrade template
- Recipes for the blue-and-white porcelain tool set, jade, bronze, cooked meat and more
  (53 new recipes total)
- Fur / raw meat drops from foxes, goats, rabbits, hoglins and ravagers

## Requirements

| Component | Version |
| --- | --- |
| Minecraft | 1.20.1 |
| Fabric Loader | >= 0.15.7 |
| Fabric API | any recent 1.20.1 build |

Porting Lib is bundled inside the jar (matching the original authors' fat jar layout),
so players do not need to install it separately.

Optional integrations: JEI, Jade, Patchouli.

## Building

First fetch the Porting Lib module jars (see
[`scripts/fetch-porting-lib.ps1`](scripts/fetch-porting-lib.ps1) for the rationale):

```powershell
powershell -File scripts/fetch-porting-lib.ps1
.\gradlew.bat build
```

The output jar is `build/libs/ultramarine-recast-fabric-1.20.1-<version>.jar`.

## Credits

Ultramarine is created by **LocusAzzurro** (programming) and **Soul_Shadow** (art),
with art contributions from NekoGabriel, SKinsark and 76823999, and community
maintenance by 北斗余晖 and cnlimiter. See
[`ultramarine_credits.txt`](src/main/resources/ultramarine_credits.txt) for the full
credits list. This port preserves all original attribution.

## License

- **Source code and binaries**: BSD 3-Clause License
- **Assets** (textures, models, sounds, texts): Creative Commons
  Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)

See [LICENSE](LICENSE) for details.
