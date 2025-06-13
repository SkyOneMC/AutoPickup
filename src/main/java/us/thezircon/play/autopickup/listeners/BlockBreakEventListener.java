package us.thezircon.play.autopickup.listeners;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.*;
import org.bukkit.persistence.PersistentDataType;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.InventoryUtils;
import us.thezircon.play.autopickup.utils.PickupObjective;
import us.thezircon.play.autopickup.utils.TallCrops;

import java.time.Instant;
import java.util.*;

public class BlockBreakEventListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onBreak(BlockBreakEvent e) {

        if (e.isCancelled()) {
//            Bukkit.getLogger().warning("BlockBreakEvent cancelled");
            return;
        }
//        Bukkit.getLogger().info("BlockBreakEvent");
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Location loc = block.getLocation();

        if (!PLUGIN.autopickup_list.contains(player) || isInBlacklistedWorld(loc) || isBlacklistedBlock(block)) {
            return;
        }
//        Bukkit.getLogger().info("BlockBreakEvent 1");

        handlePermissionsAsync(player);
//        Bukkit.getLogger().info("BlockBreakEvent 3");

        BlockState state = block.getState(false);
        if (shouldSkipContainer(block, state)) return;
//        Bukkit.getLogger().info("BlockBreakEvent 5");

        if (state instanceof Container) {
            handleContainerLoot(state, player, loc);
//            Bukkit.getLogger().info("BlockBreakEvent 6");
            return;
        }
//        Bukkit.getLogger().info("BlockBreakEvent 7");

        handleXpAndMending(e, player, block);
//        Bukkit.getLogger().info("BlockBreakEvent 8");

        handleVerticalCropHarvest(e, player);
//        Bukkit.getLogger().info("BlockBreakEvent 9");

        recordPickupObjective(loc, player);
//        Bukkit.getLogger().info("BlockBreakEvent 10");
    }

    private boolean isInBlacklistedWorld(Location loc) {
        return PLUGIN.getConfigManager().getBlacklistedWorlds().contains(loc.getWorld().getName());
    }

    private boolean isBlacklistedBlock(Block block) {
        return PLUGIN.getConfigManager().isDoBlacklisted()
                && PLUGIN.getConfigManager().getBlacklistedItems().contains(block.getType().toString());
    }

    private void handlePermissionsAsync(Player player) {
//        Bukkit.getLogger().info("BlockBreakEvent Async 2");

        Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, () -> {
            if (!PLUGIN.getConfigManager().isRequirePermsAUTO()) return;
            if (!player.hasPermission("autopickup.pickup.mined.autoenabled")) {
                PLUGIN.autopickup_list.remove(player);
            }
            if (!player.hasPermission("autopickup.pickup.mined.autosmelt.autoenabled")) {
                PLUGIN.auto_smelt_blocks.remove(player);
            }
        });
    }

    private void handleXpAndMending(BlockBreakEvent e, Player player, Block block) {
        if (!PLUGIN.getConfig().getBoolean("usingSilkSpawnerPlugin") || block.getType() != Material.SPAWNER) {
            int xp = e.getExpToDrop();
            InventoryUtils.applyMending(player, xp);
            e.setExpToDrop(0);
        }
    }

    private boolean shouldSkipContainer(Block block, BlockState state) {
        if (!(state instanceof Container)) return false;

        if (state instanceof ShulkerBox) return true;

        if (PLUGIN.getPluginHooks().isUsingUpgradableHoppers() && state instanceof Hopper hopper) {
            NamespacedKey key = new NamespacedKey(PLUGIN.getServer().getPluginManager().getPlugin("UpgradeableHoppers"), "o");
            return hopper.getPersistentDataContainer().has(key, PersistentDataType.STRING);
        }

        return PLUGIN.getPluginHooks().isUsingWildChests() && block.getType() == Material.CHEST;
    }

    private void handleContainerLoot(BlockState state, Player player, Location loc) {
        Container container = (Container) state;
        Inventory inventory = container.getInventory();

        for (ItemStack item : inventory.getContents()) {
            if (item != null) {
                HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(item);
                leftOver.values().forEach(overflow -> player.getWorld().dropItemNaturally(loc, overflow));
            }
        }

        inventory.clear();
    }

    private void handleVerticalCropHarvest(BlockBreakEvent e, Player player) {
        Material type = e.getBlock().getType();
        Location loc = e.getBlock().getLocation();
        TallCrops crops = PLUGIN.getCrops();

        if (crops.getVerticalReq().contains(type) || crops.getVerticalReqDown().contains(type)) {
            e.setDropItems(false);
            harvestVerticalChain(player, loc, type, crops);
        }

        if (isMultiBlockPlant(type)) {
            harvestConnectedVertical(player, loc, type);
        }
    }

    private boolean isMultiBlockPlant(Material type) {
        return switch (type) {
            case BAMBOO, KELP, KELP_PLANT, CACTUS, SUGAR_CANE,
                 WEEPING_VINES, WEEPING_VINES_PLANT, TWISTING_VINES,
                 TWISTING_VINES_PLANT, CAVE_VINES, CAVE_VINES_PLANT,
                 BIG_DRIPLEAF, BIG_DRIPLEAF_STEM -> true;
            default -> false;
        };
    }

    private void harvestConnectedVertical(Player player, Location base, Material type) {
        int direction = getGrowthDirection(type);
        List<Location> connectedBlocks = new ArrayList<>();

        Location checkLoc = base.clone();
        while (true) {
            checkLoc.add(0, direction, 0);
            if (checkLoc.getBlock().getType() == type) {
                connectedBlocks.add(checkLoc.clone());
            } else break;
        }

        connectedBlocks.forEach(loc -> {
            loc.getBlock().setType(Material.AIR);
            recordPickupObjective(loc, player);
        });
    }

    private int getGrowthDirection(Material type) {
        // Determine if it grows up or down
        return switch (type) {
            case BAMBOO, KELP, KELP_PLANT, CACTUS, SUGAR_CANE, TWISTING_VINES, TWISTING_VINES_PLANT, BIG_DRIPLEAF_STEM -> 1;
            case WEEPING_VINES, WEEPING_VINES_PLANT, CAVE_VINES, CAVE_VINES_PLANT, BIG_DRIPLEAF -> -1;
            default -> 0;
        };
    }

    private void harvestVerticalChain(Player player, Location loc, Material type, TallCrops crops) {
        int amt = 1;
        Material dropType = TallCrops.checkAltType(type);

        while (true) {
            loc.add(0, 1, 0);
            if (crops.getVerticalReq().contains(loc.getBlock().getType())) {
                amt++;
                loc.getBlock().setType(Material.AIR);
            } else break;
        }

        while (true) {
            loc.subtract(0, 2, 0);
            if (crops.getVerticalReqDown().contains(loc.getBlock().getType())) {
                amt++;
                loc.getBlock().setType(Material.AIR);
            } else break;
        }

        ItemStack drop = new ItemStack(dropType, amt);
        HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(drop);
        leftOver.values().forEach(item -> player.getWorld().dropItemNaturally(loc, item));

        recordPickupObjective(loc.add(0, 1, 0), player);
    }

    private void recordPickupObjective(Location loc, Player player) {
        String key = loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ() + ";" + loc.getWorld();
//        Bukkit.getLogger().info("Chiave: " + key);
        AutoPickup.customItemPatch.put(key, new PickupObjective(loc, player, Instant.now()));
    }
}
