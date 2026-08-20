# Maintainer: ZekryOne
pkgname=assistant-linux
pkgver=0.20
pkgrel=1
pkgdesc='Assistant de bureau Linux en Java et Swing'
arch=('x86_64' 'aarch64')
url='https://github.com/ZekryOne/Ze-ui'
depends=('java-runtime>=21' 'playerctl' 'procps-ng' 'xdg-utils' 'hicolor-icon-theme')
makedepends=('git' 'java-environment>=21')
optdepends=('spotify: lecteur musical contrôlé via MPRIS'
            'spicetify-cli: alternative pour lancer Spotify')
source=('git+https://github.com/ZekryOne/Ze-ui.git#commit=f918e41')
sha256sums=('SKIP')

build() {
    cd "$srcdir/Ze-ui"
    rm -rf bin
    mkdir -p bin
    javac --release 21 -d bin $(find src -name '*.java' -print)
}

package() {
    cd "$srcdir/Ze-ui"
    install -dm755 "$pkgdir/usr/lib/assistant-linux/bin"
    install -dm755 "$pkgdir/usr/bin"
    install -dm755 "$pkgdir/usr/share/applications"
    install -dm755 "$pkgdir/usr/share/icons/hicolor/scalable/apps"

    cp -r bin/. "$pkgdir/usr/lib/assistant-linux/bin/"
    install -Dm755 assistant-launcher.sh "$pkgdir/usr/bin/assistant-linux"
    sed 's|__ASSISTANT_DIR__/assistant-launcher.sh|/usr/bin/assistant-linux|g; s|__ASSISTANT_DIR__|/usr/lib/assistant-linux|g' \
        assistant.desktop > "$pkgdir/usr/share/applications/assistant-linux.desktop"
    install -Dm644 assistant-linux.svg \
        "$pkgdir/usr/share/icons/hicolor/scalable/apps/assistant-linux.svg"
}