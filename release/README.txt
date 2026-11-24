# release 目录说明

本目录按 **Minecraft 版本号** 严格区分：每个正式版本一个独立 jar 文件
（即使 1.20/1.20.1 与 1.21/1.21.1 内容相同，也各有一个文件）。

| jar 文件 | 来源分支 | 说明 |
|---|---|---|
| ultramarine-recast-fabric-1.20-0.7.0.jar | main（1.20.1 构建）+ patch-mc-version.ps1 | bundled Porting Lib 依赖范围已改为 >=1.20 <1.20.2 |
| ultramarine-recast-fabric-1.20.1-0.7.0.jar | main | 需先 fetch-porting-lib.ps1 |
| ultramarine-recast-fabric-1.20.2 … 1.20.6 | fabric/1.20.2 … fabric/1.20.6 | |
| ultramarine-recast-fabric-1.21-0.7.0.jar | fabric/1.21.1 构建产物复制 | fabric.mod.json 范围 >=1.21 <1.21.2 |
| ultramarine-recast-fabric-1.21.1-0.7.0.jar | fabric/1.21.1 | |
| ultramarine-recast-fabric-1.21.2-0.7.0.jar | fabric/1.21.2 | |
| ultramarine-recast-fabric-1.21.3-0.7.0.jar | fabric/1.21.2 构建产物复制 | 数据格式与 1.21.2 完全一致，范围 >=1.21.2 <1.21.4 |
| ultramarine-recast-fabric-1.21.4 … 1.21.7 | fabric/1.21.4 … fabric/1.21.7 | |
| ultramarine-recast-fabric-1.21.8-0.7.0.jar | fabric/1.21.9 降级构建 | 见 scripts/notes-1.21.8-build.md |
| ultramarine-recast-fabric-1.21.9 … 1.21.11 | fabric/1.21.9 … fabric/1.21.11 | |

## 以后代码更新后如何同步

在主仓库跑一键脚本（会构建全部 15 个分支、生成变体并更新本目录）：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/sync-release.ps1
```

- 全量构建约 20-40 分钟（依赖首次下载较慢，之后走缓存）。
- 仅同步已构建产物（跳过构建）：加 `-OnlySync` 参数。
- 1.21.8 不自动构建：更新它时按 scripts/notes-1.21.8-build.md 手动构建后放回本目录。
