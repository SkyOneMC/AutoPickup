package us.thezircon.play.autopickup.api;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.LocationKey;
import us.thezircon.play.autopickup.utils.PickupObjective;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;

public class AutoAPI {

    /**
     * @deprecated Experimental API; This api has not been tested & officially supported / may be removed.
     * @param itemEntity The Item Entity that should be ignored by AutoPickup
     */
    public static void addIgnoredDrop(Item itemEntity) {
        UUID uuid = itemEntity.getUniqueId();
        addIgnoredDrop(uuid);
    }

    /**
     * @deprecated Experimental API; This api has not been tested & officially supported / may be removed.
     * @param itemEntityUUID UUID of an item entity that should be ignored by AutoPickup
     */
    public static void addIgnoredDrop(UUID itemEntityUUID) {
        AutoPickup.droppedItems.add(itemEntityUUID);
    }

    /**
     * @param player The player who broke said block.
     * @param location The location that should be watched for possible custom drops.
     *
     * Blocks broken by players are already tagged for custom drops; this should be used for adjacent blocks that should be watched for additional custom drops.
     */
    public static void tagCustomDropLocation(Player player, Location location) {
        AutoPickup.customItemPatch.put(new LocationKey(location), new PickupObjective(location, player, Instant.now()));
    }

    /**
     * Retrieves the player associated with a specific location key,
     * if the location has been tagged for custom drops.
     *
     * @param key The LocationKey representing the specific location to retrieve the associated player.
     * @return The Player associated with the given location, or null if none is associated.
     */
    public static @Nullable Player getAssociatedPlayer(LocationKey key) {
        PickupObjective objective = AutoPickup.customItemPatch.get(key);
        if (objective == null) return null;

        return objective.getPlayer();
    }

}
