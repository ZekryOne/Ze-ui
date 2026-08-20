package music;

import java.io.IOException;
import java.util.Optional;

public final class MusicService {
    public String status() {
        return run("playerctl", "status").orElse("UNAVAILABLE");
    }

    public String metadata() {
        return run("playerctl", "metadata", "--format", "{{artist}} - {{title}} | {{album}}").orElse("");
    }

    public String playPause() {
        return action("play-pause");
    }

    public String next() {
        return action("next");
    }

    public String previous() {
        return action("previous");
    }

    public String volume() {
        return run("playerctl", "volume").map(value -> "Volume : " + Math.round(Double.parseDouble(value) * 100) + "%")
                .orElse(installHint());
    }

    public String changeVolume(int percentage) {
        Optional<String> current = run("playerctl", "volume");
        if (current.isEmpty()) return installHint();
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
                + "Installation : sudo apt install playerctl";
    }

    private String action(String action) {
        return run("playerctl", action).isPresent() ? "Commande musicale envoyée : " + action
                : installHint();
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
