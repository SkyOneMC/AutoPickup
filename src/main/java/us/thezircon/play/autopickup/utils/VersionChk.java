package us.thezircon.play.autopickup.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import us.thezircon.play.autopickup.AutoPickup;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import static com.google.common.net.HttpHeaders.USER_AGENT;

public class VersionChk {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);
    private static final Logger log = Logger.getLogger("Minecraft");

    public static String[] changelog;

    // Hardcoded Components for messages
    private static final Component CHECKING_MSG = Component.text(" Checking for new version...", NamedTextColor.GRAY);
    private static final Component UP_TO_DATE_MSG = Component.text("Plugin is up-to-date.", NamedTextColor.DARK_GREEN);
    private static final Component UPDATE_FOUND_MSG = Component.text(" UPDATE FOUND:", NamedTextColor.RED);
    private static final Component VERSION_LABEL = Component.text(" Version: ", NamedTextColor.GOLD);
    private static final Component USING_VERSION_LABEL = Component.text(" Using Version: ", NamedTextColor.AQUA);
    private static final Component NO_CONNECTION_MSG = Component.text(" Cannot check for update's - No internet connection!", NamedTextColor.LIGHT_PURPLE);
    private static final Component CHANGELOG_BULLET = Component.text("  - ", NamedTextColor.YELLOW);

    public static void checkVersionAsync(String pluginName, int resourceId) {
        Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, () -> {
            try {
                PLUGIN.getMsg().sendToConsole(CHECKING_MSG);

                String spigotVersion = fetchContent("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
                String changelogContent = fetchContent(
                        "https://raw.githubusercontent.com/MrButtersDEV/AutoPickup/master/update-info.txt");

                changelog = changelogContent.split("- ");

                String pluginVersion = Bukkit.getServer().getPluginManager().getPlugin(pluginName).getDescription().getVersion();

                Bukkit.getScheduler().runTask(PLUGIN, () -> {
                    if (spigotVersion.equals(pluginVersion)) {
                        PLUGIN.getMsg().sendToConsole(UP_TO_DATE_MSG
                        );
                    } else {
                        PLUGIN.getMsg().sendToConsole(UPDATE_FOUND_MSG
                                        .append(Component.space())
                                        .append(Component.text("https://www.spigotmc.org/resources/" + resourceId + "/", NamedTextColor.GREEN))
                        );
                        PLUGIN.getMsg().sendToConsole(VERSION_LABEL
                                        .append(Component.text(spigotVersion, NamedTextColor.GREEN))
                                        .append(USING_VERSION_LABEL)
                                        .append(Component.text(pluginVersion, NamedTextColor.DARK_AQUA))
                        );

                        PLUGIN.UP2Date = false;
                        for (String s : changelog) {
                            if (!s.isEmpty()) {
                                PLUGIN.getMsg().sendToConsole(CHANGELOG_BULLET.append(Component.text(s)));
                            }
                        }
                    }
                });

            } catch (Exception e) {
                Bukkit.getScheduler().runTask(PLUGIN, VersionChk::noConnection);
                log.warning("Failed to check plugin version: " + e.getMessage());
            }
        });
    }

    private static String fetchContent(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Failed to fetch URL: " + urlStr + " Response code: " + responseCode);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return content.toString();
        }
    }

    public static void noConnection() {
        PLUGIN.getMsg().sendToConsole(NO_CONNECTION_MSG);
    }
}
