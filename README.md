# Ze-ui

Assistant de bureau Linux léger, basé sur Java 21 et Swing. Il sert de launcher, d'outil système et de gestionnaire de fichiers. L'application fonctionne sans IA, sans synthèse vocale et sans bibliothèque externe.

## Installation

Après avoir téléchargé ou cloné le projet :

```bash
cd Ze-ui
java -version
mkdir -p bin
javac --release 21 -d bin $(find src -name '*.java' -print)
./install-linux.sh
```

Java 21 ou une version plus récente est nécessaire. Le script d'installation ajoute **Ze-ui** au menu des applications Linux Mint avec son icône. Pour l'ajouter à la barre des tâches, ouvrez le menu, cherchez **Ze-ui**, puis choisissez **Ajouter aux favoris**.

## Lancer

```bash
./assistant-launcher.sh
```

Pour lancer l'assistant sans afficher la fenêtre :

```bash
./assistant-launcher.sh --background
```

Le raccourci local est `Ctrl+Space`. Un raccourci global comme `Super+Space` peut être configuré dans les paramètres clavier de Linux Mint avec la commande :

```bash
/chemin/vers/Ze-ui/assistant-launcher.sh
```

## Commandes

```text
help
ouvrir <application|fichier|dossier|url>
fermer <application>
copier <source> dans <destination>
deplacer <source> vers <destination>
renommer <source> en <nom>
supprimer <fichier>
rechercher <texte>
installer <paquet>
optimiser [ram|cpu|disque|tout]
cpu, ram, disque, batterie, reseau, etat
quitter
```

Les commandes courantes sont reconnues en français, anglais et russe. Les opérations sensibles demandent une confirmation. Le nettoyage du disque se limite à la corbeille utilisateur.

## Interface

L'interface propose trois langues (`FR`, `EN`, `RU`), trois palettes (`MATRIX`, `CYBER`, `AMBER`) et une couleur d'accent personnalisable. Les préférences sont enregistrées dans `~/.config/assistant/ui.properties`.

## Musique

L'onglet **MUSIC** contrôle Spotify et Spicetify avec le protocole MPRIS via `playerctl` : lecture/pause, piste précédente, piste suivante et affichage du morceau en cours. L'animation ASCII ne se met à jour que lorsque l'onglet musique est ouvert et qu'un morceau joue.

Installez le composant système si nécessaire :

```bash
sudo apt install playerctl
```

Les commandes disponibles sont `music status`, `music play-pause`, `music previous` et `music next`.

## Structure

```text
src/
├── apps/       lancement et recherche d'applications
├── commands/   parsing des commandes et alias
├── core/       orchestration
├── files/      opérations sur les fichiers
├── network/    navigateur et recherche web
├── startup/    démarrage automatique
├── system/     état et optimisation système
└── ui/         interface Swing
```

Les fichiers `Main.py`, `Jarvis.js` et `Source.C++` sont conservés pour de futures extensions.
