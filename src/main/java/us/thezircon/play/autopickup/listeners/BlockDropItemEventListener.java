package us.thezircon.play.autopickup.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.InventoryUtils;

import java.util.List;
import java.util.stream.Collectors;

public class BlockDropItemEventListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (!PLUGIN.autopickup_list.contains(player)) return;

        Block block = event.getBlock();
        Location location = block.getLocation();
        if (PLUGIN.getConfigManager().isWorldBlacklisted(location)) return;

        List<ItemStack> drops = event.getItems().stream()
                .map(Item::getItemStack)
                .collect(Collectors.toList());

        event.getItems().clear();

        boolean isSmelt = !(event.getBlockState() instanceof Container); // Shouldn't smelt items inside of containers
        InventoryUtils.handleDropsGive(player, location, drops, isSmelt);
    }
}
