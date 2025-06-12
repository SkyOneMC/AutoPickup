package us.thezircon.play.autopickup;

import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.thezircon.play.autopickup.commands.AutoDrops;
import us.thezircon.play.autopickup.commands.AutoPickup.Auto;
import us.thezircon.play.autopickup.commands.AutoSmelt;
import us.thezircon.play.autopickup.listeners.*;
import us.thezircon.play.autopickup.papi.AutoPickupExpansion;
import us.thezircon.play.autopickup.utils.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public final class AutoPickup extends JavaPlugin {

    public HashSet<Player> autopickup_list = new HashSet<>(); // Blocks
    public HashSet<Player> autopickup_list_mobs = new HashSet<>(); // Mobs
    public HashSet<Player> auto_smelt_blocks = new HashSet<>(); // AutoSmelt - Blocks

    @Getter
    private Messages msg;

    public boolean UP2Date = true;

    @Getter
    private TallCrops crops;

    @Getter
    private PluginHooks pluginHooks;

    public static ArrayList<String> worldsBlacklist = null;

    // Custom Items Patch
    public static HashMap<String, PickupObjective> customItemPatch = new HashMap<>();
    public static HashSet<UUID> droppedItems = new HashSet<>();

    // Cache smelting recipe list
    public static final Map<Material, FurnaceRecipe> smeltRecipeCache = new HashMap<>();

    // Notification Cooldown
    public static WeakHashMap<UUID, Long> lastInvFullNotification = new WeakHashMap<>();

    @Getter
    private static AutoPickup instance;

    @Getter
    private static BukkitAudiences audiences;

    // blacklist.yml
    private File fileBlacklist;
    private FileConfiguration confBlacklist;

    // papi.yml
    private File filePAPI;
    private FileConfiguration confPAPI;

    @Override
    public void onEnable() {
        instance = this;
        audiences = BukkitAudiences.create(this);

        // Load config files
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        createBlacklist();
        createPlayerDataDir();

        // Initialize hooks
        pluginHooks = new PluginHooks();

        if (pluginHooks.isUsingPlaceholderAPI()) {
            createPAPI();
            new AutoPickupExpansion().register();
        }

        // Initialize Messages
        msg = new Messages();

        // Register listeners
        registerListeners();

        // Register commands
        getCommand("autopickup").setExecutor(new Auto());
        getCommand("autodrops").setExecutor(new AutoDrops());
        getCommand("autosmelt").setExecutor(new AutoSmelt());

        // Initialize Crops for versions
        crops = new TallCrops();

        // Metrics bStats
        new Metrics(this, 5914);

        // Version check async
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    VersionChk.checkVersionAsync(getName(), 70157);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);

        // Load worlds blacklist
        if (getBlacklistConf().contains("BlacklistedWorlds")) {
            worldsBlacklist = (ArrayList<String>) getBlacklistConf().getList("BlacklistedWorlds");
        }

        // Cleanup tasks
        scheduleCleanupTasks();

        // Load auto smelt furnace recipes cache
        AutoSmeltUtils.loadFurnaceRecipes(smeltRecipeCache);
    }

    @Override
    public void onDisable() {
        // Close any open audiences (for PlaceholderAPI, etc.)
        if (audiences != null) {
            audiences.close();
        }

        // Cancel any ongoing tasks that could be running asynchronously
        Bukkit.getScheduler().cancelTasks(this);

        // Optionally, you can notify the server or log an event indicating the plugin is disabled.
        getLogger().info(getName() + " has been disabled.");
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();

        pm.registerEvents(new BlockDropItemEventListener(), this);
        pm.registerEvents(new PlayerJoinEventListener(), this);
        pm.registerEvents(new BlockBreakEventListener(), this);
        pm.registerEvents(new EntityDeathEventListener(), this);
        pm.registerEvents(new PlayerInteractEventListener(), this);
        pm.registerEvents(new PlayerDropItemEventListener(), this);
        pm.registerEvents(new ItemSpawnEventListener(), this);
        pm.registerEvents(new EntityDropItemEventListener(), this);

        if (pluginHooks.isUsingMythicMobs()) {
            pm.registerEvents(new MythicMobListener(), this);
        }
    }

    private void scheduleCleanupTasks() {
        // Pickup Objective Cleaner - runs every 15 seconds asynchronously
        new BukkitRunnable() {
            @Override
            public void run() {
                customItemPatch.keySet().removeIf(key ->
                        Duration.between(Instant.now(), customItemPatch.get(key).getCreatedAt()).getSeconds() < -15);
            }
        }.runTaskTimerAsynchronously(this, 300L, 300L);

        // Dropped Items Cleaner - runs every 5 minutes on main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    droppedItems.removeIf(uuid -> {
                        Entity entity = Bukkit.getEntity(uuid);
                        return entity == null || entity.isDead();
                    });
                } catch (NullPointerException ignored) {}
            }
        }.runTaskTimer(this, 6000L, 6000L);
    }

    // blacklist.yml related methods
    public FileConfiguration getBlacklistConf() {
        return this.confBlacklist;
    }

    private void createBlacklist() {
        fileBlacklist = new File(getDataFolder(), "blacklist.yml");
        if (!fileBlacklist.exists()) {
            fileBlacklist.getParentFile().mkdirs();
            saveResource("blacklist.yml", false);
        }

        confBlacklist = new YamlConfiguration();
        try {
            confBlacklist.load(fileBlacklist);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void blacklistReload() {
        confBlacklist = YamlConfiguration.loadConfiguration(fileBlacklist);
    }

    // papi.yml related methods
    public FileConfiguration getPAPIConf() {
        return this.confPAPI;
    }

    private void createPAPI() {
        filePAPI = new File(getDataFolder(), "papi.yml");
        if (!filePAPI.exists()) {
            filePAPI.getParentFile().mkdirs();
            saveResource("papi.yml", false);
        }

        confPAPI = new YamlConfiguration();
        try {
            confPAPI.load(filePAPI);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void PAPIReload() {
        confPAPI = YamlConfiguration.loadConfiguration(filePAPI);
    }

    // Player data directory
    public void createPlayerDataDir() {
        File dir = new File(getDataFolder(), "PlayerData");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
