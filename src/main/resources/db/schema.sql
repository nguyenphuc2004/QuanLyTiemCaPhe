-- Lược đồ CSDL SQLite cho phần mềm Quản lý tiệm cà phê
-- Thời gian lưu dạng TEXT 'yyyy-MM-dd HH:mm:ss' theo giờ máy.
-- Tiền tệ lưu số nguyên (VNĐ).

CREATE TABLE IF NOT EXISTS cai_dat (
  khoa    TEXT PRIMARY KEY,
  gia_tri TEXT
);

CREATE TABLE IF NOT EXISTS tai_khoan (
  id            INTEGER PRIMARY KEY AUTOINCREMENT,
  ten_dang_nhap TEXT NOT NULL UNIQUE COLLATE NOCASE,
  mat_khau      TEXT NOT NULL,
  ho_ten        TEXT NOT NULL,
  vai_tro       TEXT NOT NULL CHECK (vai_tro IN ('ADMIN','NHAN_VIEN')),
  hoat_dong     INTEGER NOT NULL DEFAULT 1,
  ngay_tao      TEXT NOT NULL DEFAULT (datetime('now','localtime'))
);

CREATE TABLE IF NOT EXISTS danh_muc (
  id    INTEGER PRIMARY KEY AUTOINCREMENT,
  ten   TEXT NOT NULL UNIQUE COLLATE NOCASE,
  thu_tu INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS mon (
  id            INTEGER PRIMARY KEY AUTOINCREMENT,
  ten           TEXT NOT NULL UNIQUE COLLATE NOCASE,
  danh_muc_id   INTEGER REFERENCES danh_muc(id) ON DELETE SET NULL,
  gia           INTEGER NOT NULL CHECK (gia >= 0),
  con_ban       INTEGER NOT NULL DEFAULT 1,
  hinh          BLOB,
  ngay_tao      TEXT NOT NULL DEFAULT (datetime('now','localtime')),
  ngay_cap_nhat TEXT NOT NULL DEFAULT (datetime('now','localtime'))
);

CREATE TABLE IF NOT EXISTS nguyen_lieu (
  id              INTEGER PRIMARY KEY AUTOINCREMENT,
  ten             TEXT NOT NULL UNIQUE COLLATE NOCASE,
  so_luong        REAL NOT NULL DEFAULT 0,
  don_vi          TEXT NOT NULL,
  nguong_canh_bao REAL NOT NULL DEFAULT 0,
  hinh            BLOB,
  ngay_cap_nhat   TEXT NOT NULL DEFAULT (datetime('now','localtime'))
);

-- Định lượng: 1 món dùng bao nhiêu nguyên liệu (để tự trừ kho khi bán)
CREATE TABLE IF NOT EXISTS dinh_luong (
  mon_id         INTEGER NOT NULL REFERENCES mon(id) ON DELETE CASCADE,
  nguyen_lieu_id INTEGER NOT NULL REFERENCES nguyen_lieu(id) ON DELETE CASCADE,
  so_luong       REAL NOT NULL CHECK (so_luong > 0),
  PRIMARY KEY (mon_id, nguyen_lieu_id)
);

CREATE TABLE IF NOT EXISTS don_hang (
  id            INTEGER PRIMARY KEY AUTOINCREMENT,
  ma_don        TEXT NOT NULL UNIQUE,
  tai_khoan_id  INTEGER REFERENCES tai_khoan(id) ON DELETE SET NULL,
  ten_nhan_vien TEXT NOT NULL,
  ban           TEXT,
  ghi_chu       TEXT,
  tong_tien     INTEGER NOT NULL,
  giam_gia      INTEGER NOT NULL DEFAULT 0,
  thanh_tien    INTEGER NOT NULL,
  khach_dua     INTEGER NOT NULL DEFAULT 0,
  tien_thua     INTEGER NOT NULL DEFAULT 0,
  phuong_thuc   TEXT NOT NULL CHECK (phuong_thuc IN ('TIEN_MAT','CHUYEN_KHOAN')),
  trang_thai    TEXT NOT NULL CHECK (trang_thai IN ('DA_THANH_TOAN','DA_HUY')),
  ly_do_huy     TEXT,
  ngay_tao      TEXT NOT NULL DEFAULT (datetime('now','localtime'))
);
CREATE INDEX IF NOT EXISTS idx_don_hang_ngay ON don_hang(ngay_tao);
CREATE INDEX IF NOT EXISTS idx_don_hang_trang_thai ON don_hang(trang_thai);

CREATE TABLE IF NOT EXISTS chi_tiet_don_hang (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  don_hang_id INTEGER NOT NULL REFERENCES don_hang(id) ON DELETE CASCADE,
  mon_id      INTEGER REFERENCES mon(id) ON DELETE SET NULL,
  ten_mon     TEXT NOT NULL,
  don_gia     INTEGER NOT NULL,
  so_luong    INTEGER NOT NULL CHECK (so_luong > 0),
  thanh_tien  INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_ctdh_don_hang ON chi_tiet_don_hang(don_hang_id);
CREATE INDEX IF NOT EXISTS idx_ctdh_mon ON chi_tiet_don_hang(mon_id);

-- Lịch sử biến động kho (nhập, xuất, điều chỉnh, trừ khi bán, hoàn khi hủy đơn)
CREATE TABLE IF NOT EXISTS bien_dong_kho (
  id              INTEGER PRIMARY KEY AUTOINCREMENT,
  nguyen_lieu_id  INTEGER NOT NULL REFERENCES nguyen_lieu(id) ON DELETE CASCADE,
  so_luong        REAL NOT NULL,
  loai            TEXT NOT NULL CHECK (loai IN ('NHAP','XUAT','DIEU_CHINH','BAN_HANG','HUY_DON')),
  ghi_chu         TEXT,
  tai_khoan_id    INTEGER REFERENCES tai_khoan(id) ON DELETE SET NULL,
  ten_nguoi_thuc_hien TEXT,
  don_hang_id     INTEGER REFERENCES don_hang(id) ON DELETE SET NULL,
  ngay_tao        TEXT NOT NULL DEFAULT (datetime('now','localtime'))
);
CREATE INDEX IF NOT EXISTS idx_bdk_nguyen_lieu ON bien_dong_kho(nguyen_lieu_id);
CREATE INDEX IF NOT EXISTS idx_bdk_don_hang ON bien_dong_kho(don_hang_id);
