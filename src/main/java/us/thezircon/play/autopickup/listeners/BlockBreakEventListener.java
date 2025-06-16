package us.thezircon.play.autopickup.listeners;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.EnumVerticalItem;
import us.thezircon.play.autopickup.utils.InventoryUtils;
import us.thezircon.play.autopickup.api.AutoAPI;

public class BlockBreakEventListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onBreak(BlockBreakEvent e) {

        if (e.isCancelled()) {
            PLUGIN.debugMsg("BlockBreakEvent cancelled");
            return;
        }
        PLUGIN.debugMsg("BlockBreakEvent");
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Location loc = block.getLocation();

        if (!PLUGIN.autopickup_list.contains(player)
                || PLUGIN.getConfigManager().isWorldBlacklisted(loc) || isBlacklistedBlock(block)) {
            return;
        }
        PLUGIN.debugMsg("BlockBreakEvent 1");

        handlePermissionsAsync(player);
        PLUGIN.debugMsg("BlockBreakEvent 3");

        handleXpAndMending(e, player, block);
        PLUGIN.debugMsg("BlockBreakEvent 4: " + e.getBlock().getType());

        handleVerticalCropHarvest(block, player);
        PLUGIN.debugMsg("BlockBreakEvent 9");

        AutoAPI.tagCustomDropLocation(player, loc);
        PLUGIN.debugMsg("BlockBreakEvent 10");
    }

    private boolean isBlacklistedBlock(Block block) {
        return PLUGIN.getConfigManager().isDoBlacklisted()
                && PLUGIN.getConfigManager().getBlacklistedItems().contains(block.getType().toString());
    }

    private void handlePermissionsAsync(Player player) {
        PLUGIN.debugMsg("BlockBreakEvent Async 2");

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
        PLUGIN.debugMsg("XP 1");
        if (!PLUGIN.getConfigManager().isUsingSilkSpawner() || block.getType() != Material.SPAWNER) {
            int xp = e.getExpToDrop();
            if (xp <= 0) return;

            InventoryUtils.applyMending(player, xp);
            PLUGIN.debugMsg("XP 2: " + xp);
            e.setExpToDrop(0);
        }
    }

    private void handleVerticalCropHarvest(Block block, Player player) {
        Material type = block.getType();

        if (EnumVerticalItem.isMultiBlock(type)) {
            harvestConnectedVertical(player, block.getLocation(), type);
        }
    }

    // TODO: BIG_DRIPLEAF break both ways
    private void harvestConnectedVertical(Player player, Location base, Material type) {
        int direction = EnumVerticalItem.getGrowthDirection(type);

        Location loc = base.clone();
        while (loc.getBlock().getType() == type) {
            PLUGIN.debugMsg(loc.getBlock().getType().toString());
            AutoAPI.tagCustomDropLocation(player, loc);
            loc.add(0, direction, 0);
        }
    }
}
