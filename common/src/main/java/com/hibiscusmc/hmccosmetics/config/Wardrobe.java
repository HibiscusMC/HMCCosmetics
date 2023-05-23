package com.hibiscusmc.hmccosmetics.config;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.List;

public class Wardrobe {

    private String id;
    private int distance = WardrobeSettings.getDefaultDistance();
    private String permission;
    private List<String> players;
    private WardrobeLocation location;

    public Wardrobe(String id, WardrobeLocation location, @Nullable String permission, int distance, @Nullable List<String> players) {
        this.id = id;
        this.location = location;
        if (permission != null) this.permission = permission;
        if (distance != -1) this.distance = distance;
        if (players != null) this.players = players;
    }

    public String getId() {
        return id;
    }

    public WardrobeLocation getLocation() {
        return location;
    }

    public boolean hasPermission() {
        return permission != null;
    }

    public boolean hasPlayers() {
        return players != null;
    }

    public List<String> getPlayers() {
        return players;
    }

    public int getDistance() {
        return distance;
    }

    public String getPermission() {
        return permission;
    }

    public void setLocation(WardrobeLocation location) {
        this.location = location;
    }

    public boolean canEnter(CosmeticUser user) {
        Location wardrobeLocation = location.getNpcLocation();
        Location location = user.getPlayer().getLocation();
        if (wardrobeLocation == null) return false;
        if (distance == -1) return true;
        if (!wardrobeLocation.getWorld().equals(location.getWorld())) return false;
        return wardrobeLocation.distanceSquared(location) <= distance * distance;
    }
}
