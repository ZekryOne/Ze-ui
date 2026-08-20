package ui;

import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.Graphics;

public final class DarkTabbedPaneUI extends BasicTabbedPaneUI {
    private final UiTheme theme;

    public DarkTabbedPaneUI(UiTheme theme) {
        this.theme = theme;
    }

    @Override protected void paintTabBackground(Graphics graphics, int tabPlacement, int tabIndex,
                                                  int x, int y, int width, int height, boolean isSelected) {
        graphics.setColor(isSelected ? theme.panel() : theme.background());
        graphics.fillRect(x, y, width, height);
    }

    @Override protected void paintTabBorder(Graphics graphics, int tabPlacement, int tabIndex,
                                            int x, int y, int width, int height, boolean isSelected) {
        graphics.setColor(theme.border());
        graphics.drawRect(x, y, width, height);
    }

    @Override protected void paintContentBorder(Graphics graphics, int tabPlacement, int selectedIndex) {
        // The animated background supplies the content area.
    }

    @Override protected void installDefaults() {
        super.installDefaults();
        tabPane.setOpaque(false);
        tabPane.setBackground(theme.background());
        tabPane.setForeground(theme.text());
    }
}
