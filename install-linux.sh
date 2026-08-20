#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
DATA_DIR="${XDG_DATA_HOME:-$HOME/.local/share}"
APPLICATION_DIR="$DATA_DIR/applications"
ICON_DIR="$DATA_DIR/icons/hicolor/scalable/apps"
DESKTOP_FILE="$APPLICATION_DIR/assistant-linux.desktop"

mkdir -p "$APPLICATION_DIR" "$ICON_DIR"
chmod +x "$PROJECT_DIR/assistant-launcher.sh"
sed "s|__ASSISTANT_DIR__|$PROJECT_DIR|g" "$PROJECT_DIR/assistant.desktop" > "$DESKTOP_FILE"
cp "$PROJECT_DIR/assistant-linux.svg" "$ICON_DIR/assistant-linux.svg"
chmod 644 "$DESKTOP_FILE" "$ICON_DIR/assistant-linux.svg"

desktop-file-validate "$DESKTOP_FILE" 2>/dev/null || true
update-desktop-database "$APPLICATION_DIR" 2>/dev/null || true

echo "Assistant Linux est installe dans le menu des applications."
echo "Recherchez 'Assistant Linux', puis ajoutez-le aux favoris de la barre des taches."
