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
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class AssistantWindow extends JFrame {
    private final AssistantEngine engine;
    private final UiPreferences preferences = new UiPreferences();
    private final JTextArea output = new JTextArea();
    private final JTextField input = new JTextField();
    private final JLabel state = new JLabel("● prêt");
    private UiText text;
    private UiTheme theme;

    public AssistantWindow(AssistantEngine engine) {
        super("Assistant");
        setName("Assistant");
        this.engine = engine;
        refreshTheme();
        input.addActionListener(event -> execute());
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setMinimumSize(new Dimension(760, 480));
        setSize(900, 590);
        setLocationByPlatform(true);
        buildUi();
        installShortcuts();
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent event) { input.requestFocusInWindow(); }
        });
    }

    private void buildUi() {
        getContentPane().setBackground(theme.background());
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(theme.background());
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(theme.background());
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
        telemetry.setBackground(theme.background());
        telemetry.add(infoPanel(text.system(), "Linux Mint 22.3\nJava 21 LTS"));
        telemetry.add(infoPanel(text.memory(), "ram  /  cpu  /  swap\nmonitoring on demand"));
        telemetry.add(infoPanel(text.network(), "local services ready\nweb browser available"));
        telemetry.add(infoPanel(text.commandBus(), "authorized actions only\nno shell passthrough"));

        JPanel command = new JPanel(new BorderLayout(8, 0));
        command.setBackground(theme.background());
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
        quick.setBackground(theme.background());
        addQuickButton(quick, text.system(), "etat");
        addQuickButton(quick, text.ram(), "ram");
        addQuickButton(quick, text.cpu(), "cpu");
        addQuickButton(quick, text.disk(), "disque");
        addQuickButton(quick, text.help(), "help");
        JButton optimize = button(text.optimize());
        optimize.addActionListener(event -> chooseOptimization());
        quick.add(optimize);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(theme.background());
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
        center.setBackground(theme.background());
        center.add(telemetry, BorderLayout.NORTH);
        JScrollPane history = new JScrollPane(output);
        history.setBorder(BorderFactory.createLineBorder(theme.border()));
        center.add(history, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        JPanel lower = new JPanel(new BorderLayout(0, 8));
        lower.setBackground(theme.background());
        lower.add(command, BorderLayout.NORTH);
        lower.add(quick, BorderLayout.CENTER);
        lower.add(footer, BorderLayout.SOUTH);
        root.add(lower, BorderLayout.SOUTH);
        setContentPane(root);
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
}
