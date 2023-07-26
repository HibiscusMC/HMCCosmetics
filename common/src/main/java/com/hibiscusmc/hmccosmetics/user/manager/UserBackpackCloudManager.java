package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserBackpackCloudManager {

    private ArrayList<Integer> ids;
    private UUID owner;
    private List<Player> viewers = new ArrayList<>();
    private Long lastUpdate;

    public UserBackpackCloudManager(UUID owner) {
        this.ids = new ArrayList<>();
        this.owner = owner;
        this.lastUpdate = 0L;
    }

    public ArrayList<Integer> getId() {
        return ids;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<Player> refreshViewers(Location location) {
        if (System.currentTimeMillis() - lastUpdate <= 1000) return List.of(); //Prevents mass refreshes
        ArrayList<Player> newPlayers = new ArrayList<>();
        ArrayList<Player> removePlayers = new ArrayList<>();
        List<Player> players = PlayerUtils.getNearbyPlayers(location);

        for (Player player : players) {
            //if (player.getUniqueId().toString().equalsIgnoreCase(owner.toString())) continue;
            if (!viewers.contains(player)) {
                viewers.add(player);
                newPlayers.add(player);
                continue;
            }
            // bad loopdy loops
            for (Player viewerPlayer : viewers) {
                if (!players.contains(viewerPlayer)) {
                    removePlayers.add(viewerPlayer);
                    PacketManager.sendEntityDestroyPacket(ids, List.of(viewerPlayer)); // prevents random leashes
                }
            }
        }
        viewers.removeAll(removePlayers);
        lastUpdate = System.currentTimeMillis();
        return newPlayers;
    }

    public void hideEffects() {
        PacketManager.sendEntityDestroyPacket(ids, viewers);
        viewers.clear();
    }

    public List<Player> getViewers() {
        return viewers;
    }

}
