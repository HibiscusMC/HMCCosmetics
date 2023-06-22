package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserBalloonPufferfish {

    private int id;
    private UUID uuid;
    private List<Player> viewers = new ArrayList<>();

    public UserBalloonPufferfish(int id, UUID uuid) {
        this.id = id;
        this.uuid = uuid;
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<Player> refreshViewers(Location location) {
        ArrayList<Player> newPlayers = new ArrayList<>();
        ArrayList<Player> removePlayers = new ArrayList<>();
        List<Player> players = PlayerUtils.getNearbyPlayers(location);


        for (Player player : players) {
            if (!viewers.contains(player)) {
                viewers.add(player);
                newPlayers.add(player);
                continue;
            }
            // bad loopdy loops
            for (Player viewerPlayer : viewers) {
                if (!players.contains(viewerPlayer)) {
                    removePlayers.add(viewerPlayer);
                    PacketManager.sendEntityDestroyPacket(id, List.of(viewerPlayer)); // prevents random leashes
                }
            }
        }
        viewers.removeAll(removePlayers);
        return newPlayers;
    }
}
