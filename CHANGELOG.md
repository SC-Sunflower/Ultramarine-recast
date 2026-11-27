# Changelog

## 0.7.0 (unreleased)

First public release of **Ultramarine: Recast** — Ultramarine 0.7 content ported to
Minecraft 1.20.1 / Fabric.

### Minecraft 1.20 support

- The same jar now also runs on Minecraft 1.20: 1.20 and 1.20.1 share identical Fabric
  intermediary mappings, so the build is binary-compatible.
- `scripts/patch-mc-version.ps1` rewrites the bundled Porting Lib version declarations
  from `= 1.20.1` to `>=1.20 <1.20.2` so Fabric Loader accepts the jar on 1.20.
- 1.20 requires Fabric API 0.92.12+1.20.1; 1.20.1 uses the regular 1.20.1 Fabric API.

### Added

- 18 wooden rafters and rafter ends (oak, spruce, birch, jungle, acacia, crimson,
  warped, mangrove, cherry)
- Red floor tile block, slab, stairs and wall
- Red and blue carved carpets
- Glazed central wall pattern, grass lucky motifs 1–3, loong tuanhua, lotus leaves
  bowl, yellow-green glazed top layer eaves
- Bamboo tea basket and empty bamboo tea basket
- Carved wool chisel template
- Blue-and-white porcelain upgrade smithing template (unlocks the porcelain tool set)
- Recipes: blue-and-white porcelain sword / shovel / pickaxe / axe, jade, jade block,
  bronze ingot, bronze block, cooked meat (smelting and smoking), porcelain piece
  grinding, porcelain parts, candle from grease, brick from fired brick and more
  (53 recipes total)
- Fur drops from foxes, goats and rabbits; raw meat drops from hoglins and ravagers
- Item tags: baozi, ham, silk, xuan paper
- Updated guide book ("Tiangong Kaiwu") pages for the new content
- Updated zh_cn / en_us localizations

### Technical

- Built on the original authors' Fabric codebase, targeting Minecraft 1.20.1 with
  Fabric Loader, Fabric API and Porting Lib
- NeoForge biome modifiers replaced with `fabric-biome-api-v1` (ore generation was
  already present in the Fabric codebase)
- NeoForge global loot modifiers replaced with Fabric `LootTableEvents.MODIFY`
  injections
- Compile-only optional integrations for JEI (15.x) and Jade (11.x)

### Restored from the official 0.5.1 sources

The community Fabric build had silently dropped several 0.5.1 features. They were
restored by translating the official Forge 0.5.1 source code:

- Fired brick submerged in water converts into a cyan brick before despawning
  (Forge `ItemExpireEvent` → `ItemEntity` mixin)
- Chisel table multiblock: placing a brush-and-inkstone on a porcelain inlaid table
  assembles a chisel table; breaking the table reverts it (Forge `BlockEvent` → block
  subclasses)
- Snow falling on snow roof ridges is absorbed as an extra snow layer (Forge
  `NeighborNotifyEvent` → Porting Lib `BlockEvents.NEIGHBORS_NOTIFY`)
- Travelling merchant natural spawning near trade POIs with vanilla wandering trader
  probability (Forge `LevelTickEvent` → `ServerTickEvents.START_WORLD_TICK`)
- Full villager trade sets: mason, toolsmith, farmer, librarian, cleric and the
  complete cook list (the community build only shipped 7 of the ~30 cook trades)
- Vanilla wandering trader rare trades: incense, silk, jade, painting scroll and
  blue-and-white porcelain vase (Forge `WandererTradesEvent` → trade registry)
