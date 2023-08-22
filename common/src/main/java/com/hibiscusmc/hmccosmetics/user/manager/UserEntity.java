package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserEntity {

    @Getter
    private UUID owner;
    @Getter
    private List<Player> viewers = new ArrayList<>();
    @Getter @Setter
    private Long lastUpdate = 0L;
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
        if (System.currentTimeMillis() - lastUpdate <= 1000) return List.of(); //Prevents mass refreshes
        ArrayList<Player> newPlayers = new ArrayList<>();
        ArrayList<Player> removePlayers = new ArrayList<>();
        List<Player> players = PlayerUtils.getNearbyPlayers(location);

        for (Player player : players) {
            CosmeticUser user = CosmeticUsers.getUser(player);
            if (user != null && owner != user.getUniqueId() && user.isInWardrobe()) { // Fixes issue where players in wardrobe would see other players cosmetics if they were not in wardrobe
                removePlayers.add(player);
                PacketManager.sendEntityDestroyPacket(ids, List.of(player));
                continue;
            }
            if (!viewers.contains(player)) {
                viewers.add(player);
                newPlayers.add(player);
                continue;
            }
            // bad loopdy loops
            for (Player viewerPlayer : viewers) {
                if (!players.contains(viewerPlayer)) {
                    removePlayers.add(viewerPlayer);
                    PacketManager.sendEntityDestroyPacket(ids, List.of(viewerPlayer));
                }
            }
        }
        viewers.removeAll(removePlayers);
        lastUpdate = System.currentTimeMillis();
        return newPlayers;
    }

    public void teleport(Location location) {
        this.location = location;
        for (Integer entity : ids) {
            PacketManager.sendTeleportPacket(entity, location, false, getViewers());
        }
    }

    public void setRotation(int yaw) {
        location.setYaw(yaw);
        for (Integer entity : ids) {
            PacketManager.sendLookPacket(entity, location, getViewers());
        }
    }
}
