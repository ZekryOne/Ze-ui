package ui;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class UiPreferences {
    public enum Language { FR, EN, RU }
    public enum Palette { MATRIX, CYBER, AMBER }

    private final Path file = Path.of(System.getProperty("user.home"), ".config/assistant/ui.properties");
    private Language language = Language.FR;
    private Palette palette = Palette.MATRIX;
    private Color customAccent = new Color(54, 211, 153);
    private final List<String> favorites = new ArrayList<>(List.of("firefox", "spotify", "nemo", "gnome-terminal"));

    public UiPreferences() { load(); }
    public Language language() { return language; }
    public Palette palette() { return palette; }
    public Color customAccent() { return customAccent; }
    public List<String> favorites() { return List.copyOf(favorites); }
    public void language(Language value) { language = value; save(); }
    public void palette(Palette value) { palette = value; save(); }
    public void customAccent(Color value) { customAccent = value; save(); }
    public void favorites(List<String> values) {
        favorites.clear();
        values.stream().map(String::trim).filter(value -> !value.isBlank()).distinct().limit(8).forEach(favorites::add);
        save();
    }

    private void load() {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
            language = Language.valueOf(properties.getProperty("language", language.name()));
            palette = Palette.valueOf(properties.getProperty("palette", palette.name()));
            customAccent = Color.decode(properties.getProperty("accent", "#36D399"));
            String savedFavorites = properties.getProperty("favorites", "");
            if (!savedFavorites.isBlank()) {
                favorites.clear();
                Arrays.stream(savedFavorites.split(","))
                        .map(String::trim).filter(value -> !value.isBlank()).distinct().limit(8).forEach(favorites::add);
            }
        } catch (Exception ignored) { }
    }

    private void save() {
        try {
            Files.createDirectories(file.getParent());
            Properties properties = new Properties();
            properties.setProperty("language", language.name());
            properties.setProperty("palette", palette.name());
            properties.setProperty("accent", String.format("#%06X", customAccent.getRGB() & 0xFFFFFF));
            properties.setProperty("favorites", String.join(",", favorites));
            try (OutputStream output = Files.newOutputStream(file)) { properties.store(output, "Assistant UI"); }
        } catch (IOException ignored) { }
    }
}
