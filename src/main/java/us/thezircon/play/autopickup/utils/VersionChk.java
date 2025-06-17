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
                    if (isUpToDate(spigotVersion, pluginVersion)) {
                        PLUGIN.getMsg().sendToConsole(UP_TO_DATE_MSG);
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

    public static boolean isUpToDate(String spigotVersion, String pluginVersion) {
        return compare(spigotVersion, pluginVersion) < 0;
    }

    public static int compare(String spigotVersion, String pluginVersion) {
        String[] parts1 = spigotVersion.split("-");
        String[] parts2 = pluginVersion.split("-");

        int result = compareVersionNumbers(parts1[0], parts2[0]);
        if (result != 0) return result;

        // Handle DEVBUILD
        if (parts1.length == 1 && parts2.length == 1) return 0; // both stable
        if (parts1.length == 1) return 1; // spigotVersion is stable, pluginVersion is DEVBUILD
        if (parts2.length == 1) return -1; // pluginVersion is stable, spigotVersion is DEVBUILD

        return parts1[1].compareTo(parts2[1]); // Alpha, Beta, etc..
    }

    private static int compareVersionNumbers(String spigotVersion, String pluginVersion) {
        String[] nums1 = spigotVersion.split("\\.");
        String[] nums2 = pluginVersion.split("\\.");
        int n1, n2;

        int length = Math.max(nums1.length, nums2.length);
        for (int i = 0; i < length; i++) {
            n1 = i < nums1.length ? Integer.parseInt(nums1[i]) : 0;
            n2 = i < nums2.length ? Integer.parseInt(nums2[i]) : 0;
            if (n1 != n2) return Integer.compare(n1, n2);
        }
        return 0;
    }

    public static void noConnection() {
        PLUGIN.getMsg().sendToConsole(NO_CONNECTION_MSG);
    }
}
