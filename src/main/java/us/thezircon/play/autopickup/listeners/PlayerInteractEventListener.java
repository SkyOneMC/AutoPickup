package us.thezircon.play.autopickup.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import us.thezircon.play.autopickup.AutoPickup;

import java.util.HashMap;

public class PlayerInteractEventListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        // Only handle block interactions
        if (action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR) {
            return;
        }

        Player player = event.getPlayer();

        if (!PLUGIN.autopickup_list.contains(player) || event.getClickedBlock() == null) {
            return;
        }

        Location blockLocation = event.getClickedBlock().getLocation();

        // Respect world blacklist
        if (PLUGIN.getConfigManager().getBlacklistedWorlds().contains(blockLocation.getWorld().getName())) {
            return;
        }

        // Handle Sweet Berry Bush interaction
        if (!(action == Action.RIGHT_CLICK_BLOCK &&
                event.getClickedBlock().getType() == Material.SWEET_BERRY_BUSH))
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : blockLocation.getWorld().getNearbyEntities(blockLocation, 1, 1, 1)) {
                    if (entity.getType() != EntityType.ITEM) continue;

                    Item itemEntity = (Item) entity;
                    ItemStack stack = itemEntity.getItemStack();

                    if (stack.getType() == Material.SWEET_BERRIES) {
                        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
                        itemEntity.remove();

                        // Drop remaining items naturally
                        for (ItemStack remainder : leftover.values()) {
                            player.getWorld().dropItemNaturally(blockLocation, remainder);
                        }
                    }
                }
            }
        }.runTaskLater(PLUGIN, 1); // Schedule 1 tick later to allow natural drop to occur
        }
}
