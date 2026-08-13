## 变更概述
<!-- 一句话说明本次改了什么、为什么 -->

## 变更类型
- [ ] feat（新功能）
- [ ] fix（修复）
- [ ] docs（文档）
- [ ] style（格式，不影响逻辑）
- [ ] refactor（重构）
- [ ] perf（性能）
- [ ] test（测试）
- [ ] build / ci（构建 / 流水线）
- [ ] chore（杂项）

## 关联 Issue
<!-- 如：Closes #123 -->

## 自测清单
### 本地检查（push 前，不触发 CI）
- [ ] `./gradlew spotlessApply` 已执行，格式化通过
- [ ] 相关模块测试已通过（`./gradlew test`）
- [ ] 含跨层回归测试时已跑 `./gradlew integrationTest`
- [ ] 新增 bugfix：service 层单元测试 + 至少一个跨层回归测试
- [ ] 无敏感信息硬编码（密钥走环境变量）

### CI 门禁（由 ci.yml 强制执行）
- [ ] compileJava 通过
- [ ] test + integrationTest 通过
- [ ] JaCoCo 覆盖率 ≥94%
- [ ] bootJar 构建 + API 烟雾测试通过

## 测试计划
<!-- 说明如何验证本次变更：手测步骤 / 关键 API / 边界场景 -->

## 破坏性变更
<!-- 是否影响现有 API / 表结构 / 配置；如有，写清迁移步骤 -->

## 截图 / 录屏（前端或界面变更时）
<!-- 粘贴截图或录屏链接 -->
