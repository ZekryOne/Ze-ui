package startup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URISyntaxException;

public final class StartupService {
    private final Path desktopFile = Path.of(System.getProperty("user.home"), ".config/autostart/assistant.desktop");

    public boolean isEnabled() {
        return Files.exists(desktopFile);
    }

    public void setEnabled(boolean enabled) throws IOException {
        if (!enabled) {
            Files.deleteIfExists(desktopFile);
            return;
        }
        Files.createDirectories(desktopFile.getParent());
        String launcher = launcherPath();
        String content = "[Desktop Entry]\nType=Application\nName=Assistant\nExec=\"" + launcher
                + "\" --background\nX-GNOME-Autostart-enabled=true\n";
        Files.writeString(desktopFile, content);
    }

    private String launcherPath() throws IOException {
        try {
            Path classes = Path.of(StartupService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return classes.getParent().resolve("assistant-launcher.sh").toString();
        } catch (URISyntaxException | NullPointerException error) {
            throw new IOException("Impossible de localiser assistant-launcher.sh", error);
        }
    }
}
