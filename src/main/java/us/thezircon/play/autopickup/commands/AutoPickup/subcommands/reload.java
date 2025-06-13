package us.thezircon.play.autopickup.commands.AutoPickup.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.commands.CMDManager;
import us.thezircon.play.autopickup.utils.Lang;

import java.util.List;

public class reload extends CMDManager {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads the plugin";
    }

    @Override
    public String getSyntax() {
        return "/auto reload";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender.hasPermission("autopickup.admin")
                || !PLUGIN.getConfigManager().isRequirePermsRELOAD()) {
            PLUGIN.reloadConfig();
            PLUGIN.getConfigManager().reload();

            PLUGIN.getMsg().reloadMessages();
            PLUGIN.getMsg().send(sender, Lang.RELOAD);
        } else {
            PLUGIN.getMsg().send(sender, Lang.NO_PERMS);
        }
    }

    @Override
    public List<String> arg1(Player player, String[] args) {
        return null;
    }

    @Override
    public List<String> arg2(Player player, String[] args) {
        return null;
    }

    @Override
    public List<String> arg3(Player player, String[] args) {
        return null;
    }
}
