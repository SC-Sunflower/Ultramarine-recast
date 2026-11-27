# Roadmap / 鐗堟湰璺嚎

Ultramarine: Recast 鈥?ports of [Ultramarine](https://github.com/LocusAzzurro/Ultramarine) to
multiple Minecraft versions, with permission from the original authors.

Current release: **0.7.0 content, Minecraft 1.20.1 鈥?26.2 / Fabric**.

## Version matrix

| Phase | Versions | Loader | Status | Notes |
| --- | --- | --- | --- | --- |
| 鉁?Done | 1.20.1 | Fabric | Done | Baseline, content = Ultramarine 0.7 |
| 鉁?Done | 1.20 | Fabric | Done (jar variant) | Runs from the 1.20.1 jar, Porting Lib flattened |
| 鉁?Done | 1.20.2 鈥?1.20.6 | Fabric | Done | Five branches |
| 鉁?Done | 1.21 鈥?1.21.11 | Fabric | Done (three variants) | 1.21/1.21.3 are jar variants; 1.21.8 is a downgraded 1.21.9 build |
| 鉁?Done | 26.1, 26.1.1, 26.1.2 | Fabric | Done (26.1 branch + variants) | 26.x branches are local-only until pushed |
| 馃敡 In progress | 26.2 | Fabric | Port exists, release pending | Needs pack format/config cleanup before shipping |
| 馃敡 In progress | 26.3 | Fabric | Port compiles; release pending | Newest Minecraft release (pack format 121.0) |
| 猬?Planned | 1.19.2 | Fabric | Not started | ROADMAP phase 4 |
| 猬?Planned | 1.18.2, 1.17.1 | Fabric | Not started | Older Fabric API surface |
| 猬?Planned | 1.16.5 and older | Forge | Not started | Fabric does not exist before 1.14 |

## Branch strategy

- `main` 鈥?1.20.1 (and the 1.20 jar variant)
- `fabric/<mc-version>` 鈥?one branch per Fabric version, e.g. `fabric/1.21.2`, `fabric/26.2`
- `forge/<mc-version>` 鈥?Forge ports (1.13.2 and older), separate from the Fabric branches

Shared resources (textures, models, lang) are kept per-branch because their formats
change between Minecraft versions.

## Content policy

- Mod id stays `ultramarine` on every version so worlds and data packs stay compatible.
- Code baseline: the original authors' Fabric sources; content baseline: Ultramarine 0.7.
- Porting Lib is bundled into the jar on 1.20.1/1.20; other versions may bundle or declare it
  depending on the loader's conventions.
- Optional integrations: REI (recipe viewer), Jade (tooltips), Patchouli (guide book).
  JEI integration was dropped; REI is the supported recipe viewer.

## License

- Source: BSD-3-Clause
- Assets: CC BY-NC 4.0 (no commercial use 鈥?see `LICENSE`)
- Original mod: https://github.com/LocusAzzurro/Ultramarine
