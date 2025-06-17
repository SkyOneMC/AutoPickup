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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Location loc = block.getLocation();

        if (!PLUGIN.autopickup_list.contains(player)
                || PLUGIN.getConfigManager().isWorldBlacklisted(loc) || isBlacklistedBlock(block)) {
            return;
        }

        handlePermissionsAsync(player);

        handleXpAndMending(e, player, block);

        handleVerticalCropHarvest(block, player);

        AutoAPI.tagCustomDropLocation(player, loc);
    }

    private boolean isBlacklistedBlock(Block block) {
        return PLUGIN.getConfigManager().isDoBlacklisted()
                && PLUGIN.getConfigManager().getBlacklistedItems().contains(block.getType().toString());
    }

    private void handlePermissionsAsync(Player player) {

        Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, () -> {
            if (!PLUGIN.getConfigManager().isRequirePermsAUTO()) return;

            if (!player.hasPermission("autopickup.pickup.mined") && !player.hasPermission("autopickup.pickup.mined.autoenabled")) {
                PLUGIN.autopickup_list.remove(player);
            }

            if (!player.hasPermission("autopickup.pickup.mined.autosmelt") && !player.hasPermission("autopickup.pickup.mined.autosmelt.autoenabled")) {
                PLUGIN.auto_smelt_blocks.remove(player);
            }
        });
    }

    private void handleXpAndMending(BlockBreakEvent e, Player player, Block block) {
        if (!PLUGIN.getConfigManager().isUsingSilkSpawner() || block.getType() != Material.SPAWNER) {
            int xp = e.getExpToDrop();
            if (xp <= 0) return;

            InventoryUtils.applyMending(player, xp);
                e.setExpToDrop(0);
        }
    }

    // TODO: BIG_DRIPLEAF break both ways
    private void handleVerticalCropHarvest(Block block, Player player) {
        Material type = block.getType();

        int direction = EnumVerticalItem.getGrowthDirection(type);

        if (direction == 0) return;

        Location loc = block.getLocation().clone();
        while (loc.getBlock().getType() == type) {
            AutoAPI.tagCustomDropLocation(player, loc);
            loc.add(0, direction, 0);
        }
    }
}
