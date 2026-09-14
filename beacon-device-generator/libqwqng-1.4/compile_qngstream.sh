#!/bin/bash
# ==============================================================================
# Script de compilação do utilitário qngstream no CentOS / Linux
# ==============================================================================
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
OUTPUT="$DIR/qngstream"

echo "[INFO] Localizando diretórios de inclusão e bibliotecas..."

# Localiza headers
INCLUDE_DIR=""
for inc in "$DIR/libqwqng-1.4/src" "$DIR/src" "$HOME/libqwqng-1.4/src" "$HOME/libqwqng-1.4/libqwqng-1.4/src" /usr/local/include; do
    if [ -f "$inc/qwqng.hpp" ]; then
        INCLUDE_DIR="-I$inc"
        echo "[INFO] Headers encontrados em: $inc"
        break
    fi
done

# Localiza onde está a libqwqng.so.1
LIB_DIRS=("/usr/local/lib" "/usr/local/lib64" "/usr/lib" "/usr/lib64")
for candidate in \
    "$DIR/libqwqng-1.4/build/src" \
    "$DIR/build/src" \
    "$HOME/libqwqng-1.4/build/src" \
    "$HOME/libqwqng-1.4/libqwqng-1.4/build/src" \
    "/home/beacon/libqwqng-1.4/build/src" \
    "/home/beacon/libqwqng-1.4/libqwqng-1.4/build/src"; do
    if [ -d "$candidate" ]; then
        LIB_DIRS+=("$candidate")
    fi
done

LDFLAGS=""
RPATH_FLAGS=""
for libdir in "${LIB_DIRS[@]}"; do
    if [ -d "$libdir" ]; then
        LDFLAGS="$LDFLAGS -L$libdir"
        RPATH_FLAGS="$RPATH_FLAGS -Wl,-rpath,$libdir"
    fi
done

echo "[INFO] Compilando qngstream com g++ e embutindo RPATH..."
g++ -O3 "$DIR/qngstream.cpp" $INCLUDE_DIR $LDFLAGS $RPATH_FLAGS -lqwqng -lftdi1 -lusb-1.0 -lpthread -o "$OUTPUT"

echo "[OK] Compilado com sucesso: $OUTPUT"
echo ""
echo "Teste agora diretamente:"
echo "  $OUTPUT | head -n 1"
