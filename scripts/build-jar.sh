#!/usr/bin/env bash
# Build file jar chạy độc lập: target/tiemcaphe.jar (chạy bằng: java -jar target/tiemcaphe.jar, cần Java 21+).
# Jar chứa sẵn JavaFX theo NỀN TẢNG, mặc định là nền tảng của máy đang build.
# Build cho máy khác: scripts/build-jar.sh win | linux | mac | mac-aarch64
set -euo pipefail
cd "$(dirname "$0")/.."
PLATFORM="${1:-}"
if [ -n "$PLATFORM" ]; then
  ./mvnw -q -DskipTests package -Djavafx.platform="$PLATFORM"
  echo "Đã build target/tiemcaphe.jar cho nền tảng: $PLATFORM"
else
  ./mvnw -q -DskipTests package
  echo "Đã build target/tiemcaphe.jar cho nền tảng của máy này"
fi
