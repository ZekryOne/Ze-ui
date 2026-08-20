package music;

import java.io.IOException;
import java.util.Optional;

public final class MusicService {
    public String status() {
        return run("playerctl", "status").orElse("UNAVAILABLE");
    }

    public String metadata() {
        return run("playerctl", "metadata", "--format", "{{artist}} - {{title}}\\n{{album}}").orElse("");
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
