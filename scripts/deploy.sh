#!/usr/bin/env bash
set -euo pipefail

IMAGE="jenkins-demo:latest"
CONTAINER="jenkins-demo"
PORT=8081

echo "🚀 部署新版本…"
# build image
docker build -t ${IMAGE} .

# 如果舊容器存在就先停並移除
docker ps -a --filter "name=${CONTAINER}" --format '{{.ID}}' | \
  xargs -r docker rm -f

# 啟動容器
docker run -d -p ${PORT}:80 --name ${CONTAINER} ${IMAGE}

echo "✅ 部署完成，容器已於 ${PORT} 埠啟動。"
