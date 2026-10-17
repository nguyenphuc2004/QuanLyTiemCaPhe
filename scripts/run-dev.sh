#!/usr/bin/env bash
# Chạy ứng dụng ở chế độ phát triển (không cần đóng gói).
# Thêm tham số --demo để tạo đơn hàng mẫu 30 ngày gần nhất:  scripts/run-dev.sh --demo
set -euo pipefail
cd "$(dirname "$0")/.."
if [ $# -gt 0 ]; then
  exec ./mvnw -q javafx:run -Djavafx.args="$*"
fi
exec ./mvnw -q javafx:run
