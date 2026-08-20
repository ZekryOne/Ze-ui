package apps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ApplicationService {
    public String launch(String application, List<String> arguments) throws IOException {
        List<String> command = new ArrayList<>();
        command.add(application);
        command.addAll(arguments);
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
}
