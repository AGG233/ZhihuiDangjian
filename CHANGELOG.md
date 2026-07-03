# Changelog

## [1.2.7](https://github.com/AGG233/ZhihuiDangjian/compare/v1.2.6...v1.2.7) (2026-07-03)


### Bug Fixes

* 移除 CD 中 buildx 镜像等待步骤，优化 CI/Docker/代码格式 ([216eb68](https://github.com/AGG233/ZhihuiDangjian/commit/216eb682279b9d23ba86faf292f8681b730c2fac))
* 移除 CD 中 buildx 镜像等待步骤，优化 CI/Docker/代码格式 ([1cb71b5](https://github.com/AGG233/ZhihuiDangjian/commit/1cb71b513077f0daff10f4f9676b7a14c0bfb9b2))

## [1.2.6](https://github.com/AGG233/ZhihuiDangjian/compare/v1.2.5...v1.2.6) (2026-07-02)


### Bug Fixes

* 修复测验创建参数校验和评论列表空集合查询问题 ([c198411](https://github.com/AGG233/ZhihuiDangjian/commit/c19841122dbf253c7333909dc545ec4de0a67b26))
* 修复测验创建参数校验和评论列表空集合查询问题 ([7cfbc12](https://github.com/AGG233/ZhihuiDangjian/commit/7cfbc12d59684e8d299e099ba8096d1557e8e805))

## [1.2.5](https://github.com/AGG233/ZhihuiDangjian/compare/v1.2.4...v1.2.5) (2026-07-01)


### Bug Fixes

* 修复 trigger-cd 因缺少 .git 导致 gh workflow run 失败 ([3cf78f4](https://github.com/AGG233/ZhihuiDangjian/commit/3cf78f43cc6d78d48f0f6227d5affb4b6b120a1f))

## [1.2.4](https://github.com/AGG233/ZhihuiDangjian/compare/v1.2.3...v1.2.4) (2026-06-28)


### Bug Fixes

* 修复 dev 模式 DevAutoLoginFilter 并发踢下线导致 500 ([146d5bd](https://github.com/AGG233/ZhihuiDangjian/commit/146d5bd73a63bca9ca8adb2c679485bdc143fff1))
* 修复 Sa-Token 白名单路径错误，/api/schools/list 改为 /api/school/all ([bd9fcf2](https://github.com/AGG233/ZhihuiDangjian/commit/bd9fcf26b553b996c27cf0803a01b4a912f42ae6))


### Performance Improvements

* 调大 HikariCP/Tomcat/Redis 连接池适配生产负载 ([b11c9cf](https://github.com/AGG233/ZhihuiDangjian/commit/b11c9cf63e60da2a1e7e760a32c2e0cd5bb1a545))

## [1.2.3](https://github.com/AGG233/ZhihuiDangjian/compare/v1.2.2...v1.2.3) (2026-06-28)


### Bug Fixes

* 修复 Release Please 配置文件名（缺少前置点）并将 release-type 改为 simple ([eb3072f](https://github.com/AGG233/ZhihuiDangjian/commit/eb3072f6b1d1b967db7f6c92d90a68566f75b0fe))
* 错误的文件名 ([fea80d6](https://github.com/AGG233/ZhihuiDangjian/commit/fea80d6b11f45f3e772ea511d7babb343ec01caa))

## [1.2.2](https://github.com/AGG233/ZhihuiDangjian/compare/v1.2.1...v1.2.2) (2026-06-28)


### Bug Fixes

* 避免无PR的情况下继续执行 ([ce4df6a](https://github.com/AGG233/ZhihuiDangjian/commit/ce4df6a8d50e49b96931ca9b1c51f64be329f3b2))

## [1.2.1](https://github.com/AGG233/ZhihuiDangjian/compare/v1.2.0...v1.2.1) (2026-06-28)


### Bug Fixes

* **ci:** 将编译步骤改为打包并上传 JAR 供下游复用 ([6f2efce](https://github.com/AGG233/ZhihuiDangjian/commit/6f2efce67d763144171342fb99c729a8653f6091))
* **ci:** 恢复 product 分支的 PR 触发，确保 dev-&gt;product PR 正确触发 CI ([a8d76b0](https://github.com/AGG233/ZhihuiDangjian/commit/a8d76b01bf0216e90e07170d5d02ac72faef2f09))
* **release-please:** pr 输出为 JSON 对象，需用 fromJSON().number 提取编号 ([9a8c362](https://github.com/AGG233/ZhihuiDangjian/commit/9a8c3625a7bb1b41ed8e59177b3c90e86b93e308))
* **release-please:** 将 PR 编号从 ${{}} 内联改为 env 传递，避免 bash 语法错误 ([ea63215](https://github.com/AGG233/ZhihuiDangjian/commit/ea632158a1eedec4da19bb0e109fcbc1f3ea95c8))
* 修复 CodeRabbit 报告的 6 个工作流问题 ([8e62b8e](https://github.com/AGG233/ZhihuiDangjian/commit/8e62b8e9ea181ceea37a2671f0b378cf564a5d1b))
* 将 release-please-action 固定到完整 commit SHA 以防止供应链攻击 ([ee9b762](https://github.com/AGG233/ZhihuiDangjian/commit/ee9b76230121aa199b5423abf01d8bea13f1e5aa))
* 正确的文件名 ([5e020cb](https://github.com/AGG233/ZhihuiDangjian/commit/5e020cbf1a377c66e3451511822916f0a67e31a4))
* 正确的文件名 ([e44524d](https://github.com/AGG233/ZhihuiDangjian/commit/e44524de696f13166cf33850a6565bc490934646))


### Performance Improvements

* 避免 cd.yml 中重复拉取 Docker 镜像 ([e988c8b](https://github.com/AGG233/ZhihuiDangjian/commit/e988c8b72ec10e6b7feb268f139c4c4c003739b1))


### Reverts

* 回退 PR [#59](https://github.com/AGG233/ZhihuiDangjian/issues/59) 中混入的无关测试文件变更（34个测试文件） ([f7a7987](https://github.com/AGG233/ZhihuiDangjian/commit/f7a798768ac4b9802b40dce30420bcfc62914d85))
