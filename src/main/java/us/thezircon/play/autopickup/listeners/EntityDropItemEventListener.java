package us.thezircon.play.autopickup.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.InventoryUtils;

import java.util.HashMap;
import java.util.Iterator;

public class EntityDropItemEventListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler
    public void onShear(PlayerShearEntityEvent e) {
        boolean doFullInvMSG = PLUGIN.getConfig().getBoolean("doFullInvMSG");

        if (AutoPickup.worldsBlacklist != null &&
                AutoPickup.worldsBlacklist.contains(e.getEntity().getWorld().getName())) {
            return;
        }

        Player player = e.getPlayer();
        if (!PLUGIN.autopickup_list.contains(player)) return;

        Iterator<ItemStack> iter = e.getDrops().iterator();
        while (iter.hasNext()) {
            ItemStack drop = iter.next();

            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(drop);

            iter.remove();

            if (!leftover.isEmpty()) {
                InventoryUtils.handleItemOverflow(player.getLocation(), player, doFullInvMSG, leftover, PLUGIN);
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, () -> {
            if (!player.hasPermission("autopickup.pickup.mined")) {
                PLUGIN.autopickup_list.remove(player);
            }
        });
    }
}
