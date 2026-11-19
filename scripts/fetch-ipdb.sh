#!/usr/bin/env bash
# 下载离线 IP 归属地库（ip2region.xdb，约 10MB），供大屏地图解析攻击源地域
set -e
cd "$(dirname "$0")/.."
OUT="data/ip2region.xdb"
mkdir -p data

if [ -s "$OUT" ]; then
  echo "[skip] $OUT 已存在（$(du -h "$OUT" | cut -f1)）"
  exit 0
fi

URL="${IPDB_URL:-https://registry.npmmirror.com/ip2region-ts/-/ip2region-ts-2.0.1.tgz}"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

echo "[get ] $URL"
curl -fL --retry 3 --connect-timeout 15 "$URL" -o "$TMP/p.tgz"
tar -xzf "$TMP/p.tgz" -C "$TMP"
cp "$TMP/package/data/ip2region.xdb" "$OUT"
echo "[ok  ] $OUT（$(du -h "$OUT" | cut -f1)）"
