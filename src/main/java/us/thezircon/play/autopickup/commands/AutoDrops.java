package us.thezircon.play.autopickup.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.Lang;
import us.thezircon.play.autopickup.utils.PickupPlayer;

public class AutoDrops implements CommandExecutor {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean requirePermsAUTO = PLUGIN.getConfig().getBoolean("requirePerms.autopickup");

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("autopickup.pickup.entities") || (!requirePermsAUTO)) {
                toggle(player);
            } else {
                PLUGIN.getMsg().send(player, Lang.NO_PERMS);
            }
        } else {
            PLUGIN.getMsg().sendToConsole(PLUGIN.getMsg().getNoConsoleMessage());
        }

        return false;

    }

    public static void toggle(Player player) {
        PickupPlayer PP = new PickupPlayer(player);
        if (PLUGIN.autopickup_list_mobs.contains(player)) {
            PLUGIN.autopickup_list_mobs.remove(player);
            PLUGIN.getMsg().send(player, Lang.AUTO_DROPS_DISABLE);
            PP.setEnabledEntities(false);
        } else {
            PLUGIN.autopickup_list_mobs.add(player);
            PLUGIN.getMsg().send(player, Lang.AUTO_DROPS_ENABLE);
            PP.setEnabledEntities(true);
        }
    }
}
