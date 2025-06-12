package us.thezircon.play.autopickup.listeners;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.InventoryUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MythicMobListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler
    public void onDeath(MythicMobDeathEvent event) {
        if (!(event.getKiller() instanceof Player player)) return;
        if (!PLUGIN.autopickup_list_mobs.contains(player)) return;

        Location location = player.getLocation();
        if (isWorldBlacklisted(location)) return;
        if (isMobBlacklisted(event)) return;

        boolean showFullInvMsg = PLUGIN.getConfig().getBoolean("doFullInvMSG");

        Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack drop = iterator.next();
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(drop);
            iterator.remove();

            if (!leftover.isEmpty()) {
                InventoryUtils.handleItemOverflow(location, player, showFullInvMsg, leftover, PLUGIN);
            }
        }
    }

    private boolean isWorldBlacklisted(Location location) {
        List<String> blacklist = AutoPickup.worldsBlacklist;
        return blacklist != null && blacklist.contains(location.getWorld().getName());
    }

    private boolean isMobBlacklisted(MythicMobDeathEvent event) {
        if (!PLUGIN.getBlacklistConf().contains("BlacklistedEntities", true)) return false;

        boolean doBlacklist = PLUGIN.getBlacklistConf().getBoolean("doBlacklistedEntities");
        List<String> blacklist = PLUGIN.getBlacklistConf().getStringList("BlacklistedEntities");

        return doBlacklist && blacklist.contains(event.getMobType().toString());
    }
}
