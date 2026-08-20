package commands;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class CommandParser {
    public ParsedCommand parse(String input) {
        List<String> tokens = Arrays.stream(input.trim().split("\\s+"))
                .filter(token -> !token.isBlank())
                .toList();
        if (tokens.isEmpty()) {
            return new ParsedCommand("", List.of());
        }
        return new ParsedCommand(normalize(tokens.getFirst()), tokens.subList(1, tokens.size()));
    }

    private String normalize(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "ouvre", "lance", "démarre", "demarre", "open", "launch", "start", "открыть", "запустить" -> "ouvrir";
            case "cherche", "search", "find", "искать", "найти" -> "rechercher";
            case "installe", "install", "установить" -> "installer";
            case "copie", "copy", "копировать" -> "copier";
            case "déplace", "deplace", "move", "переместить" -> "deplacer";
            case "renomme", "renommer", "rename", "переименовать" -> "renommer";
            case "supprime", "delete", "remove", "удалить" -> "supprimer";
            case "fermer", "close", "stop", "закрыть" -> "fermer";
            case "état", "etat", "status", "состояние" -> "etat";
            case "ram", "memory", "память", "озу" -> "ram";
            case "cpu", "процессор" -> "cpu";
            case "disque", "disk", "диск" -> "disque";
            case "batterie", "battery", "батарея" -> "batterie";
            case "réseau", "reseau", "network", "сеть" -> "reseau";
            case "aide", "help", "помощь" -> "help";
            case "quitter", "quit", "exit", "выход" -> "quitter";
            case "optimiser", "optimise", "optimize", "оптимизировать" -> "optimiser";
            default -> value.toLowerCase(Locale.ROOT);
        };
    }
}
