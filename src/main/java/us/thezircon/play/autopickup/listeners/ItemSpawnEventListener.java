package us.thezircon.play.autopickup.listeners;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.api.AutoAPI;
import us.thezircon.play.autopickup.utils.InventoryUtils;
import us.thezircon.play.autopickup.utils.LocationKey;

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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSpawn(ItemSpawnEvent event) {
        Item itemEntity = event.getEntity();
        Location location = event.getLocation();

        if (PLUGIN.getPluginHooks().isUsingWildStacker()) {
            handleStackedSpawn(itemEntity, location);
        } else {
            handleVanillaSpawn(itemEntity, location);
        }
    }

    private void handleStackedSpawn(Item itemEntity, Location location) {
        StackedItem stackedItem = WildStackerAPI.getStackedItem(itemEntity);

        if (PLUGIN.getConfigManager().isWorldBlacklisted(location)) return;
        if (isIgnoredDrop(itemEntity)) return;
        if (isBlacklistedItem(stackedItem.getItemStack())) return;

        int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();
        LocationKey key = new LocationKey(location.getWorld().getUID());

        for (int[] offset : offsets) {
            key.setX(x + offset[0]);
            key.setY(y + offset[1]);
            key.setZ(z + offset[2]);

            Player player = AutoAPI.getAssociatedPlayer(key);
            if (player != null) {
                itemEntity.remove();
                stackedItem.giveItemStack(player.getInventory());
            }
        }
    }

    private void handleVanillaSpawn(Item itemEntity, Location location) {
        ItemStack itemStack = itemEntity.getItemStack();

        if (PLUGIN.getConfigManager().isWorldBlacklisted(location)) return;
        if (isIgnoredDrop(itemEntity)) return;
        if (isBlacklistedItem(itemStack)) return;

        int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();
        LocationKey key = new LocationKey(location.getWorld().getUID());

        for (int[] offset : offsets) {
            key.setX(x + offset[0]);
            key.setY(y + offset[1]);
            key.setZ(z + offset[2]);

            Player player = AutoAPI.getAssociatedPlayer(key);
            if (player != null) {
                itemEntity.remove();
                InventoryUtils.handleDropGive(player, location, itemStack, false);
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
}
