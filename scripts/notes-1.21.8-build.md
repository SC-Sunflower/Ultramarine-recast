# 1.21.8 构建说明（手动流程）

1.21.8 没有独立 git 分支；由 `fabric/1.21.9` 分支临时降级构建。1.21.9 分支代码使用了一些
1.21.9 才有的 API，构建 1.21.8 前需要做以下临时改动（构建完全部还原）：

1. `git worktree add %TEMP%\um-1218 fabric/1.21.9`（PowerShell：`git worktree add $env:TEMP\um-1218 fabric/1.21.9`）
2. 改 `gradle.properties`：
   - `minecraft_version=1.21.8`
   - `fabric_api_version=0.134.0+1.21.8`
   - `parchment_minecraft_version=1.21.8`
   - `parchment_mappings_version=2025.09.14`（1.21.8 的 parchment 数据；不要沿用 1.21.9 的 2025.10.05）
   - `fabric_loader_version` 保持 0.17.0（fabric-api 0.134 要求 >=0.17.0）
3. **改 `src/main/resources/fabric.mod.json`**：`"minecraft"` 依赖范围从 `>=1.21.9 <1.21.10`
   改为 **`>=1.21.8 <1.21.9`**（该字段是写死的字符串，processResources 不会展开，
   漏改会导致 1.21.8 启动报 `HARD_DEP_INCOMPATIBLE`）。
4. `pack.mcmeta` 用 1.21.8 传统格式：`pack_format: 81` + `supported_formats: [64, 81]`
   （1.21.9 的 min_format/max_format 写法 1.21.8 不认识）。
5. 代码临时改动（1.21.8 API 差异，MCP 查证为准，编译报错为准）：
   - Screen 鼠标事件用旧签名 `mouseClicked(double,double,int)` / `mouseDragged(double,double,int,double,double)`
     （1.21.9 才改成 MouseButtonEvent），删除 `import net.minecraft.client.input.MouseButtonEvent`
   - `SixSideBlock` / `WallSideBlock`：`noOcclusion().noCollision()` → `noOcclusion()`
   - `wind_chime.json` 配方 `minecraft:iron_chain` 改回 `minecraft:chain`（1.21.9 才拆分）
6. 构建：`gradle build --console=plain --no-daemon`，然后 runServer 快速验证
   （`Loaded N recipes`、`Couldn't parse` 计数 0、`Done`）。
7. 复制 `build/libs/ultramarine-recast-fabric-1.21.8-0.7.0.jar` 到主仓库 `release/`
   （覆盖同名文件）。
8. 全部还原（`git checkout -- gradle.properties pack.mcmeta src`，`git worktree remove`）；
   fabric/1.21.9 分支**不要提交**任何临时改动。

> 说明：1.21.8 与 1.21.9 数据格式相同（单数目录 + 字符串 ingredient），只需上述 API 差异处理。
