package us.thezircon.play.autopickup.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.InventoryUtils;

public class EntityDeathEventListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();

        if (killer == null || killer.getType() != EntityType.PLAYER) return;
        if (!PLUGIN.autopickup_list_mobs.contains(killer)) return;

        if (PLUGIN.getConfigManager().isWorldBlacklisted(killer.getLocation())) return;
        if (isEntityBlacklisted(event)) return;

        checkPermissionsAsync(killer);

        handleDrops(event, killer);
        handleXp(event, killer);
    }

    private void checkPermissionsAsync(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, () -> {
            if (!PLUGIN.getConfigManager().isRequirePermsAUTO()) return;

            boolean hasPermission = player.hasPermission("autopickup.pickup.entities")
                    || player.hasPermission("autopickup.pickup.entities.autoenabled");

            if (!hasPermission) {
                PLUGIN.autopickup_list_mobs.remove(player);
            }
        });
    }

    private void handleDrops(EntityDeathEvent event, Player player) {
        Location loc = player.getLocation();

        InventoryUtils.handleDropsGive(player, loc, event.getDrops(), true);
    }

    private void handleXp(EntityDeathEvent event, Player player) {
        if (PLUGIN.getConfigManager().isIgnoreMobXPDrops()) return;

        int xp = event.getDroppedExp();
        if (xp <= 0) return;

        InventoryUtils.applyMending(player, xp);
        event.setDroppedExp(0);
    }

    private boolean isEntityBlacklisted(EntityDeathEvent event) {
        return PLUGIN.getConfigManager().isDoBlacklistedEntities()
                && PLUGIN.getConfigManager().getBlacklistedEntities().contains(event.getEntity().getType().toString());
    }
}
