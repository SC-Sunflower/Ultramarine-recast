# Changelog

## 0.7.0 (unreleased)

First public release of **Ultramarine: Recast** — Ultramarine 0.7 content ported to
Minecraft **1.20.1 – 26.2 / Fabric**.

### Supported versions

- 26.1 / 26.1.1 / 26.1.2 (fabric/26.1 and jar variants), 26.2 (fabric/26.2)
- 26.3 (fabric/26.3; pack format 121.0, Jade 26.3, REI 26.3).
- One jar per Minecraft version; 1.20/1.20.1, 1.21/1.21.1, 1.21.2/1.21.3 and
  26.1/26.1.1/26.1.2 share jars where the data formats are identical.

### Integrations

- **REI**: woodworking, chisel table and composite smelting categories.
- **Jade**: brick kiln progress, censer burn time, bottle gourd potion charges,
  mallet-toggleable blocks.
- **Patchouli**: "Tiangong Kaiwu" guide book resources (requires Patchouli).
- **JEI integration was dropped**; REI is the supported recipe viewer.

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

### Fixed

- Woodworking workbench recipe sync payload is now registered on both sides, so it
  works on dedicated servers (previously client-only registration).
- Travelling merchant offers are copied per entity, so stock/price no longer leak
  between merchants; trade selection is bounded and uses the entity's random source.
- Removed the `minecraft:brick` and `minecraft:white_candle` recipe overrides that
  replaced vanilla recipes.
- Recipe-unlock advancements now use the 1.21.2 `items` predicate field instead of the
  removed `tag` field; dye-powder advancements use `#c:dyes/*` tags.
- Removed the dangling `c:ingots/cobalt` advancement (the tag is not shipped).
- Jade integration reworked for every supported branch: Jade 12 (1.20.2), 13 (1.20.4),
  14 (1.20.5/1.20.6), 15 (1.21.1), 16 (1.21.2), 17 (1.21.4), 18 (1.21.5), 19 (1.21.6/1.21.7),
  20 (1.21.9/1.21.10), 21 (1.21.11), 26.x (26.1/26.2).
- REI integration restored for 26.2 (REI 26.2.820).
- Recipe book categories for custom recipes use registered categories.

### Technical

- Built on the original authors' Fabric codebase; content baseline is Ultramarine 0.7.
- NeoForge biome modifiers replaced with `fabric-biome-api-v1`.
- NeoForge global loot modifiers replaced with Fabric `LootTableEvents.MODIFY`
  injections.
- Jade is a compile-only optional dependency; REI is compile-only.

### Restored from the official 0.5.1 sources

The community Fabric build had silently dropped several 0.5.1 features. They were
restored by translating the official Forge 0.5.1 source code:

- Fired brick submerged in water converts into a cyan brick before despawning
  (Forge `ItemExpireEvent` → `ItemEntity` mixin)
- Chisel table multiblock: placing a brush-and-inkstone on a porcelain inlaid table
  assembles a chisel table; breaking the table reverts it (Forge `BlockEvent` → block
  subclasses)
- Snow falling on snow roof ridges is absorbed as an extra snow layer (Forge
  `NeighborNotifyEvent` → `Porting Lib BlockEvents.NEIGHBORS_NOTIFY` on 1.20.1,
  `SnowLayerBlock` random-tick mixin on newer versions)
- Travelling merchant natural spawning near trade POIs with vanilla wandering trader
  probability (Forge `LevelTickEvent` → `ServerTickEvents.START_WORLD_TICK`)
- Full villager trade sets: mason, toolsmith, farmer, librarian, cleric and the
  complete cook list (the community build only shipped 7 of the ~30 cook trades)
- Vanilla wandering trader rare trades: incense, silk, jade, painting scroll and
  blue-and-white porcelain vase (Forge `WandererTradesEvent` → trade registry)
