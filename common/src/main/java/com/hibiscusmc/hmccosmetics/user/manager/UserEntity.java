package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public class UserEntity {

    @Getter
    private UUID owner;
    @Getter
    private List<Player> viewers = new ArrayList<>();
    @Getter @Setter
    private Long viewerLastUpdate = 0L;
    @Getter @Setter
    private Long lastPositionUpdate = 0L;
    @Getter @Setter
    private List<Integer> ids = new ArrayList<>();
    @Getter
    private Location location;

    public UserEntity(UUID owner) {
        this.owner = owner;
    }

    public List<Player> refreshViewers() {
        return refreshViewers(location);
    }

    public List<Player> refreshViewers(Location location) {
        //Prevents mass refreshes
        //if(System.currentTimeMillis() - viewerLastUpdate <= 3000) {
        //    return List.of();
        //}

        Entity ownerPlayer = Bukkit.getEntity(owner);
        if (ownerPlayer == null) {
            MessagesUtil.sendDebugMessages("Owner is null (refreshViewers), returning empty list");
            return List.of();
        }

        final List<Player> players = HMCCosmeticsPlugin.getInstance()
            .getPlayerSearchManager()
            .getPlayersInRange(location, Settings.getViewDistance());

        final ArrayList<UUID> newPlayerIds = new ArrayList<>();
        final ArrayList<UUID> removePlayerIds = new ArrayList<>();

        // Go through all nearby players, check if they are new to the viewers list.
        for (Player player : players) {
            CosmeticUser user = CosmeticUsers.getUser(player);
            if(
                user != null
                && owner != user.getUniqueId()
                && user.isInWardrobe()
                // Fixes issue where players in wardrobe would see other players cosmetics if they were not in wardrobe
                && !player.canSee(ownerPlayer)
            ) {
                removePlayerIds.add(player.getUniqueId());
                continue;
            }

            if (!viewers.contains(player)) {
                viewers.add(player);
                newPlayerIds.add(player.getUniqueId());
            }
        }
        // Basically, if they are not nearby, they are still in the viewers and we need to kick em to the curb
        for (Player viewerPlayer : viewers) {
            if (!players.contains(viewerPlayer)) {
                removePlayerIds.add(viewerPlayer.getUniqueId());
            }
        }

        // If there are players for removal, send the packets to them
        if (!removePlayerIds.isEmpty()) {
            final List<Player> removePlayers = removePlayerIds.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();

            HMCCPacketManager.sendEntityDestroyPacket(ids, removePlayers);
            viewers.removeAll(removePlayers);
        }

        this.setViewerLastUpdate(System.currentTimeMillis());
        return newPlayerIds.stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .toList();
    }

    public void teleport(Location location) {
        if (this.getLocation() != null && this.getLocation().getWorld() == location.getWorld()) {
            // Was thinking about using schedulers to just send the packet later... but that would be a lot of tasks and
            // would probably cause more lag. Furthermore, the server "ticks" the cosmetics every second by defualt. So it's fine like this.
            //if (System.currentTimeMillis() - getLastPositionUpdate() <= Settings.getPacketEntityTeleportCooldown()) return;
        }
        this.location = location;
        for (Integer entity : ids) {
            HMCCPacketManager.sendTeleportPacket(entity, location, false, getViewers());
        }
        setLastPositionUpdate(System.currentTimeMillis());
    }

    // 在Folia环境中使用异步传送
    public void teleportAsync(Location location) {
        this.location = location;
        for (Integer entity : ids) {
            HMCCPacketManager.sendTeleportPacket(entity, location, false, getViewers());
        }
        setLastPositionUpdate(System.currentTimeMillis());
    }

    public void setRotation(int yaw) {
        setRotation(yaw, false);
    }

    public void setRotation(int yaw, boolean additonalPacket) {
        location.setYaw(yaw);
        for (Integer entity : ids) {
            // First person backpacks need both packets to rotate properly, otherwise they look off
            // Regular backpacks just need the look packet
            if (additonalPacket) HMCCPacketManager.sendRotationPacket(entity, yaw, false, getViewers());
            HMCCPacketManager.sendRotateHeadPacket(entity, location, getViewers());
        }
    }
}