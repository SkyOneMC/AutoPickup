package us.thezircon.play.autopickup.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.thezircon.play.autopickup.AutoPickup;

import java.util.HashMap;

public class InventoryUtils {
    private static final long cooldown = 15000; // 15 sec

    public static void handleItemOverflow(Location loc, Player player, boolean doFullInvMSG, HashMap<Integer, ItemStack> leftOver, AutoPickup plugin) {
        for (ItemStack item : leftOver.values()) {
            player.getWorld().dropItemNaturally(loc, item);

        }
        if (doFullInvMSG) {
            long secondsLeft;
            if (AutoPickup.lastInvFullNotification.containsKey(player.getUniqueId())) {
                secondsLeft = (AutoPickup.lastInvFullNotification.get(player.getUniqueId()) / 1000) + cooldown / 1000 - (System.currentTimeMillis() / 1000);
            } else {
                secondsLeft = 0;
            }
            if (secondsLeft <= 0) {
                player.sendMessage(plugin.getMsg().getPrefix() + " " + plugin.getMsg().getFullInventory());
                AutoPickup.lastInvFullNotification.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }
    }
}
