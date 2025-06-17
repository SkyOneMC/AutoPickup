package us.thezircon.play.autopickup.utils;

import org.bukkit.Location;

import java.util.UUID;

public class LocationKey {
    private final long most;
    private final long least;
    private final int x;
    private final short y;
    private final int z;

    public LocationKey(Location location) {
        UUID uuid = location.getWorld().getUID();
        this.most = uuid.getMostSignificantBits();
        this.least = uuid.getLeastSignificantBits();
        this.x = location.getBlockX();
        this.y = (short) location.getBlockY();
        this.z = location.getBlockZ();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LocationKey other)) return false;
        return most == other.most && least == other.least && x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int hashCode() {
        int h = Long.hashCode(most);
        h = 31 * h + Long.hashCode(least);
        h = 31 * h + x;
        h = 31 * h + y;
        h = 31 * h + z;
        return h;
    }
}