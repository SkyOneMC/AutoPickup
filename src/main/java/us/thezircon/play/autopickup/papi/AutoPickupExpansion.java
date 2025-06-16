package us.thezircon.play.autopickup.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.PickupPlayer;

public class AutoPickupExpansion extends PlaceholderExpansion {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "BUTTERFIELD8";
    }

    @Override
    public String getIdentifier() {
        return "autopickup";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String identifier) {
        if (!(offlinePlayer instanceof Player player)) {
            return null;
        }

        String trueString = PLUGIN.getConfigManager().getPapiEnabledTrue();
        String falseString =  PLUGIN.getConfigManager().getPapiEnabledFalse();

        PickupPlayer pickupPlayer = new PickupPlayer(player);

        switch (identifier) {
            case "autoenabled" -> {
                return pickupPlayer.getToggle() ? trueString : falseString;
            }
            case "dropsenabled" -> {
                return pickupPlayer.getMobDropsToggle() ? trueString : falseString;
            }
            case "autosmeltenabled" -> {
                return pickupPlayer.getAutoSmeltToggle() ? trueString : falseString;
            }
            default -> {
                return null;
            }
        }
    }
}
