package ui;

import java.awt.Color;

public record UiTheme(Color background, Color panel, Color text, Color accent, Color border) {
    public static UiTheme create(UiPreferences.Palette palette, Color customAccent) {
        Color accent = customAccent;
        return switch (palette) {
            case MATRIX -> new UiTheme(new Color(10, 13, 15), new Color(17, 23, 24, 218), new Color(215, 230, 224), accent, new Color(43, 67, 62));
            case CYBER -> new UiTheme(new Color(8, 12, 19), new Color(15, 24, 36, 218), new Color(215, 226, 240), accent, new Color(42, 69, 96));
            case AMBER -> new UiTheme(new Color(18, 14, 9), new Color(29, 22, 14, 218), new Color(238, 226, 204), accent, new Color(91, 65, 34));
        };
    }
}
