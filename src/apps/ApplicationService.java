package apps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ApplicationService {
    private static final Map<String, String> APPLICATION_ALIASES = Map.of(
            "visual studio code", "code",
            "gnome terminal", "gnome-terminal",
            "google chrome", "google-chrome",
            "mozilla firefox", "firefox"
    );

    public String launch(String application, List<String> arguments) throws IOException {
        List<String> requested = new ArrayList<>();
        requested.add(application);
        requested.addAll(arguments);
        List<String> command = resolveCommand(requested);
        new ProcessBuilder(command).start();
        return "Lancé : " + String.join(" ", command);
    }

    public String close(String application) throws IOException {
        new ProcessBuilder("pkill", "-x", application).start();
        return "Arrêt demandé : " + application;
    }

    public String find(String application) {
        for (String directory : System.getenv().getOrDefault("PATH", "").split(":")) {
            Path candidate = Path.of(directory, application);
            if (Files.isExecutable(candidate)) return "Disponible : " + candidate;
        }
        return "Application introuvable dans le PATH : " + application;
    }

    private List<String> resolveCommand(List<String> requested) {
        for (Map.Entry<String, String> alias : APPLICATION_ALIASES.entrySet()) {
            String[] aliasTokens = alias.getKey().split(" ");
            if (requested.size() < aliasTokens.length) continue;
            boolean matches = true;
            for (int index = 0; index < aliasTokens.length; index++) {
                if (!aliasTokens[index].equalsIgnoreCase(requested.get(index))) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                List<String> command = new ArrayList<>();
                command.add(alias.getValue());
                command.addAll(requested.subList(aliasTokens.length, requested.size()));
                return command;
            }
        }
        return requested;
    }
}
