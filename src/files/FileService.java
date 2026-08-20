package files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Collectors;

public final class FileService {
    public String open(String value) throws IOException {
        run("gio", "open", resolve(value).toString());
        return "Ouverture : " + value;
    }

    public String copy(String source, String destination) throws IOException {
        Files.copy(resolve(source), resolve(destination), StandardCopyOption.REPLACE_EXISTING);
        return "Copié : " + source + " -> " + destination;
    }

    public String move(String source, String destination) throws IOException {
        Files.move(resolve(source), resolve(destination), StandardCopyOption.REPLACE_EXISTING);
        return "Déplacé : " + source + " -> " + destination;
    }

    public String rename(String source, String name) throws IOException {
        Path sourcePath = resolve(source);
        Files.move(sourcePath, sourcePath.resolveSibling(name), StandardCopyOption.REPLACE_EXISTING);
        return "Renommé : " + source + " -> " + name;
    }

    public String delete(String value) throws IOException {
        Path target = resolve(value);
        if (Files.isDirectory(target)) {
            try (var paths = Files.walk(target)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> deletePath(path));
            }
        } else {
            Files.deleteIfExists(target);
        }
        return "Supprimé : " + value;
    }

    public String search(String text) throws IOException {
        try (var paths = Files.walk(Path.of(System.getProperty("user.home")), 5)) {
            String result = paths.filter(path -> path.getFileName().toString().toLowerCase().contains(text.toLowerCase()))
                    .limit(30).map(Path::toString).collect(Collectors.joining("\n"));
            return result.isBlank() ? "Aucun résultat." : result;
        }
    }

    public String mkdir(String value) throws IOException {
        Files.createDirectories(resolve(value));
        return "Dossier créé : " + value;
    }

    private Path resolve(String value) {
        if (value.startsWith("~/")) return Path.of(System.getProperty("user.home"), value.substring(2));
        return Path.of(value).toAbsolutePath().normalize();
    }

    private static void run(String... command) throws IOException {
        new ProcessBuilder(command).start();
    }

    private static void deletePath(Path path) {
        try { Files.deleteIfExists(path); } catch (IOException ignored) { }
    }
}
