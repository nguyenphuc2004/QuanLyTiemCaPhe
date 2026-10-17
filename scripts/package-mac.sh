#!/usr/bin/env bash
# Đóng gói ứng dụng thành file cài đặt macOS (.dmg) bằng jpackage.
#
# Yêu cầu : JDK 21 trở lên ĐẦY ĐỦ (có thư mục jmods). JBR đi kèm Android Studio/IntelliJ
#           không có jmods nên không dùng được; cài Temurin JDK từ https://adoptium.net
# Cách dùng: scripts/package-mac.sh                 (tự tìm JDK qua JAVA_HOME, ~/.local/jdk, /Library/Java)
#            JAVA_HOME=/duong/dan/jdk scripts/package-mac.sh
#            VENDOR="Tên công ty" scripts/package-mac.sh
# Kết quả  : target/installer/QuanLyTiemCaPhe-<phiên bản>.dmg
set -euo pipefail
cd "$(dirname "$0")/.."

APP_NAME="QuanLyTiemCaPhe"
MAIN_JAR="tiemcaphe.jar"
MAIN_CLASS="com.tiemcaphe.Launcher"
VENDOR="${VENDOR:-TiemCaPhe}"
APP_VERSION="$(sed -n 's:.*<version>\([0-9][0-9.]*\)</version>.*:\1:p' pom.xml | head -1)"
[ -n "$APP_VERSION" ] || APP_VERSION="1.0.0"

pick_jdk() {
  local c
  for c in "${JAVA_HOME:-}" "$HOME"/.local/jdk/*/Contents/Home /Library/Java/JavaVirtualMachines/*/Contents/Home; do
    [ -n "$c" ] && [ -x "$c/bin/jpackage" ] && [ -d "$c/jmods" ] && { echo "$c"; return 0; }
  done
  return 1
}
JDK="$(pick_jdk)" || {
  echo "Không tìm thấy JDK có jpackage + jmods. Cài Temurin JDK 21 rồi đặt JAVA_HOME trỏ tới nó." >&2
  exit 1
}
export JAVA_HOME="$JDK"
echo "==> Dùng JDK: $JAVA_HOME"

echo "==> Build jar (bỏ qua test)"
./mvnw -q -DskipTests package
[ -f "target/$MAIN_JAR" ] || { echo "Không thấy target/$MAIN_JAR sau khi build" >&2; exit 1; }

rm -rf target/jpackage-input target/installer
mkdir -p target/jpackage-input target/installer
cp "target/$MAIN_JAR" target/jpackage-input/

ICON_ARGS=()
[ -f packaging/icon.icns ] && ICON_ARGS=(--icon packaging/icon.icns)

echo "==> jpackage (.dmg)"
"$JAVA_HOME/bin/jpackage" \
  --type dmg \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --vendor "$VENDOR" \
  --description "Phần mềm bán hàng và quản lý tiệm cà phê" \
  --input target/jpackage-input \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --dest target/installer \
  --add-modules java.base,java.desktop,java.scripting,java.sql,java.logging,java.naming,java.prefs,java.xml,jdk.unsupported,jdk.jfr,jdk.localedata,jdk.charsets \
  --java-options "-Dfile.encoding=UTF-8" \
  --mac-package-identifier com.tiemcaphe \
  --mac-package-name "Tiem Ca Phe" \
  "${ICON_ARGS[@]}"

echo "==> Xong. File cài đặt:"
ls -1 target/installer/*.dmg
