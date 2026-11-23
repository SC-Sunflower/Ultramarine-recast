# Roadmap / 版本路线

Ultramarine: Recast — ports of [Ultramarine](https://github.com/LocusAzzurro/Ultramarine) to
multiple Minecraft versions, with permission from the original authors.

Current release: **0.7.0 content, Minecraft 1.20.1 / Fabric** (this repository's `main`).

## Version matrix plan

| Phase | Versions | Loader | Difficulty | Notes |
| --- | --- | --- | --- | --- |
| ✅ Done | 1.20.1 | Fabric | — | Baseline, content = Ultramarine 0.7 |
| ✅ Done | 1.20 | Fabric | Very low | Same jar + `scripts/patch-mc-version.ps1`; 1.20 uses Fabric API 0.83.0+1.20 |
| 2 | 26.1, 26.1.1, 26.1.2 | Fabric | Low–medium | Official `fabric/1.20`-era 26.1 Fabric sources exist in Flechazo's fork branches |
| 2 | 26.2, 26.3 | Fabric | Medium | Iterate forward from the 26.1 port |
| 3 | 1.21, 1.21.1, 1.21.2 – 1.21.11 | Fabric | Medium–high | Data components (1.20.5+), item/recipe format changes, new render pipeline in 1.21.11+ |
| 4 | 1.19.2, 1.18.2, 1.17.1 | Fabric | Medium | Older Fabric API surface, model/state format changes |
| 5 | 1.16.5, 1.15.2, 1.14.4 | Fabric | High | Pre-1.17 world height / registry assumptions |
| 6 | 1.13.2, 1.12.2, 1.11.2, 1.10.2 | **Forge** | Very high | Fabric does not exist for these versions — these must use Forge, not Fabric |

> **Note**: Fabric only exists from 1.14 onward. The 1.10–1.13 entries in the requested
> range can only be delivered as Forge ports, and would belong in a separate repository
> (or clear `forge/<version>` branches), because they share almost no code with the
> Fabric line.

## Branch strategy

- `main` — current stable (1.20.1 Fabric)
- `fabric/<mc-version>` — one branch per Fabric version, e.g. `fabric/1.19.2`, `fabric/26.1`
- `forge/<mc-version>` — Forge ports (1.13.2 and older), separate from the Fabric branches

Shared resources (textures, models, lang) are kept per-branch because their formats
change between Minecraft versions.

## Content policy

- Mod id stays `ultramarine` on every version so worlds and data packs stay compatible.
- Code baseline: the original authors' Fabric sources; content baseline: Ultramarine 0.7.
- Porting Lib is bundled into the jar on 1.20.1; other versions may bundle or declare it
  depending on the loader's conventions.

## License

- Source: BSD-3-Clause
- Assets: CC BY-NC 4.0 (no commercial use — see `LICENSE`)
- Original mod: https://github.com/LocusAzzurro/Ultramarine
