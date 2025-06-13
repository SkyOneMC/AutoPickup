package us.thezircon.play.autopickup;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
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
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public final class AutoPickup extends JavaPlugin {

    public HashSet<Player> autopickup_list = new HashSet<>(); // Blocks
    public HashSet<Player> autopickup_list_mobs = new HashSet<>(); // Mobs
    public HashSet<Player> auto_smelt_blocks = new HashSet<>(); // AutoSmelt - Blocks

    @Getter
    private Messages msg;
    @Getter
    private ConfigManager configManager;

    public boolean UP2Date = true;

    @Getter
    private TallCrops crops;

    @Getter
    private PluginHooks pluginHooks;

    // Custom Items Patch
    public static HashMap<String, PickupObjective> customItemPatch;
    public static HashSet<UUID> droppedItems = new HashSet<>();

    // Cache smelting recipe list
    public static final Map<Material, FurnaceRecipe> smeltRecipeCache = new HashMap<>();

    // Notification Cooldown
    public static WeakHashMap<UUID, Long> lastInvFullNotification = new WeakHashMap<>();

    @Getter
    private static AutoPickup instance;

    @Override
    public void onEnable() {
        instance = this;
        customItemPatch = new HashMap<>();

        // Initialize hooks
        pluginHooks = new PluginHooks();

        // Initialize config options
        configManager = new ConfigManager();

        // Initialize Messages
        msg = new Messages();

        // Initialize player data
        createPlayerDataDir();

        if (pluginHooks.isUsingPlaceholderAPI()) {
            new AutoPickupExpansion().register();
        }

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

        // Cleanup tasks
        scheduleCleanupTasks();

        // Load auto smelt furnace recipes cache
        AutoSmeltUtils.loadFurnaceRecipes(smeltRecipeCache);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

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
        pm.registerEvents(new ShearEntityEventListener(), this);

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

    // Player data directory
    public void createPlayerDataDir() {
        File dir = new File(getDataFolder(), "PlayerData");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
