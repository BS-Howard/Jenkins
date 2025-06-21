#!/usr/bin/env bash
set -e

HOST=${1:-localhost}
PORT=${2:-8081}

echo "🔍 檢查 ${HOST}:${PORT}…"
if curl --fail --silent "http://${HOST}:${PORT}/healthz"; then
  echo "✅ 服務正常（HTTP 200）"
else
  echo "🚨 健康檢查失敗！"
  exit 2
fi
