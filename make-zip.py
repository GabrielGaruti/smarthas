#!/usr/bin/env python3
"""
Gera smart-has.zip com o código-fonte do projeto.
Uso: python3 make-zip.py
"""

import zipfile
import os
from datetime import datetime

SRC = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(SRC, "smart-has.zip")

EXCLUDE_DIRS = {
    "node_modules", ".git", "build", ".dart_tool", ".pub-cache",
    "__pycache__", ".local", ".cache", ".flutter", ".gradle",
}
EXCLUDE_EXTS = {".pyc", ".db", ".tsbuildinfo"}
EXCLUDE_FILES = {
    "smarthas.db", ".flutter-plugins", ".flutter-plugins-dependencies",
    "smart-has.zip",
}

MIN_DATE = (1980, 1, 1, 0, 0, 0)

added = 0
with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as zf:
    for root, dirs, files in os.walk(SRC):
        dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]
        for file in files:
            if file in EXCLUDE_FILES:
                continue
            if any(file.endswith(e) for e in EXCLUDE_EXTS):
                continue
            full = os.path.join(root, file)
            arcname = os.path.relpath(full, SRC)
            try:
                info = zipfile.ZipInfo(arcname)
                mtime = os.path.getmtime(full)
                dt = datetime.fromtimestamp(mtime)
                info.date_time = dt.timetuple()[:6] if dt.year >= 1980 else MIN_DATE
                info.compress_type = zipfile.ZIP_DEFLATED
                with open(full, "rb") as f:
                    zf.writestr(info, f.read())
                added += 1
            except Exception as e:
                print(f"  aviso: ignorando {arcname} ({e})")

size_mb = os.path.getsize(OUT) / 1024 / 1024
print(f"✓ smart-has.zip gerado — {added} arquivos, {size_mb:.1f} MB")
print(f"  Caminho: {OUT}")
