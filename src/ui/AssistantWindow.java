package ui;

import core.AssistantEngine;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public final class AssistantWindow extends JFrame {
    private final AssistantEngine engine;
    private final UiPreferences preferences = new UiPreferences();
    private final JTextArea output = new JTextArea();
    private final JTextField input = new JTextField();
    private final JLabel state = new JLabel("● prêt");
    private final JLabel musicTrack = new JLabel("MPRIS // --");
    private final JLabel musicBars = new JLabel("[ . . . . . . . . ]");
    private final JLabel musicVolume = new JLabel("VOL --");
    private Timer musicTimer;
    private Timer musicAnimationTimer;
    private boolean musicPlaying;
    private double musicAnimationPhase;
    private AnimatedBackground animatedBackground;
    private UiText text;
    private UiTheme theme;

    public AssistantWindow(AssistantEngine engine) {
        super("Assistant");
        setName("Assistant");
        this.engine = engine;
        refreshTheme();
        setIconImage(createApplicationIcon());
        input.addActionListener(event -> execute());
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setMinimumSize(new Dimension(760, 480));
        setSize(900, 590);
        setLocationByPlatform(true);
        buildUi();
        installShortcuts();
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent event) { input.requestFocusInWindow(); animatedBackground.start(); }
            @Override public void windowActivated(WindowEvent event) { animatedBackground.start(); }
            @Override public void windowDeactivated(WindowEvent event) { animatedBackground.stop(); }
            @Override public void windowClosed(WindowEvent event) { animatedBackground.stop(); }
        });
    }

    private void buildUi() {
        getContentPane().setBackground(theme.background());
        animatedBackground = new AnimatedBackground(theme);
        JPanel root = animatedBackground;
        root.setLayout(new BorderLayout(0, 10));
        root.setBackground(theme.background());
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        JLabel title = new JLabel(text.title());
        title.setForeground(theme.accent());
        title.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        state.setForeground(theme.accent());
        state.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        header.add(title, BorderLayout.WEST);
        header.add(headerControls(), BorderLayout.EAST);

        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        output.setOpaque(false);
        output.setBackground(theme.background());
        output.setForeground(theme.text());
        output.setCaretColor(theme.accent());
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        output.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        if (output.getDocument().getLength() == 0) output.setText(
            "+------------------------------------------------------------+\n"
                + "| " + text.online() + "                           |\n"
                + "+------------------------------------------------------------+\n");

        JPanel telemetry = new JPanel(new GridLayout(2, 2, 8, 8));
        telemetry.setOpaque(false);
        telemetry.add(infoPanel(text.system(), engine.platformName() + "\nJava 21 LTS"));
        telemetry.add(infoPanel(text.memory(), "ram  /  cpu  /  swap\nmonitoring on demand"));
        telemetry.add(infoPanel(text.network(), "local services ready\nweb browser available"));
        telemetry.add(infoPanel(text.commandBus(), "authorized actions only\nno shell passthrough"));

        JPanel command = new JPanel(new BorderLayout(8, 0));
        command.setOpaque(false);
        input.setBackground(theme.panel());
        input.setForeground(theme.text());
        input.setCaretColor(theme.accent());
        input.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.border()),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            JButton run = button(text.execute());
        run.addActionListener(event -> execute());
        command.add(input, BorderLayout.CENTER);
        command.add(run, BorderLayout.EAST);

        JPanel quick = new JPanel(new GridLayout(1, 6, 6, 0));
        quick.setOpaque(false);
        addQuickButton(quick, text.system(), "etat");
        addQuickButton(quick, text.ram(), "ram");
        addQuickButton(quick, text.cpu(), "cpu");
        addQuickButton(quick, text.disk(), "disque");
        addQuickButton(quick, text.help(), "help");
        JButton optimize = button(text.optimize());
        optimize.addActionListener(event -> chooseOptimization());
        quick.add(optimize);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JCheckBox startup = new JCheckBox(text.startup(), engine.startupEnabled());
        startup.setForeground(theme.text());
        startup.setBackground(theme.background());
        startup.addActionListener(event -> {
            try { engine.setStartup(startup.isSelected()); }
            catch (Exception error) { append("Erreur démarrage : " + error.getMessage()); }
        });
        footer.add(startup, BorderLayout.WEST);
        JLabel shortcut = new JLabel(text.shortcut());
        shortcut.setForeground(theme.text().darker());
        footer.add(shortcut, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(telemetry, BorderLayout.NORTH);
        JScrollPane history = new JScrollPane(output);
        history.setOpaque(false);
        history.getViewport().setOpaque(false);
        history.setBorder(BorderFactory.createLineBorder(theme.border()));
        center.add(history, BorderLayout.CENTER);
        JPanel lower = new JPanel(new BorderLayout(0, 8));
        lower.setOpaque(false);
        lower.add(command, BorderLayout.NORTH);
        lower.add(quick, BorderLayout.CENTER);
        lower.add(footer, BorderLayout.SOUTH);
        JPanel console = new JPanel(new BorderLayout(0, 8));
        console.setOpaque(false);
        console.add(center, BorderLayout.CENTER);
        console.add(lower, BorderLayout.SOUTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setOpaque(false);
        tabs.setForeground(theme.accent());
        tabs.setUI(new DarkTabbedPaneUI(theme));
        tabs.addTab(text.console(), console);
        tabs.addTab(text.music(), musicPanel());
        tabs.addChangeListener(event -> {
            if (tabs.getSelectedIndex() == 1) startMusicMonitor();
            else stopMusicMonitor();
        });
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
        installFavorites(console);
    }

    private void installShortcuts() {
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("control SPACE"), "toggle");
        getRootPane().getActionMap().put("toggle", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                setVisible(!isVisible());
                if (isVisible()) input.requestFocusInWindow();
            }
        });
    }

    private void execute() {
        String command = input.getText().trim();
        if (command.isBlank()) return;
        input.setText("");
        append("> " + command);
        state.setText(text.state(text.processing()));
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() { return engine.execute(command, question -> javax.swing.JOptionPane.showConfirmDialog(
                    AssistantWindow.this, question, text.confirmation(), javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION); }
            @Override protected void done() {
                try { append(get()); } catch (Exception error) { append("Erreur : " + error.getMessage()); }
                state.setText(text.state(text.ready()));
            }
        }.execute();
    }

    private void addQuickButton(JPanel panel, String label, String command) {
        JButton button = button(label);
        button.addActionListener(event -> { input.setText(command); execute(); });
        panel.add(button);
    }

    private void chooseOptimization() {
        JCheckBox ram = new JCheckBox(text.ram(), true);
        JCheckBox cpu = new JCheckBox(text.cpu(), true);
        JCheckBox disk = new JCheckBox(text.disk(), false);
        JPanel choices = new JPanel(new GridLayout(3, 1));
        choices.add(ram);
        choices.add(cpu);
        choices.add(disk);
        int result = javax.swing.JOptionPane.showConfirmDialog(this, choices, text.selectOptimization(),
                javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);
        if (result != javax.swing.JOptionPane.OK_OPTION) return;
        StringBuilder command = new StringBuilder("optimiser");
        if (ram.isSelected()) command.append(" ram");
        if (cpu.isSelected()) command.append(" cpu");
        if (disk.isSelected()) command.append(" disque");
        if (command.length() == "optimiser".length()) return;
        input.setText(command.toString());
        execute();
    }

    private JPanel musicPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel heading = new JLabel("+-- " + text.music() + " // MPRIS ------------------------------+");
        heading.setForeground(theme.accent());
        heading.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        panel.add(heading, BorderLayout.NORTH);

        JPanel display = new JPanel(new GridLayout(4, 1, 0, 8));
        display.setBackground(theme.panel());
        display.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.border()),
                BorderFactory.createEmptyBorder(20, 16, 20, 16)));
        musicTrack.setForeground(theme.text());
        musicTrack.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        musicTrack.setHorizontalAlignment(JLabel.CENTER);
        musicBars.setForeground(theme.accent());
        musicBars.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        musicBars.setHorizontalAlignment(JLabel.CENTER);
        musicVolume.setForeground(theme.text());
        musicVolume.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        musicVolume.setHorizontalAlignment(JLabel.CENTER);
        JLabel hint = new JLabel("Spotify / Spicetify via playerctl");
        hint.setForeground(theme.text().darker());
        hint.setHorizontalAlignment(JLabel.CENTER);
        display.add(musicTrack);
        display.add(musicBars);
        display.add(musicVolume);
        display.add(hint);
        panel.add(display, BorderLayout.CENTER);

        JPanel controls = new JPanel(new GridLayout(1, 5, 8, 0));
        controls.setOpaque(false);
        addMusicButton(controls, text.previous(), "music previous");
        addMusicButton(controls, text.playPause(), "music play-pause");
        addMusicButton(controls, text.next(), "music next");
        addMusicButton(controls, "VOL -", "music volume-down");
        addMusicButton(controls, "VOL +", "music volume-up");
        panel.add(controls, BorderLayout.SOUTH);
        return panel;
    }

    private void addMusicButton(JPanel panel, String label, String command) {
        JButton button = button(label);
        button.addActionListener(event -> runMusicCommand(command));
        panel.add(button);
    }

    private void runMusicCommand(String command) {
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() { return engine.execute(command, question -> false); }
            @Override protected void done() {
                try { append(get()); updateMusicDisplay(); updateMusicVolume(); }
                catch (Exception error) { append("Music error: " + error.getMessage()); }
            }
        }.execute();
    }

    private void startMusicMonitor() {
        if (musicTimer == null) musicTimer = new Timer(900, event -> updateMusicDisplay());
        if (musicAnimationTimer == null) {
            musicAnimationTimer = new Timer(33, event -> {
                if (musicPlaying) {
                    musicAnimationPhase += 0.16;
                    musicBars.setText(animatedBars());
                }
            });
        }
        updateMusicDisplay();
        musicTimer.start();
        musicAnimationTimer.start();
    }

    private void stopMusicMonitor() {
        if (musicTimer != null) musicTimer.stop();
        if (musicAnimationTimer != null) musicAnimationTimer.stop();
        musicPlaying = false;
    }

    private void updateMusicDisplay() {
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() { return engine.execute("music status", question -> false); }
            @Override protected void done() {
                try {
                    String result = get();
                    musicPlaying = result.toLowerCase().contains("playing") || result.toLowerCase().contains("lecture");
                    musicTrack.setText(result.replace("\n", "  |  "));
                    if (!musicPlaying) musicBars.setText("[ . . . . . . . . ]");
                } catch (Exception error) {
                    musicTrack.setText(text.noMusic());
                    musicBars.setText("[ - - - - - - - - ]");
                }
            }
        }.execute();
    }

    private void updateMusicVolume() {
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() { return engine.execute("music volume", question -> false); }
            @Override protected void done() {
                try { musicVolume.setText(get()); } catch (Exception ignored) { }
            }
        }.execute();
    }

    private void installFavorites(JPanel console) {
        JPanel favorites = new JPanel(new BorderLayout(8, 0));
        favorites.setOpaque(false);
        JLabel label = new JLabel("FAVORITES //");
        label.setForeground(theme.accent());
        label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        JPanel buttons = new JPanel(new GridLayout(1, Math.max(1, preferences.favorites().size() + 1), 6, 0));
        buttons.setOpaque(false);
        for (String favorite : preferences.favorites()) {
            JButton button = button(favorite.toUpperCase());
            button.addActionListener(event -> runFavorite(favorite));
            buttons.add(button);
        }
        JButton add = button("+");
        add.setToolTipText("Ajouter une application favorite");
        add.addActionListener(event -> addFavorite());
        buttons.add(add);
        favorites.add(label, BorderLayout.WEST);
        favorites.add(buttons, BorderLayout.CENTER);
        console.add(favorites, BorderLayout.NORTH);
    }

    private void runFavorite(String favorite) {
        input.setText("open " + favorite);
        execute();
    }

    private void addFavorite() {
        String value = javax.swing.JOptionPane.showInputDialog(this, "Nom de l'application ou commande", "Ajouter un favori", javax.swing.JOptionPane.PLAIN_MESSAGE);
        if (value == null || value.isBlank()) return;
        java.util.ArrayList<String> favorites = new java.util.ArrayList<>(preferences.favorites());
        favorites.add(value.trim());
        preferences.favorites(favorites);
        rebuild();
    }

    private String animatedBars() {
        String levels = " ▁▂▃▄▅▆▇█";
        StringBuilder bars = new StringBuilder("[ ");
        for (int index = 0; index < 12; index++) {
            double wave = Math.sin(musicAnimationPhase + index * 0.58) * 0.5
                    + Math.sin(musicAnimationPhase * 0.63 + index * 0.21) * 0.25 + 0.5;
            int level = Math.max(1, Math.min(levels.length() - 1, (int) Math.round(wave * (levels.length() - 1))));
            bars.append(levels.charAt(level)).append(' ');
        }
        return bars.append(']').toString();
    }

    private JButton button(String label) {
        JButton button = new JButton(label);
        button.setForeground(theme.accent());
        button.setBackground(theme.panel());
        button.setFocusPainted(false);
        button.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        button.setMargin(new Insets(7, 8, 7, 8));
        return button;
    }

    private JPanel infoPanel(String heading, String content) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(theme.panel());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(theme.border()),
                BorderFactory.createEmptyBorder(9, 11, 9, 11)));
        JLabel title = new JLabel(heading);
        title.setForeground(theme.accent());
        title.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        JLabel value = new JLabel("<html>" + content.replace("\n", "<br>") + "</html>");
        value.setForeground(theme.text().darker());
        value.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        panel.add(title, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private void refreshTheme() {
        text = new UiText(preferences.language());
        theme = UiTheme.create(preferences.palette(), preferences.customAccent());
        setTitle(text.title());
        state.setText(text.state(text.ready()));
        if (isDisplayable()) setIconImage(createApplicationIcon());
    }

    private JPanel headerControls() {
        JPanel controls = new JPanel(new BorderLayout(7, 0));
        controls.setBackground(theme.background());
        JComboBox<String> languages = new JComboBox<>(new String[] { "FR", "EN", "RU" });
        languages.setSelectedItem(preferences.language().name());
        languages.setToolTipText(text.language());
        languages.addActionListener(event -> {
            UiPreferences.Language selected = UiPreferences.Language.valueOf((String) languages.getSelectedItem());
            if (selected != preferences.language()) {
                preferences.language(selected);
                rebuild();
            }
        });
        JComboBox<String> palettes = new JComboBox<>(new String[] { "MATRIX", "CYBER", "AMBER" });
        palettes.setSelectedItem(preferences.palette().name());
        palettes.setToolTipText(text.palette());
        palettes.addActionListener(event -> {
            UiPreferences.Palette selected = UiPreferences.Palette.valueOf((String) palettes.getSelectedItem());
            if (selected != preferences.palette()) {
                preferences.palette(selected);
                rebuild();
            }
        });
        JButton color = button(text.color());
        color.setToolTipText(text.color());
        color.addActionListener(event -> {
            Color chosen = JColorChooser.showDialog(this, text.color(), preferences.customAccent());
            if (chosen != null) {
                preferences.customAccent(chosen);
                rebuild();
            }
        });
        JPanel selectors = new JPanel(new BorderLayout(5, 0));
        selectors.setBackground(theme.background());
        selectors.add(languages, BorderLayout.WEST);
        selectors.add(palettes, BorderLayout.CENTER);
        controls.add(state, BorderLayout.WEST);
        controls.add(selectors, BorderLayout.CENTER);
        controls.add(color, BorderLayout.EAST);
        return controls;
    }

    private void rebuild() {
        stopMusicMonitor();
        refreshTheme();
        getContentPane().removeAll();
        buildUi();
        revalidate();
        repaint();
    }

    private void append(String text) {
        output.append(text + "\n");
        output.setCaretPosition(output.getDocument().getLength());
    }

    private BufferedImage createApplicationIcon() {
        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(14, 16, 19));
        graphics.fillRoundRect(0, 0, 128, 128, 16, 16);
        graphics.setColor(theme.accent());
        graphics.setStroke(new java.awt.BasicStroke(4));
        for (int y = 32; y <= 80; y += 16) graphics.drawLine(20, y, y == 80 ? 78 : 108, y);
        graphics.setColor(new Color(141, 169, 162));
        graphics.drawLine(20, 96, 58, 96);
        graphics.setColor(theme.accent());
        graphics.drawOval(84, 84, 24, 24);
        graphics.drawLine(91, 96, 95, 100);
        graphics.drawLine(95, 100, 103, 91);
        graphics.dispose();
        return image;
    }
}
