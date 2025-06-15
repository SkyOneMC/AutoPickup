package us.thezircon.play.autopickup.listeners;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.*;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.AltCropResolver;
import us.thezircon.play.autopickup.utils.EnumVerticalItem;
import us.thezircon.play.autopickup.utils.InventoryUtils;
import us.thezircon.play.autopickup.api.AutoAPI;

import java.util.*;

public class BlockBreakEventListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onBreak(BlockBreakEvent e) {

        if (e.isCancelled()) {
            Bukkit.getLogger().warning("BlockBreakEvent cancelled");
            return;
        }
        Bukkit.getLogger().info("BlockBreakEvent");
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Location loc = block.getLocation();

        if (!PLUGIN.autopickup_list.contains(player) || isInBlacklistedWorld(loc) || isBlacklistedBlock(block)) {
            return;
        }
        Bukkit.getLogger().info("BlockBreakEvent 1");

        handlePermissionsAsync(player);
        Bukkit.getLogger().info("BlockBreakEvent 3");

        handleXpAndMending(e, player, block);
        Bukkit.getLogger().info("BlockBreakEvent 8");

        handleVerticalCropHarvest(e, player);
        Bukkit.getLogger().info("BlockBreakEvent 9");

        AutoAPI.tagCustomDropLocation(player, loc);
        Bukkit.getLogger().info("BlockBreakEvent 10");
    }

    private boolean isInBlacklistedWorld(Location loc) {
        return PLUGIN.getConfigManager().getBlacklistedWorlds().contains(loc.getWorld().getName());
    }

    private boolean isBlacklistedBlock(Block block) {
        return PLUGIN.getConfigManager().isDoBlacklisted()
                && PLUGIN.getConfigManager().getBlacklistedItems().contains(block.getType().toString());
    }

    private void handlePermissionsAsync(Player player) {
        Bukkit.getLogger().info("BlockBreakEvent Async 2");

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
        Bukkit.getLogger().info("XP 1");
        if (!PLUGIN.getConfigManager().isUsingSilkSpawner() || block.getType() != Material.SPAWNER) {
            int xp = e.getExpToDrop();
            InventoryUtils.applyMending(player, xp);
            Bukkit.getLogger().info("XP 2");
            e.setExpToDrop(0);
        }
    }


    private void handleVerticalCropHarvest(BlockBreakEvent e, Player player) {
        Material type = e.getBlock().getType();
        Location loc = e.getBlock().getLocation();


        if (EnumVerticalItem.RequiresVerticalSupport(type)) {
            e.setDropItems(false);
            harvestVerticalChain(player, loc, type);
        }

        if (EnumVerticalItem.isMultiBlock(type)) {
            harvestConnectedVertical(player, loc, type);
        }
    }

    private void harvestConnectedVertical(Player player, Location base, Material type) {
        int direction = EnumVerticalItem.getGrowthDirection(type);

        Location checkLoc = base.clone();
        while (checkLoc.getBlock().getType() == type) {
            checkLoc.add(0, direction, 0);
            Location loc = checkLoc.clone();
            loc.getBlock().setType(Material.AIR);
            AutoAPI.tagCustomDropLocation(player, loc);

        }
    }


    private void harvestVerticalChain(Player player, Location loc, Material type) {
        int amt = 1;
        Material dropType = AltCropResolver.resolve(type);
        loc.add(0, 1, 0);

        while (EnumVerticalItem.RequiresVerticalSupport(loc.getBlock().getType())) {
                amt++;
                loc.getBlock().setType(Material.AIR);

        }
        loc.subtract(0, 2, 0);
        while (EnumVerticalItem.RequiresVerticalSupport(loc.getBlock().getType())) {
                amt++;
                loc.getBlock().setType(Material.AIR);

        }

        ItemStack drop = new ItemStack(dropType, amt);
        HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(drop);
        leftOver.values().forEach(item -> player.getWorld().dropItemNaturally(loc, item));

        AutoAPI.tagCustomDropLocation(player, loc);
    }

}