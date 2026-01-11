package com.hibiscusmc.hmccosmetics.listener;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.SchedulerUtil;
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
    private static final List<CosmeticSlot> MOVEMENT_COSMETICS = List.of(
        CosmeticSlot.BACKPACK,
        CosmeticSlot.BALLOON
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
        
        // 检查玩家是否需要刷新时装（传送后）
        if(user.needsRefresh()) {
            // 如果玩家在衣柜中，则跳过刷新时装（防止衣柜传送导致的NPC预览时装传送到玩家附近）
            if(user.isInWardrobe()) {
                user.setNeedsRefresh(false);
                return;
            }
            
            // 玩家移动了，取消刷新状态并延迟刷新时装
            user.setNeedsRefresh(false);
            SchedulerUtil.runTaskLater(HMCCosmeticsPlugin.getInstance(), player, () -> {
                user.refreshCosmetics();
            }, 20);
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