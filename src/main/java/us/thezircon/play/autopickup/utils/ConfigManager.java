package us.thezircon.play.autopickup.utils;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.thezircon.play.autopickup.AutoPickup;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Getter
public class ConfigManager {

    private static final AutoPickup PLUGIN = AutoPickup.getInstance();

    // Main config.yml values
    private boolean doAutoEnableMSG;
    private boolean requirePermsAUTO;
    private boolean requirePermsRELOAD;
    private boolean usingSilkSpawner;
    private boolean doFullInvMsg;
    private boolean voidOnFullInv;
    private boolean titleBar;
    private boolean ignoreMobXPDrops;
    private boolean doReenableMsg;
    private double configVersion;

    // papi.yml values
    private String papiEnabledTrue;
    private String papiEnabledFalse;

    // blacklist.yml values
    private boolean doBlacklisted;
    private boolean doBlacklistedEntities;
    private boolean doAutoSmeltBlacklist;
    private Set<String> blacklistedItems;
    private Set<String> blacklistedEntities;
    private Set<String> blacklistedWorlds;
    private Set<String> autoSmeltBlacklist;

    // File references
    private File fileBlacklist;
    private FileConfiguration blacklistConf;
    private File filePAPI;
    private FileConfiguration papiConf;

    public ConfigManager() {
        reload();
    }

    public void reload() {
        PLUGIN.saveDefaultConfig();
        reloadConfig();
        reloadConfigBlacklist();

        if (PLUGIN.getPluginHooks().isUsingPlaceholderAPI()
                && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            reloadConfigPapi();
        }
    }

    // === config.yml ===
    public void reloadConfig() {
        PLUGIN.reloadConfig();
        FileConfiguration config = PLUGIN.getConfig();

        this.doAutoEnableMSG = config.getBoolean("doAutoEnableMSG", true);
        this.requirePermsAUTO = config.getBoolean("requirePerms.autopickup", true);
        this.requirePermsRELOAD = config.getBoolean("requirePerms.auto-reload", true);
        this.usingSilkSpawner = config.getBoolean("usingSilkSpawnerPlugin", true);
        this.doFullInvMsg = config.getBoolean("doFullInvMSG", true);
        this.voidOnFullInv = config.getBoolean("voidOnFullInv", false);
        this.titleBar = config.getBoolean("titlebar.doTitleBar", false);
        this.ignoreMobXPDrops = config.getBoolean("ignoreMobXPDrops", false);
        this.doReenableMsg = config.getBoolean("doEnabledOnJoinMSG", true);
        this.configVersion = config.getDouble("ConfigVersion", 1.4);
    }

    // === papi.yml ===
    public void reloadConfigPapi() {
        filePAPI = ensureFileExists("papi.yml");
        papiConf = YamlConfiguration.loadConfiguration(filePAPI);

        this.papiEnabledTrue = papiConf.getString("papi.enabled.true", "<green>✔</green>");
        this.papiEnabledFalse = papiConf.getString("papi.enabled.false", "<red>✘</red>");
    }

    // === blacklist.yml ===
    public void reloadConfigBlacklist() {
        fileBlacklist = ensureFileExists("blacklist.yml");
        blacklistConf = YamlConfiguration.loadConfiguration(fileBlacklist);

        this.doBlacklisted = blacklistConf.getBoolean("doBlacklisted", false);
        this.doBlacklistedEntities = blacklistConf.getBoolean("doBlacklistedEntities", true);
        this.doAutoSmeltBlacklist = blacklistConf.getBoolean("doAutoSmeltBlacklist", false);

        this.blacklistedItems = new HashSet<>(blacklistConf.getStringList("Blacklisted"));
        this.blacklistedEntities = new HashSet<>(blacklistConf.getStringList("BlacklistedEntities"));
        this.blacklistedWorlds = new HashSet<>(blacklistConf.getStringList("BlacklistedWorlds"));
        this.autoSmeltBlacklist = new HashSet<>(blacklistConf.getStringList("AutoSmeltBlacklist"));
    }

    private File ensureFileExists(String name) {
        if (!PLUGIN.getDataFolder().exists()) {
            PLUGIN.getDataFolder().mkdirs();
        }

        File file = new File(PLUGIN.getDataFolder(), name);
        if (!file.exists()) {
            PLUGIN.saveResource(name, false);
        }

        return file;
    }


    // === Utility ===
    public boolean isWorldBlacklisted(Location location) {
        return blacklistedWorlds.contains(location.getWorld().getName());
    }
}
