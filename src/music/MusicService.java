package music;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class MusicService {
    public String status() {
        if (!commandAvailable("playerctl")) return "PLAYERCTL_MISSING";
        return run("playerctl", "status").orElse("UNAVAILABLE");
    }

    public String metadata() {
        return run("playerctl", "metadata", "--format", "{{artist}} - {{title}} | {{album}}").orElse("");
    }

    public String playPause() {
        if (!commandAvailable("playerctl")) return installHint();
        if (run("playerctl", "status").isEmpty()) return play();
        return action("play-pause");
    }

    public String stop() {
        if (!commandAvailable("playerctl")) return installHint();
        return action("pause");
    }

    public String play() {
        if (!commandAvailable("playerctl")) return installHint();
        if (run("playerctl", "status").isEmpty()) {
            Optional<List<String>> player = installedPlayer();
            if (player.isEmpty()) return playerHint();
            try {
                if (!processRunning("spotify")) new ProcessBuilder(player.get()).start();
                if (!waitForPlayer()) return "Spotify a été lancé, mais MPRIS n'est pas encore disponible.";
            } catch (IOException error) {
                return "Impossible de lancer Spotify : " + error.getMessage();
            }
        }
        return action("play");
    }

    public String next() {
        return action("next");
    }

    public String previous() {
        return action("previous");
    }

    public String volume() {
        if (!commandAvailable("playerctl")) return installHint();
        return run("playerctl", "volume").map(value -> "Volume : " + Math.round(Double.parseDouble(value) * 100) + "%")
            .orElse("Aucun lecteur MPRIS détecté.");
    }

    public String changeVolume(int percentage) {
        if (!commandAvailable("playerctl")) return installHint();
        Optional<String> current = run("playerctl", "volume");
        if (current.isEmpty()) return "Aucun lecteur MPRIS détecté.";
        try {
            double next = Math.max(0, Math.min(1, Double.parseDouble(current.get()) + percentage / 100.0));
            run("playerctl", "volume", String.format(java.util.Locale.ROOT, "%.2f", next));
            return volume();
        } catch (NumberFormatException error) {
            return "Volume indisponible.";
        }
    }

    public String installHint() {
        return "playerctl est requis pour contrôler Spotify via MPRIS.\n"
                + "Installez-le avec le gestionnaire de paquets de votre distribution.";
    }

    private String playerHint() {
        return "Spotify ou Spicetify est requis pour démarrer la musique.";
    }

    private Optional<List<String>> installedPlayer() {
        if (commandAvailable("spotify")) return Optional.of(List.of("spotify"));
        if (commandAvailable("spicetify") && commandAvailable("xdg-open")) {
            return Optional.of(List.of("xdg-open", "spotify:"));
        }
        return Optional.empty();
    }

    private boolean commandAvailable(String command) {
        String path = System.getenv("PATH");
        if (path == null) return false;
        for (String directory : path.split(java.util.regex.Pattern.quote(java.io.File.pathSeparator))) {
            if (Files.isExecutable(Path.of(directory, command))) return true;
        }
        return false;
    }

    private boolean waitForPlayer() {
        for (int attempt = 0; attempt < 10; attempt++) {
            if (run("playerctl", "status").isPresent()) return true;
            try {
                Thread.sleep(300);
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private boolean processRunning(String process) {
        return run("pgrep", "-x", process).isPresent();
    }

    private String action(String action) {
        return run("playerctl", action).isPresent() ? "Commande musicale envoyée : " + action
                : "Aucun lecteur MPRIS détecté.";
    }

    private Optional<String> run(String... command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes()).trim();
            int exit = process.waitFor();
            return exit == 0 ? Optional.of(output) : Optional.empty();
        } catch (IOException error) {
            return Optional.empty();
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
}
