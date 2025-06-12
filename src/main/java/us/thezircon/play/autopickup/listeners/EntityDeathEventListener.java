package us.thezircon.play.autopickup.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.InventoryUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class EntityDeathEventListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();

        if (killer == null || killer.getType() != EntityType.PLAYER) return;
        if (!PLUGIN.autopickup_list_mobs.contains(killer)) return;

        if (isWorldBlacklisted(killer.getLocation())) return;
        if (isEntityBlacklisted(event)) return;

        checkPermissionsAsync(killer);

        handleDrops(event, killer);
        handleXp(event, killer);
    }

    private void checkPermissionsAsync(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, () -> {
            if (!PLUGIN.getConfig().getBoolean("requirePerms.autopickup")) return;

            boolean hasPermission = player.hasPermission("autopickup.pickup.entities")
                    || player.hasPermission("autopickup.pickup.entities.autoenabled");

            if (!hasPermission) {
                PLUGIN.autopickup_list_mobs.remove(player);
            }
        });
    }

    private void handleDrops(EntityDeathEvent event, Player player) {
        Location loc = player.getLocation();
        boolean doFullInvMSG = PLUGIN.getConfig().getBoolean("doFullInvMSG");

        Iterator<ItemStack> iterator = event.getDrops().iterator();

        while (iterator.hasNext()) {
            ItemStack drop = iterator.next();
            HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(drop);
            iterator.remove();

            if (!leftOver.isEmpty()) {
                InventoryUtils.handleItemOverflow(loc, player, doFullInvMSG, leftOver, PLUGIN);
            }
        }

        event.getDrops().clear();
    }

    private void handleXp(EntityDeathEvent event, Player player) {
        if (PLUGIN.getConfig().getBoolean("ignoreMobXPDrops")) return;

        int xp = event.getDroppedExp();
        InventoryUtils.applyMending(player, xp);
        event.setDroppedExp(0);
    }

    private boolean isWorldBlacklisted(Location loc) {
        return AutoPickup.worldsBlacklist != null
                && AutoPickup.worldsBlacklist.contains(loc.getWorld().getName());
    }

    private boolean isEntityBlacklisted(EntityDeathEvent event) {
        if (!PLUGIN.getBlacklistConf().getBoolean("doBlacklistedEntities")) return false;

        List<String> blacklist = PLUGIN.getBlacklistConf().getStringList("BlacklistedEntities");
        return blacklist.contains(event.getEntity().getType().toString());
    }
}
