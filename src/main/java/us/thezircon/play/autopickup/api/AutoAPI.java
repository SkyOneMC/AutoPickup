package us.thezircon.play.autopickup.api;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.LocationKey;
import us.thezircon.play.autopickup.utils.PickupObjective;

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
     * Checks if a specific location in the given world is tagged as a custom drop location.
     *
     * @param uuid The UUID of the world where the location resides.
     * @param x The x-coordinate of the location to check.
     * @param y The y-coordinate of the location to check.
     * @param z The z-coordinate of the location to check.
     * @return True if the location is tagged as a custom drop location, false otherwise.
     */
    public static boolean isCustomDropLocationTagged(UUID uuid, int x, int y, int z) {
        LocationKey key = new LocationKey(uuid, x, y, z);
        return AutoPickup.customItemPatch.containsKey(key);
    }

    /**
     * Checks if a specific location has been tagged as a custom drop location.
     *
     * @param location The location to check for a custom drop tag.
     * @return True if the location is tagged as a custom drop location, false otherwise.
     */
    public static boolean isCustomDropLocationTagged(Location location) {
        return AutoPickup.customItemPatch.containsKey(new LocationKey(location));
    }

    /**
     * Retrieves the player associated with a specific location in the given world.
     *
     * @param uuid The UUID of the world where the location resides.
     * @param x The x-coordinate of the location.
     * @param y The y-coordinate of the location.
     * @param z The z-coordinate of the location.
     * @return The player associated with the given location, or null if no player is associated.
     */
    public static Player getAssociatedPlayer(UUID uuid, int x, int y, int z) {
        PickupObjective objective = AutoPickup.customItemPatch.get(new LocationKey(uuid, x, y, z));

        return objective.getPlayer();
    }

    /**
     * Retrieves the player associated with a specific location, if any.
     *
     * @param location The location to check for an associated player.
     * @return The player associated with the given location, or null if no player is associated.
     */
    public static Player getAssociatedPlayer(Location location) {
        PickupObjective objective = AutoPickup.customItemPatch.get(new LocationKey(location));

        return objective.getPlayer();
    }

}
