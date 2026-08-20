package system;

import java.io.IOException;
import java.io.BufferedReader;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;

public final class SystemService {
    public String status() {
        return String.join("\n", cpu(), memory(), disk(), battery(), uptime(), os());
    }

    public String cpu() {
        double load = java.lang.management.ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
        int processors = Runtime.getRuntime().availableProcessors();
        if (load < 0) return "CPU: indisponible";
        return "CPU: " + Math.round(Math.min(100, load / processors * 100)) + "% charge";
    }

    public String memory() {
        try {
            long total = 0;
            long available = 0;
            try (var lines = Files.lines(Path.of("/proc/meminfo"))) {
                for (String line : (Iterable<String>) lines::iterator) {
                if (line.startsWith("MemTotal:")) total = valueInKb(line);
                if (line.startsWith("MemAvailable:")) available = valueInKb(line);
                if (total > 0 && available > 0) break;
                }
            }
            long used = total - available;
            return "RAM: " + formatMemory(used) + " / " + formatMemory(total);
        } catch (IOException e) {
            return "RAM: indisponible";
        }
    }

    public String disk() {
        var store = Path.of(System.getProperty("user.home")).toFile().toPath();
        var fs = store.toFile();
        long total = fs.getTotalSpace();
        long free = fs.getUsableSpace();
        return "Disque: " + formatMb(total - free) + " / " + formatMb(total);
    }

    public String battery() {
        try {
            Path power = Path.of("/sys/class/power_supply");
            try (var entries = Files.list(power)) {
                var capacity = entries.map(entry -> entry.resolve("capacity"))
                        .filter(Files::isReadable).findFirst();
                return capacity.map(path -> "Batterie: " + read(path) + "%").orElse("Batterie: non détectée");
            }
        } catch (IOException e) {
            return "Batterie: indisponible";
        }
    }

    public String uptime() {
        String value = read(Path.of("/proc/uptime"));
        if (value.isBlank()) return "Uptime: indisponible";
        long seconds = (long) Double.parseDouble(value.split(" ")[0]);
        return "Uptime: " + (seconds / 3600) + " h " + (seconds / 60 % 60) + " min";
    }

    public String os() {
        return "OS: " + read(Path.of("/etc/os-release")).lines()
                .filter(line -> line.startsWith("PRETTY_NAME="))
                .map(line -> line.substring("PRETTY_NAME=".length()).replace("\"", ""))
                .findFirst().orElse("Linux");
    }

    public String network() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            return "Réseau: " + address.getHostName() + " (" + address.getHostAddress() + ")";
        } catch (IOException e) {
            return "Réseau: indisponible";
        }
    }

    public String optimizeRam() {
        String before = memory();
        System.gc();
        return "RAM analysée : récupération mémoire demandée pour l'assistant.\nAvant : " + before + "\nAprès : " + memory();
    }

    public String optimizeCpu() {
        String load = cpu();
        try {
            Process process = new ProcessBuilder("ps", "-eo", "comm,%cpu", "--sort=-%cpu")
                    .redirectErrorStream(true).start();
            String top;
            try (BufferedReader reader = process.inputReader()) {
                reader.readLine();
                top = reader.lines().limit(6).collect(Collectors.joining("\n"));
            } finally {
                process.destroy();
            }
            return "CPU analysé : " + load + "\nProcessus les plus actifs :\n" + top
                    + "\nAucun processus n'a été arrêté automatiquement.";
        } catch (IOException error) {
            return "CPU analysé : " + load + "\nListe des processus indisponible.";
        }
    }

    public String diskOptimizationPreview() {
        Path trash = Path.of(System.getProperty("user.home"), ".local/share/Trash/files");
        return "Corbeille utilisateur : " + formatMb(directorySize(trash))
                + "\nAction proposée : vider uniquement la corbeille utilisateur.";
    }

    public String cleanUserTrash() {
        Path trash = Path.of(System.getProperty("user.home"), ".local/share/Trash/files");
        try {
            if (Files.exists(trash)) {
                try (var paths = Files.walk(trash)) {
                    paths.sorted(Comparator.reverseOrder()).filter(path -> !path.equals(trash))
                            .forEach(SystemService::deleteQuietly);
                }
            }
            return "Corbeille utilisateur vidée.\n" + disk();
        } catch (IOException error) {
            return "Nettoyage du disque impossible : " + error.getMessage();
        }
    }

    private static long directorySize(Path directory) {
        if (!Files.isDirectory(directory)) return 0;
        try (var paths = Files.walk(directory)) {
            return paths.filter(Files::isRegularFile).mapToLong(path -> {
                try { return Files.size(path); } catch (IOException ignored) { return 0; }
            }).sum();
        } catch (IOException ignored) { return 0; }
    }

    private static void deleteQuietly(Path path) {
        try { Files.deleteIfExists(path); } catch (IOException ignored) { }
    }

    private static long valueInKb(String line) {
        long value = 0;
        boolean reading = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character >= '0' && character <= '9') {
                value = value * 10 + character - '0';
                reading = true;
            } else if (reading) {
                break;
            }
        }
        return value;
    }

    private static String formatMb(long bytes) {
        return Math.max(0, bytes / 1024 / 1024) + " MB";
    }

    private static String formatMemory(long kib) {
        return Math.max(0, kib / 1024) + " MB";
    }

    private static String read(Path path) {
        try { return Files.readString(path).trim(); } catch (IOException e) { return ""; }
    }

}
