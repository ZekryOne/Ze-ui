#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
JAVA_BIN="${JAVA_HOME:-}/bin/java"
if [[ ! -x "$JAVA_BIN" ]]; then
    JAVA_BIN="$(command -v java)"
fi

if [[ ! -d "$PROJECT_DIR/bin" ]] || ! find "$PROJECT_DIR/bin" -name '*.class' -print -quit | grep -q .; then
    mkdir -p "$PROJECT_DIR/bin"
    javac --release 21 -d "$PROJECT_DIR/bin" $(find "$PROJECT_DIR/src" -name '*.java' -print)
fi

JAVA_OPTS=("-Xms16m" "-Xmx256m" "-XX:+UseSerialGC" "-Dsun.awt.X11.awtClassName=Assistant")
if [[ -n "${ASSISTANT_JAVA_OPTS:-}" ]]; then
    read -r -a EXTRA_JAVA_OPTS <<< "$ASSISTANT_JAVA_OPTS"
    JAVA_OPTS+=("${EXTRA_JAVA_OPTS[@]}")
fi

exec "$JAVA_BIN" "${JAVA_OPTS[@]}" -cp "$PROJECT_DIR/bin" App "$@"
