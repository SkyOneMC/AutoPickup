package us.thezircon.play.autopickup.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.Lang;
import us.thezircon.play.autopickup.utils.PickupPlayer;

public class AutoSmelt implements CommandExecutor {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean requirePermsAUTO = PLUGIN.getConfig().getBoolean("requirePerms.autopickup");

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("autopickup.pickup.mined.autosmelt") || (!requirePermsAUTO)) {
                toggle(player);
            } else {
                PLUGIN.getMsg().send(player, Lang.NO_PERMS);
            }
        }

        return false;

    }

    public static void toggle(Player player) {
        PickupPlayer PP = new PickupPlayer(player);
        boolean isAutoSmelt = PLUGIN.auto_smelt_blocks.contains(player);
        if (isAutoSmelt) {
            PLUGIN.auto_smelt_blocks.remove(player);
            PLUGIN.getMsg().send(player, Lang.AUTO_SMELT_DISABLE);
            PP.setEnabledAutoSmelt(false);
        } else {
            PLUGIN.auto_smelt_blocks.add(player);
            PLUGIN.getMsg().send(player, Lang.AUTO_SMELT_ENABLE);
            PP.setEnabledAutoSmelt(true);
        }
    }
}
