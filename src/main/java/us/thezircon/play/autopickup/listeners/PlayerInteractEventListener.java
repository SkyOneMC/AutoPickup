package us.thezircon.play.autopickup.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.api.AutoAPI;

public class PlayerInteractEventListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        // Only handle block interactions
        if (action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        if (!PLUGIN.autopickup_list.contains(player) || event.getClickedBlock() == null) {
            return;
        }

        Location blockLocation = event.getClickedBlock().getLocation();

        // Respect world blacklist
        if (PLUGIN.getConfigManager().isWorldBlacklisted(blockLocation)) {
            return;
        }

        AutoAPI.tagCustomDropLocation(player, blockLocation);
    }
}
