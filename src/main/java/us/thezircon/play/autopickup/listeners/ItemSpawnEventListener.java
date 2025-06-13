package us.thezircon.play.autopickup.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.PickupObjective;

import java.util.*;

public class ItemSpawnEventListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);
    private static final List<int[]> offsets = new ArrayList<>();

    static {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    offsets.add(new int[]{dx, dy, dz});
                }
            }
        }

        // Manhattan distance to check the adjacent blocks first
        offsets.sort(Comparator.comparingInt(o -> Math.abs(o[0]) + Math.abs(o[1]) + Math.abs(o[2])));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(ItemSpawnEvent event) {
        Item itemEntity = event.getEntity();
        Location location = event.getLocation();
        ItemStack itemStack = itemEntity.getItemStack();
//        Bukkit.getLogger().info("Item " + itemEntity.getItemStack().getType() + " spawned at " +
//                "block [" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + "] in world " + location.getWorld().getName());

        if (PLUGIN.getConfigManager().isWorldBlacklisted(location)) return;
        if (isIgnoredDrop(itemEntity)) return;
        if (isBlacklistedItem(itemStack)) return;

        String key;

        String world = location.getWorld().toString();

        for (int[] offset : offsets) {
            int x = location.getBlockX() + offset[0];
            int y = location.getBlockY() + offset[1];
            int z = location.getBlockZ() + offset[2];
            key = x + ";" + y + ";" + z + ";" + world;

//            Bukkit.getLogger().info("Checking key: " + key);

            if (AutoPickup.customItemPatch.containsKey(key)) {
//                Bukkit.getLogger().info("ItemSpawnEvent: Found match at " + key);
                handleCustomPickup(event, key);
                return;
            }
        }

    }

    private boolean isIgnoredDrop(Item item) {
        UUID uuid = item.getUniqueId();

        if (AutoPickup.droppedItems.contains(uuid)) {
            AutoPickup.droppedItems.remove(uuid);
            return true;
        }

        return item.hasMetadata("ap-ignore");
    }

    private boolean isBlacklistedItem(ItemStack itemStack) {
        if (!PLUGIN.getConfigManager().isDoBlacklisted()) return false;

        return PLUGIN.getConfigManager().getBlacklistedItems().contains(itemStack.getType().toString());
    }

    private String generateLocationKey(Location loc) {
        return loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ() + ";" + loc.getWorld();
    }

    private void handleCustomPickup(ItemSpawnEvent event, String key) {
        PickupObjective objective = AutoPickup.customItemPatch.get(key);
        Player player = objective.getPlayer();
        ItemStack item = event.getEntity().getItemStack();

        HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(item);

        if (leftOver.isEmpty()) {
            event.getEntity().remove();
        }

        // If you want to handle overflow or smelting in future, this is where you'd plug it in.
        // Example:
        // if (PLUGIN.auto_smelt_blocks.contains(player)) {
        //     item = AutoSmeltUtils.smelt(item, player);
        // }
    }
}
