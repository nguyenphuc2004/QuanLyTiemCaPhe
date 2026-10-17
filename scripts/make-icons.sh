#!/usr/bin/env bash
# Tạo icon đóng gói từ src/main/resources/img/logo.png (512x512):
#   packaging/icon.icns (macOS)  và  packaging/icon.ico (Windows)
# Chỉ chạy được trên macOS (dùng sips + iconutil); .ico được ghép bằng python3 có sẵn.
set -euo pipefail
cd "$(dirname "$0")/.."
SRC="src/main/resources/img/logo.png"
OUT="packaging"
[ -f "$SRC" ] || { echo "Không thấy $SRC" >&2; exit 1; }
mkdir -p "$OUT/tmp" "$OUT/icon.iconset"

# PNG các cỡ
for s in 16 32 48 64 128 256 512; do
  sips -s format png -z "$s" "$s" "$SRC" --out "$OUT/tmp/icon-$s.png" >/dev/null
done

# .icns
cp "$OUT/tmp/icon-16.png"  "$OUT/icon.iconset/icon_16x16.png"
cp "$OUT/tmp/icon-32.png"  "$OUT/icon.iconset/icon_16x16@2x.png"
cp "$OUT/tmp/icon-32.png"  "$OUT/icon.iconset/icon_32x32.png"
cp "$OUT/tmp/icon-64.png"  "$OUT/icon.iconset/icon_32x32@2x.png"
cp "$OUT/tmp/icon-128.png" "$OUT/icon.iconset/icon_128x128.png"
cp "$OUT/tmp/icon-256.png" "$OUT/icon.iconset/icon_128x128@2x.png"
cp "$OUT/tmp/icon-256.png" "$OUT/icon.iconset/icon_256x256.png"
cp "$OUT/tmp/icon-512.png" "$OUT/icon.iconset/icon_256x256@2x.png"
cp "$OUT/tmp/icon-512.png" "$OUT/icon.iconset/icon_512x512.png"
iconutil -c icns "$OUT/icon.iconset" -o "$OUT/icon.icns"

# .ico (định dạng ICO chứa PNG, Windows Vista trở lên đọc được)
python3 -I - "$OUT/tmp" "$OUT/icon.ico" <<'PY'
import struct, sys, os
tmp, out = sys.argv[1], sys.argv[2]
sizes = [16, 32, 48, 64, 128, 256]
pngs = [open(os.path.join(tmp, f"icon-{s}.png"), "rb").read() for s in sizes]
header = struct.pack("<HHH", 0, 1, len(pngs))
offset = 6 + 16 * len(pngs)
entries, data = b"", b""
for s, png in zip(sizes, pngs):
    w = 0 if s == 256 else s
    entries += struct.pack("<BBBBHHII", w, w, 0, 0, 1, 32, len(png), offset)
    data += png
    offset += len(png)
with open(out, "wb") as f:
    f.write(header + entries + data)
PY

rm -rf "$OUT/tmp" "$OUT/icon.iconset"
ls -la "$OUT"
