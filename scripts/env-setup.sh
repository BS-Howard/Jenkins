#!/usr/bin/env bash
set -euo pipefail

echo "🔧 開始安裝系統相依套件…"

# 例：安裝 unzip、zip 與 Docker CLI
if ! command -v zip >/dev/null; then
  apt-get update && apt-get install -y zip unzip
fi

if ! command -v docker >/dev/null; then
  echo "請先安裝 Docker 並將 Jenkins 使用者加入 docker 群組"
  exit 1
fi

echo "✅ 環境初始化完成。"