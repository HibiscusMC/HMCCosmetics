package com.hibiscusmc.hmccosmetics.listener;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class PlayerMovementListener implements Listener {
    // Balloons are absent on purpose - BalloonSmoothingTask drives them every tick, so dispatching move
    // events at them only logged a "does not implement CosmeticMovementBehavior" debug line.
    private static final List<CosmeticSlot> MOVEMENT_COSMETICS = List.of(
        CosmeticSlot.BACKPACK
    );

    // Player Id -> Small Location
    private final Map<UUID, SmallLocation> locations = new HashMap<>();

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent ev) {
        final Player player = ev.getPlayer();

        final CosmeticUser user = CosmeticUsers.getUser(player);
        if(user == null) {
            return;
        }

        if(!updateDirtyLocation(ev.getPlayer(), ev.getTo())) {
            return;
        }

        for(final CosmeticSlot slot : MOVEMENT_COSMETICS) {
            user.updateMovementCosmetic(slot, ev.getFrom(), ev.getTo());
        }
    }

    private boolean updateDirtyLocation(final Player player, final Location nextLoc) {
        final SmallLocation previous = locations.computeIfAbsent(
            player.getUniqueId(),
            $ -> SmallLocation.fromLocation(nextLoc)
        );
        final SmallLocation next = SmallLocation.fromLocation(nextLoc);

        if(next.distanceTo(previous) > 0.25) {
            this.locations.put(player.getUniqueId(), next);
            return true;
        }

        if(next.yawDistanceTo(previous) > 5) {
            this.locations.put(player.getUniqueId(), next);
            return true;
        }

        return false;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldChange(final PlayerChangedWorldEvent ev) {
        this.locations.remove(ev.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent ev) {
        locations.remove(ev.getPlayer().getUniqueId());
    }

    record SmallLocation(
        double x,
        double y,
        double z,
        float yaw
    ) {
        public double distanceTo(SmallLocation other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            double dz = this.z - other.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        public float yawDistanceTo(SmallLocation other) {
            float diff = Math.abs(this.yaw - other.yaw) % 360;
            return diff > 180 ? 360 - diff : diff;
        }

        public static SmallLocation fromLocation(final Location location) {
            return new SmallLocation(location.getX(), location.getY(), location.getZ(), location.getYaw());
        }
    }
}
