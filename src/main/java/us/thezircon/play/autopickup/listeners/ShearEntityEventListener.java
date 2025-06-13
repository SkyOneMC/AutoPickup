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

public class ShearEntityEventListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler
    public void onShear(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();

        if (!PLUGIN.autopickup_list.contains(player)) return;
        if (PLUGIN.getConfigManager().isWorldBlacklisted(event.getEntity().getLocation())) return;

        handleDrops(event, player);
        validatePermissionsAsync(player);
    }

    private void handleDrops(PlayerShearEntityEvent event, Player player) {
        boolean notifyFullInventory = PLUGIN.getConfig().getBoolean("doFullInvMSG");

        Iterator<ItemStack> iterator = event.getDrops().iterator();

        while (iterator.hasNext()) {
            ItemStack drop = iterator.next();
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(drop);
            iterator.remove();

            if (!leftover.isEmpty()) {
                InventoryUtils.handleItemOverflow(player.getLocation(), player, notifyFullInventory, leftover, PLUGIN);
            }
        }
    }

    private void validatePermissionsAsync(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, () -> {
            if (!player.hasPermission("autopickup.pickup.mined")) {
                PLUGIN.autopickup_list.remove(player);
            }
        });
    }
}
