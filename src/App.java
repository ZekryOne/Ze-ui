import core.AssistantEngine;
import ui.AssistantWindow;

import javax.swing.SwingUtilities;

public final class App {
    public static void main(String[] args) {
        if (hasArgument(args, "--check")) {
            var engine = new AssistantEngine();
            System.out.println(engine.execute("help", question -> false));
            System.out.println(engine.execute("ram", question -> false));
            System.out.println(engine.execute("etat", question -> false));
            System.out.println(engine.execute("optimiser ram cpu", question -> false));
            System.out.println(engine.execute("supprimer /tmp/assistant-test", question -> false));
            return;
        }
        SwingUtilities.invokeLater(() -> {
            AssistantWindow window = new AssistantWindow(new AssistantEngine());
            window.setVisible(!hasArgument(args, "--background"));
        });
    }

    private static boolean hasArgument(String[] args, String value) {
        for (String argument : args) if (value.equals(argument)) return true;
        return false;
    }
}
