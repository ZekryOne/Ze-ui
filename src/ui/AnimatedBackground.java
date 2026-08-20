package ui;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class AnimatedBackground extends JPanel {
    private final UiTheme theme;
    private final Timer timer;
    private double phase;

    public AnimatedBackground(UiTheme theme) {
        this.theme = theme;
        setOpaque(true);
        timer = new Timer(33, event -> {
            phase += 0.045;
            repaint();
        });
    }

    public void start() { timer.start(); }
    public void stop() { timer.stop(); }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        int width = getWidth();
        int height = getHeight();
        g.setColor(new Color(theme.accent().getRed(), theme.accent().getGreen(), theme.accent().getBlue(), 22));
        for (int x = -40; x < width; x += 42) g.drawLine(x, 0, x, height);
        for (int y = 0; y < height; y += 28) g.drawLine(0, y, width, y);
        g.setColor(new Color(theme.accent().getRed(), theme.accent().getGreen(), theme.accent().getBlue(), 45));
        int scanY = (int) ((Math.sin(phase) * 0.5 + 0.5) * height);
        g.drawLine(0, scanY, width, scanY);
        for (int x = 0; x < width; x += 18) {
            int waveY = (int) (height * 0.72 + Math.sin(phase + x * 0.035) * 7);
            g.fillRect(x, waveY, 3, 2);
        }
        g.dispose();
    }
}
