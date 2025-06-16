package us.thezircon.play.autopickup.listeners;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.InventoryUtils;

public class MythicMobListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler
    public void onDeath(MythicMobDeathEvent event) {
        if (!(event.getKiller() instanceof Player player)) return;
        if (!PLUGIN.autopickup_list_mobs.contains(player)) return;

        Location location = player.getLocation();
        if (PLUGIN.getConfigManager().isWorldBlacklisted(location)) return;
        if (isMobBlacklisted(event)) return;

        InventoryUtils.handleDropsGive(player, location, event.getDrops(), true);
    }

    private boolean isMobBlacklisted(MythicMobDeathEvent event) {
        return PLUGIN.getConfigManager().isDoBlacklistedEntities() &&
                PLUGIN.getConfigManager().getBlacklistedEntities().contains(event.getMobType().toString());
    }
}
