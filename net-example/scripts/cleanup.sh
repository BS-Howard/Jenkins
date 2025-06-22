#!/usr/bin/env bash
set -euo pipefail

echo "🧹 清理無標記（dangling）映像與停止容器…"
docker system prune -af
echo "✅ 清理完成。"
