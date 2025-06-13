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

public class MythicMobListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler
    public void onDeath(MythicMobDeathEvent event) {
        if (!(event.getKiller() instanceof Player player)) return;
        if (!PLUGIN.autopickup_list_mobs.contains(player)) return;

        Location location = player.getLocation();
        if (PLUGIN.getConfigManager().isWorldBlacklisted(location)) return;
        if (isMobBlacklisted(event)) return;

        boolean showFullInvMsg = PLUGIN.getConfigManager().isDoFullInvMsg();

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

    private boolean isMobBlacklisted(MythicMobDeathEvent event) {
        return PLUGIN.getConfigManager().isDoBlacklistedEntities() &&
                PLUGIN.getConfigManager().getBlacklistedEntities().contains(event.getMobType().toString());
    }
}
