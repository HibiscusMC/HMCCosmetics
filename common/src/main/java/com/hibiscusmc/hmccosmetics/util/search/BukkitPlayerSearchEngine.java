package com.hibiscusmc.hmccosmetics.util.search;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains immutable player-position snapshots. Queries never touch another
 * region's live entity or chunk state, which makes this safe on Folia.
 */
public class BukkitPlayerSearchEngine extends PlayerSearchEngine {

    private final Map<UUID, PlayerPosition> positions = new ConcurrentHashMap<>();

    public BukkitPlayerSearchEngine(@NotNull HMCCosmeticsPlugin plugin) {
        super(plugin);
    }

    @Override
    public List<Player> getPlayersInRange(Location location, double range) {
        if (location.getWorld() == null) return List.of();
        UUID worldId = location.getWorld().getUID();
        double rangeSquared = range * range;
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        return positions.values().stream()
            .filter(position -> position.worldId().equals(worldId))
            .filter(position -> position.distanceSquared(x, y, z) <= rangeSquared)
            .map(PlayerPosition::player)
            .filter(Player::isOnline)
            .toList();
    }

    protected void update(Player player, Location location) {
        if (location.getWorld() == null) return;
        positions.put(player.getUniqueId(), new PlayerPosition(
            player, location.getWorld().getUID(), location.getX(), location.getY(), location.getZ()
        ));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        update(event.getPlayer(), event.getPlayer().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.hasChangedPosition()) update(event.getPlayer(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        update(event.getPlayer(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        positions.remove(event.getPlayer().getUniqueId());
    }

    private record PlayerPosition(Player player, UUID worldId, double x, double y, double z) {
        double distanceSquared(double otherX, double otherY, double otherZ) {
            double dx = x - otherX;
            double dy = y - otherY;
            double dz = z - otherZ;
            return dx * dx + dy * dy + dz * dz;
        }
    }
}
