package core;

import apps.ApplicationService;
import commands.CommandParser;
import commands.ParsedCommand;
import files.FileService;
import network.NetworkService;
import startup.StartupService;
import system.SystemService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AssistantEngine {
    private static final Logger LOG = Logger.getLogger(AssistantEngine.class.getName());
    private final CommandParser parser = new CommandParser();
    private final ApplicationService applications = new ApplicationService();
    private final FileService files = new FileService();
    private final NetworkService network = new NetworkService();
    private final SystemService system = new SystemService();
    private final StartupService startup = new StartupService();

    public String execute(String input, Predicate<String> confirm) {
        ParsedCommand command = parser.parse(input);
        try {
            return switch (command.name()) {
                case "", "help" -> help();
                case "ouvrir", "lancer" -> open(command.arguments());
                case "fermer" -> requires(command, confirm, "Arrêter " + first(command),
                        () -> applications.close(first(command)));
                case "copier" -> files.copy(argument(command, 0), destination(command));
                case "deplacer" -> files.move(argument(command, 0), destination(command));
                case "renommer" -> files.rename(argument(command, 0), argument(command, 2, "en"));
                case "supprimer" -> requires(command, confirm, "Supprimer définitivement " + first(command),
                        () -> files.delete(first(command)));
                case "rechercher" -> search(command.arguments());
                case "installer" -> install(command, confirm);
                case "optimiser" -> optimize(command.arguments(), confirm);
                case "cpu" -> system.cpu();
                case "ram" -> system.memory();
                case "disque" -> system.disk();
                case "batterie" -> system.battery();
                case "réseau", "reseau" -> system.network();
                case "etat" -> system.status();
                case "quitter" -> "__QUIT__";
                default -> applications.launch(command.name(), command.arguments());
            };
        } catch (Exception error) {
            LOG.log(Level.WARNING, "Command failed: " + input, error);
            return "Erreur : " + error.getMessage();
        }
    }

    public boolean startupEnabled() { return startup.isEnabled(); }
    public void setStartup(boolean enabled) throws Exception { startup.setEnabled(enabled); }

    private String open(List<String> arguments) throws Exception {
        String target = first(arguments);
        if (target.startsWith("http://") || target.startsWith("https://")) return network.openUrl(target);
        Path path = Path.of(target.replaceFirst("^~/", System.getProperty("user.home") + "/"));
        if (!path.isAbsolute()) {
            Path homeTarget = Path.of(System.getProperty("user.home"), target);
            if (Files.exists(homeTarget)) path = homeTarget;
        }
        if (Files.exists(path)) return files.open(target);
        return applications.launch(target, arguments.subList(1, arguments.size()));
    }

    private String search(List<String> arguments) throws Exception {
        String query = String.join(" ", arguments);
        if (query.isBlank()) return "Usage : rechercher <texte>";
        if (Files.exists(Path.of(query))) return files.search(query);
        return network.search(query);
    }

    private String install(ParsedCommand command, Predicate<String> confirm) throws Exception {
        String packageName = first(command);
        String manager = command.arguments().contains("--flatpak") ? "flatpak" : "apt";
        if (!confirm.test("Installer " + packageName + " avec " + manager + " (sudo requis) ?")) return "Installation annulée.";
        if (manager.equals("flatpak")) new ProcessBuilder("flatpak", "install", "-y", packageName).start();
        else new ProcessBuilder("sudo", "apt", "install", "-y", packageName).start();
        return "Installation lancée : " + manager + " " + packageName;
    }

    private String optimize(List<String> arguments, Predicate<String> confirm) throws Exception {
        List<String> targets = arguments.stream().map(AssistantEngine::optimizationTarget).toList();
        if (targets.isEmpty() || targets.contains("tout")) targets = List.of("ram", "cpu", "disque");
        StringBuilder result = new StringBuilder();
        if (targets.contains("ram")) result.append(system.optimizeRam()).append("\n");
        if (targets.contains("cpu")) result.append(system.optimizeCpu()).append("\n");
        if (targets.contains("disque")) {
            if (!confirm.test(system.diskOptimizationPreview() + "\nVider la corbeille maintenant ?")) {
                result.append("Nettoyage disque annulé.\n");
            } else {
                result.append(system.cleanUserTrash()).append("\n");
            }
        }
        return result.toString().trim();
    }

    private static String optimizationTarget(String value) {
        return switch (value.toLowerCase()) {
            case "memory", "память", "озу" -> "ram";
            case "disk", "диск" -> "disque";
            case "all", "tout", "все", "всё" -> "tout";
            default -> value.toLowerCase();
        };
    }

    private String requires(ParsedCommand command, Predicate<String> confirm, String question, ThrowingAction action) throws Exception {
        if (!confirm.test(question + " ?")) return "Opération annulée.";
        action.run();
        return "Opération terminée.";
    }

    private String help() {
        return "Commandes : ouvrir/lancer, fermer, copier, deplacer, renommer, supprimer, rechercher, installer, optimiser [ram|cpu|disque|tout], cpu, ram, disque, batterie, reseau, etat, quitter";
    }

    private static String first(ParsedCommand command) { return first(command.arguments()); }
    private static String first(List<String> values) { return values.isEmpty() ? "" : values.getFirst(); }
    private static String argument(ParsedCommand command, int index) {
        if (command.arguments().size() <= index) throw new IllegalArgumentException("Arguments insuffisants");
        return command.arguments().get(index);
    }
    private static String argument(ParsedCommand command, int index, String separator) {
        if (command.arguments().size() > index && (separator.equalsIgnoreCase(command.arguments().get(index - 1))
            || "to".equalsIgnoreCase(command.arguments().get(index - 1))
            || "в".equalsIgnoreCase(command.arguments().get(index - 1)))) {
            return command.arguments().get(index);
        }
        return argument(command, 1);
    }
    private static String destination(ParsedCommand command) {
        if (command.arguments().size() > 2 && List.of("dans", "vers", "en", "in", "to", "в")
            .contains(command.arguments().get(1).toLowerCase())) {
            return command.arguments().get(2);
        }
        return argument(command, 1);
    }
    @FunctionalInterface private interface ThrowingAction { void run() throws Exception; }
}
