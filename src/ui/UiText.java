package ui;

public final class UiText {
    private final UiPreferences.Language language;

    public UiText(UiPreferences.Language language) { this.language = language; }
    public String title() { return switch (language) { case FR -> "ASSISTANT // CONSOLE SYSTEME"; case EN -> "ASSISTANT // SYSTEM CONSOLE"; case RU -> "ASSISTANT // СИСТЕМНАЯ КОНСОЛЬ"; }; }
    public String online() { return switch (language) { case FR -> "[ assistant en ligne ]  tapez help pour les commandes"; case EN -> "[ assistant online ]  type help for commands"; case RU -> "[ ассистент онлайн ]  введите help для команд"; }; }
    public String execute() { return switch (language) { case FR -> "EXECUTER"; case EN -> "EXECUTE"; case RU -> "ЗАПУСК"; }; }
    public String ready() { return switch (language) { case FR -> "pret"; case EN -> "ready"; case RU -> "готов"; }; }
    public String processing() { return switch (language) { case FR -> "traitement"; case EN -> "processing"; case RU -> "обработка"; }; }
    public String startup() { return switch (language) { case FR -> "Lancer au demarrage"; case EN -> "Launch at startup"; case RU -> "Запуск при старте"; }; }
    public String shortcut() { return switch (language) { case FR -> "Ctrl+Space : afficher/masquer"; case EN -> "Ctrl+Space: show/hide"; case RU -> "Ctrl+Space: показать/скрыть"; }; }
    public String language() { return switch (language) { case FR -> "Langue"; case EN -> "Language"; case RU -> "Язык"; }; }
    public String palette() { return switch (language) { case FR -> "Theme"; case EN -> "Theme"; case RU -> "Тема"; }; }
    public String color() { return switch (language) { case FR -> "COULEUR"; case EN -> "COLOR"; case RU -> "ЦВЕТ"; }; }
    public String confirmation() { return switch (language) { case FR -> "Confirmation"; case EN -> "Confirmation"; case RU -> "Подтверждение"; }; }
    public String system() { return switch (language) { case FR -> "SYSTEME"; case EN -> "SYSTEM"; case RU -> "СИСТЕМА"; }; }
    public String memory() { return switch (language) { case FR -> "MEMOIRE"; case EN -> "MEMORY"; case RU -> "ПАМЯТЬ"; }; }
    public String network() { return switch (language) { case FR -> "RESEAU"; case EN -> "NETWORK"; case RU -> "СЕТЬ"; }; }
    public String commandBus() { return switch (language) { case FR -> "BUS COMMANDES"; case EN -> "COMMAND BUS"; case RU -> "ШИНА КОМАНД"; }; }
    public String ram() { return switch (language) { case FR -> "RAM"; case EN -> "RAM"; case RU -> "ОЗУ"; }; }
    public String cpu() { return switch (language) { case FR -> "CPU"; case EN -> "CPU"; case RU -> "ЦП"; }; }
    public String disk() { return switch (language) { case FR -> "DISQUE"; case EN -> "DISK"; case RU -> "ДИСК"; }; }
    public String help() { return switch (language) { case FR -> "AIDE"; case EN -> "HELP"; case RU -> "ПОМОЩЬ"; }; }
    public String optimize() { return switch (language) { case FR -> "OPTIMISER"; case EN -> "OPTIMIZE"; case RU -> "ОПТИМИЗАЦИЯ"; }; }
    public String selectOptimization() { return switch (language) { case FR -> "Choisir les optimisations"; case EN -> "Choose optimizations"; case RU -> "Выберите оптимизацию"; }; }
    public String state(String value) { return "● " + value; }
}
