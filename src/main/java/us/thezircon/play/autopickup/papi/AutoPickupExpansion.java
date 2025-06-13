package us.thezircon.play.autopickup.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.PickupPlayer;

public class AutoPickupExpansion extends PlaceholderExpansion {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final Component trueComponent;
    private final Component falseComponent;

    public AutoPickupExpansion() {
        String rawTrue = PLUGIN.getConfigManager().getPapiEnabledTrue();
        String rawFalse =  PLUGIN.getConfigManager().getPapiEnabledFalse();

        this.trueComponent = miniMessage.deserialize(rawTrue);
        this.falseComponent = miniMessage.deserialize(rawFalse);
    }

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

        PickupPlayer pickupPlayer = new PickupPlayer(player);

        switch (identifier) {
            case "autoenabled" -> {
                return miniMessage.serialize(pickupPlayer.getToggle() ? trueComponent : falseComponent);
            }
            case "dropsenabled" -> {
                return miniMessage.serialize(pickupPlayer.getMobDropsToggle() ? trueComponent : falseComponent);
            }
            case "autosmeltenabled" -> {
                return miniMessage.serialize(pickupPlayer.getAutoSmeltToggle() ? trueComponent : falseComponent);
            }
            default -> {
                return null;
            }
        }
    }
}
