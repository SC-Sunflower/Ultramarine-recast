# 1.21.8 构建说明（手动流程）

1.21.8 没有独立 git 分支；由 `fabric/1.21.9` 分支临时降级构建。1.21.9 分支代码使用了一些
1.21.9 才有的 API，构建 1.21.8 前需要做以下临时改动（构建完全部还原）：

1. `git worktree add C:\Users\34324\Desktop\um-1218 fabric/1.21.9`
2. 改 `gradle.properties`：
   - `minecraft_version=1.21.8`
   - `fabric_api_version=0.134.0+1.21.8`
3. 代码临时改动（1.21.8 API 差异，MCP 查证为准）：
   - Screen 类鼠标事件用旧的 `mouseClicked/mouseDragged` 签名（1.21.9 才改 MouseButtonEvent）
   - `SixSideBlock` / `WallSideBlock` 去掉 1.21.9 才有的 `noCollision()` 调用
   - `wind_chime.json` 配方 `minecraft:iron_chain` 改回 `minecraft:chain`
4. `pack.mcmeta` 用 1.21.8 传统格式：`pack_format: 81` + `supported_formats: [64, 81]`
5. 构建：`gradle build --console=plain --no-daemon`
6. 复制 `build/libs/ultramarine-recast-fabric-1.21.8-0.7.0.jar` 到主仓库 `release/`
7. 全部还原（`git checkout -- gradle.properties pack.mcmeta src`，`git worktree remove`）

> 说明：1.21.8 与 1.21.9 数据格式相同（单数目录 + 字符串 ingredient），只需上述 API 差异处理。
> fabric_loader_version 需 >=0.17.0（fabric-api 0.134 要求）。
