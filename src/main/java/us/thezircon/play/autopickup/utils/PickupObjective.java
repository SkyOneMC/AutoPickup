package us.thezircon.play.autopickup.utils;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.HashSet;

@Getter
public class PickupObjective {

    private final Location location;
    private final Player player;
    private final Instant createdAt;
    private final HashSet<Item> processed;

    public PickupObjective(Location location, Player player, Instant instant, HashSet<Item> processed) {
        this.location = location;
        this.player = player;
        this.createdAt = instant;
        this.processed = processed;
    }

    public PickupObjective(Location location, Player player, Instant instant) {
        this.location = location;
        this.player = player;
        this.createdAt = instant;
        this.processed = null;
    }

}
