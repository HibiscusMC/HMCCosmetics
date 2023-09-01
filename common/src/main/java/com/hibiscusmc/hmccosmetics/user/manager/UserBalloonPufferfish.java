package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserBalloonPufferfish extends UserEntity {

    private int pufferFishEntityId;
    private UUID uuid;

    public UserBalloonPufferfish(UUID owner, int pufferFishEntityId, UUID uuid) {
        super(owner);
        this.pufferFishEntityId = pufferFishEntityId;
        this.uuid = uuid;
    }

    public int getPufferFishEntityId() {
        return pufferFishEntityId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void hidePufferfish() {
        PacketManager.sendEntityDestroyPacket(pufferFishEntityId, getViewers());
        getViewers().clear();
    }

    @Override
    public List<Player> refreshViewers(Location location) {
        if (System.currentTimeMillis() - getViewerLastUpdate() <= 1000) return List.of(); //Prevents mass refreshes
        ArrayList<Player> newPlayers = new ArrayList<>();
        ArrayList<Player> removePlayers = new ArrayList<>();
        List<Player> players = PlayerUtils.getNearbyPlayers(location);

        for (Player player : players) {
            CosmeticUser user = CosmeticUsers.getUser(player);
            if (user != null && getOwner() != user.getUniqueId() && user.isInWardrobe()) { // Fixes issue where players in wardrobe would see other players cosmetics if they were not in wardrobe
                removePlayers.add(player);
                PacketManager.sendEntityDestroyPacket(getPufferFishEntityId(), List.of(player));
                continue;
            }
            if (!getViewers().contains(player)) {
                getViewers().add(player);
                newPlayers.add(player);
                continue;
            }
            // bad loopdy loops
            for (Player viewerPlayer : getViewers()) {
                if (!players.contains(viewerPlayer)) {
                    removePlayers.add(viewerPlayer);
                    PacketManager.sendEntityDestroyPacket(getPufferFishEntityId(), List.of(viewerPlayer));
                }
            }
        }
        getViewers().removeAll(removePlayers);
        setViewerLastUpdate(System.currentTimeMillis());
        return newPlayers;
    }
}
