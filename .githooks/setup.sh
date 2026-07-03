#!/bin/bash
#
# 激活项目预定义的 git hooks
# 运行方式: bash .githooks/setup.sh
#
set -euo pipefail

HOOKS_DIR="$(cd "$(dirname "$0")" && pwd)"

git config core.hooksPath "$HOOKS_DIR"
echo "✅ Git hooks 已激活: ${HOOKS_DIR}"
echo ""
echo "当前注册的 hook:"
for hook in "$HOOKS_DIR"/*; do
    [ -x "$hook" ] || continue
    name="$(basename "$hook")"
    [ "$name" = "setup.sh" ] && continue
    echo "  - ${name}"
done
echo ""
echo "跳过方式: SKIP_CHECKS=1 git push"
