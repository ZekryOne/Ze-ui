package network;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class NetworkService {
    public String openUrl(String value) throws Exception {
        Desktop.getDesktop().browse(URI.create(value));
        return "Navigateur ouvert : " + value;
    }

    public String search(String query) throws Exception {
        String url = "https://www.google.com/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        return openUrl(url);
    }
}
