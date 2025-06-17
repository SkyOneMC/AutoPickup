package us.thezircon.play.autopickup.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Instant;

public record PickupObjective(Location location, Player player, Instant createdAt) {}
