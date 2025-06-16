package us.thezircon.play.autopickup.utils;

import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
public class PluginHooks {

    private final boolean usingMythicMobs;
    private final boolean usingPlaceholderAPI;
    private final boolean usingWildStacker;

    public PluginHooks() {
        this.usingMythicMobs = isPluginEnabled("MythicMobs");
        this.usingPlaceholderAPI = isPluginEnabled("PlaceholderAPI");
        this.usingWildStacker = isPluginEnabled("WildStacker");
    }

    private boolean isPluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }
}
