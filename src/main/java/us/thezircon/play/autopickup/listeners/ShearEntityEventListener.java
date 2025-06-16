package us.thezircon.play.autopickup.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.InventoryUtils;

public class ShearEntityEventListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler
    public void onShear(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();

        if (!PLUGIN.autopickup_list.contains(player)) return;
        Location location = event.getEntity().getLocation();
        if (PLUGIN.getConfigManager().isWorldBlacklisted(location)) return;

        InventoryUtils.handleDropsGive(player, location, event.getDrops(), true);
        validatePermissionsAsync(player);
    }

    private void validatePermissionsAsync(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, () -> {
            if (!player.hasPermission("autopickup.pickup.mined")) {
                PLUGIN.autopickup_list.remove(player);
            }
        });
    }
}
