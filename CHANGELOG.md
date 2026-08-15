# Changelog

## [0.9.0](https://github.com/AGG233/ZhihuiDangjian/compare/v0.8.4...v0.9.0) (2026-08-15)


### Features

* **ai:** add knowledge graph evaluation tool ([#106](https://github.com/AGG233/ZhihuiDangjian/issues/106)) ([b5ec534](https://github.com/AGG233/ZhihuiDangjian/commit/b5ec5343de2060891a88346901f3f4ba2d225fe7))
* **auth:** adopt JWT token via sa-token-jwt ([#105](https://github.com/AGG233/ZhihuiDangjian/issues/105)) ([bfadaab](https://github.com/AGG233/ZhihuiDangjian/commit/bfadaabf26068d2bf1b60e8794f15c9256b4afb9))
* **search:** enrich user profile with interaction stats and CF fusion ([#107](https://github.com/AGG233/ZhihuiDangjian/issues/107)) ([b2c8821](https://github.com/AGG233/ZhihuiDangjian/commit/b2c88215245bb2856320e7d643af97baac5800ac))


### Bug Fixes

* **migrations:** align legacy comment/user_like tables with new schema ([#103](https://github.com/AGG233/ZhihuiDangjian/issues/103)) ([b6a7d42](https://github.com/AGG233/ZhihuiDangjian/commit/b6a7d42f9d48631c76aa8f69cb8301ec95eb364c))
* **migrations:** renumber new V8-V10 to V12-V14 to avoid legacy checksum conflict ([#102](https://github.com/AGG233/ZhihuiDangjian/issues/102)) ([de44cf6](https://github.com/AGG233/ZhihuiDangjian/commit/de44cf68fca1881bdbb7b9905fb60628d8c24b9d))
* **migrations:** renumber V5-V7 to V8-V10 and ignore legacy missing migrations ([#100](https://github.com/AGG233/ZhihuiDangjian/issues/100)) ([2992ee7](https://github.com/AGG233/ZhihuiDangjian/commit/2992ee79f177b5b0c59355b34982114362a9e593))

## [0.8.4](https://github.com/AGG233/ZhihuiDangjian/compare/v0.8.3...v0.8.4) (2026-08-15)


### Bug Fixes

* **ci:** disable provenance for aliyun acr push ([#95](https://github.com/AGG233/ZhihuiDangjian/issues/95)) ([ebd6687](https://github.com/AGG233/ZhihuiDangjian/commit/ebd66871d8e25fa5c67fae7823421a674794575a))
* **ci:** inject app image inside ssh script ([f5d1197](https://github.com/AGG233/ZhihuiDangjian/commit/f5d11973773a589c20508927ad80da5fa78ae48a))
* **migrations:** renumber conflicting V5-V7 to V8-V10 ([#98](https://github.com/AGG233/ZhihuiDangjian/issues/98)) ([50e157f](https://github.com/AGG233/ZhihuiDangjian/commit/50e157fd2310d4ac0f4ae332f3c7bad8337b2712))

## [0.8.3](https://github.com/AGG233/ZhihuiDangjian/compare/v0.8.2...v0.8.3) (2026-08-15)


### Bug Fixes

* resolve review findings from [#86](https://github.com/AGG233/ZhihuiDangjian/issues/86) [#88](https://github.com/AGG233/ZhihuiDangjian/issues/88) [#89](https://github.com/AGG233/ZhihuiDangjian/issues/89) ([#92](https://github.com/AGG233/ZhihuiDangjian/issues/92)) ([4cc8801](https://github.com/AGG233/ZhihuiDangjian/commit/4cc8801fcd15471094fd52add483271be83d63b1))

## [0.8.2](https://github.com/AGG233/ZhihuiDangjian/compare/v0.8.1...v0.8.2) (2026-08-15)


### Bug Fixes

* correct silent failures and concurrency races ([#88](https://github.com/AGG233/ZhihuiDangjian/issues/88)) ([c39be97](https://github.com/AGG233/ZhihuiDangjian/commit/c39be973d5a8648d2eed8e7bc6897883ca451094))

## [0.8.1](https://github.com/AGG233/ZhihuiDangjian/compare/v0.8.0...v0.8.1) (2026-08-15)


### Bug Fixes

* **auth,user,resource:** security hardening ([#86](https://github.com/AGG233/ZhihuiDangjian/issues/86)) ([ce82370](https://github.com/AGG233/ZhihuiDangjian/commit/ce82370f579c5efc17f1f1bc4d4ce0f5a108b9b5))

## [0.8.0](https://github.com/AGG233/ZhihuiDangjian/compare/v0.7.4...v0.8.0) (2026-08-14)


### Features

* **release:** integrate release-please automation ([#84](https://github.com/AGG233/ZhihuiDangjian/issues/84)) ([7664d95](https://github.com/AGG233/ZhihuiDangjian/commit/7664d955d6b6774f424233156438a7b15feae653))
