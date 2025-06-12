package us.thezircon.play.autopickup.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.Lang;
import us.thezircon.play.autopickup.utils.PickupPlayer;
import us.thezircon.play.autopickup.utils.VersionChk;

public class PlayerJoinEventListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        notifyUpdateIfOutdated(player);
        enableAutoPickupModes(player);
        reEnablePreviousState(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PLUGIN.autopickup_list.remove(player);
        PLUGIN.auto_smelt_blocks.remove(player);
        PLUGIN.autopickup_list_mobs.remove(player);
        AutoPickup.lastInvFullNotification.remove(player.getUniqueId());
    }

    private void notifyUpdateIfOutdated(Player player) {
        if (!player.hasPermission("autopickup.admin.notifyupdate") || PLUGIN.UP2Date) return;

        PLUGIN.getMsg().send(player, PLUGIN.getMsg().getUpdateNoticeMessage());
        PLUGIN.getMsg().send(player, PLUGIN.getMsg().getUpdateClickHereMessage());

        for (String change : VersionChk.changelog) {
            if (!change.isEmpty()) {
                Component updateLine = Component.text("  - " + change, NamedTextColor.YELLOW);
                PLUGIN.getMsg().send(player, updateLine); // false = don't double-prefix
            }
        }
    }

    private void enableAutoPickupModes(Player player) {
        boolean doAutoEnableMSG = PLUGIN.getConfig().getBoolean("doAutoEnableMSG");

        if (player.hasPermission("autopickup.pickup.mined.autoenabled") &&
                PLUGIN.autopickup_list.add(player) && doAutoEnableMSG) {
            PLUGIN.getMsg().send(player, Lang.AUTO_ENABLED);
        }

        if (player.hasPermission("autopickup.pickup.entities.autoenabled")) {
            PLUGIN.autopickup_list_mobs.add(player);
        }

        if (player.hasPermission("autopickup.pickup.mined.autosmelt.autoenabled")) {
            PLUGIN.auto_smelt_blocks.add(player);
        }
    }

    private void reEnablePreviousState(Player player) {
        PickupPlayer pickupPlayer = new PickupPlayer(player);
        boolean doReenableMsg = PLUGIN.getConfig().getBoolean("doEnabledOnJoinMSG");

        if (pickupPlayer.getToggle() && player.hasPermission("autopickup.pickup.mined") &&
                PLUGIN.autopickup_list.add(player) && doReenableMsg) {
            PLUGIN.getMsg().send(player, Lang.AUTO_REENABLED);
        }

        if (pickupPlayer.getAutoSmeltToggle() && player.hasPermission("autopickup.pickup.mined.autosmelt")) {
            PLUGIN.auto_smelt_blocks.add(player);
        }

        if (pickupPlayer.getMobDropsToggle() && player.hasPermission("autopickup.pickup.entities")) {
            PLUGIN.autopickup_list_mobs.add(player);
        }
    }
}
