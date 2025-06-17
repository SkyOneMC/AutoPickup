package us.thezircon.play.autopickup.listeners;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.api.AutoAPI;
import us.thezircon.play.autopickup.utils.InventoryUtils;

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

        UUID worldId = location.getWorld().getUID();
        int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();

        for (int[] offset : offsets) {
            int ox = x + offset[0];
            int oy = y + offset[1];
            int oz = z + offset[2];

            if (AutoAPI.isCustomDropLocationTagged(worldId, ox, oy, oz)) {
                stackedItem.giveItemStack(AutoAPI.getAssociatedPlayer(worldId, ox, oy, oz).getInventory());
                return;
            }
        }
    }

    private void handleVanillaSpawn(Item itemEntity, Location location) {
        ItemStack itemStack = itemEntity.getItemStack();

        if (PLUGIN.getConfigManager().isWorldBlacklisted(location)) return;
        if (isIgnoredDrop(itemEntity)) return;
        if (isBlacklistedItem(itemStack)) return;

        int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();

        for (int[] offset : offsets) {
            Location key = new Location(location.getWorld(), x + offset[0], y + offset[1], z + offset[2]);

            if (AutoAPI.isCustomDropLocationTagged(key)) {
                InventoryUtils.handleDropGive(AutoAPI.getAssociatedPlayer(key), location, itemStack, false);
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
}
