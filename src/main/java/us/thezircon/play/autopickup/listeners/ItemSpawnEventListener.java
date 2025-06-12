package us.thezircon.play.autopickup.listeners;

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

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ItemSpawnEventListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(ItemSpawnEvent event) {
        Item itemEntity = event.getEntity();
        Location location = event.getLocation();
        ItemStack itemStack = itemEntity.getItemStack();

        if (isWorldBlacklisted(location)) return;
        if (isIgnoredDrop(itemEntity)) return;
        if (isBlacklistedItem(itemStack)) return;

        String key = generateLocationKey(location);
        if (AutoPickup.customItemPatch.containsKey(key)) {
            handleCustomPickup(event, key);
        }
    }

    private boolean isWorldBlacklisted(Location location) {
        List<String> blacklist = AutoPickup.worldsBlacklist;
        return blacklist != null && blacklist.contains(location.getWorld().getName());
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
        if (!PLUGIN.getBlacklistConf().getBoolean("doBlacklisted")) return false;

        List<String> blacklist = PLUGIN.getBlacklistConf().getStringList("Blacklisted");
        return blacklist.contains(itemStack.getType().toString());
    }

    private String generateLocationKey(Location loc) {
        return loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ() + ";" + loc.getWorld().getName();
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
