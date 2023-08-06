package com.hibiscusmc.hmccosmetics.config;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.Location;

import javax.annotation.Nullable;

public class Wardrobe {

    private String id;
    private int distance = WardrobeSettings.getDisplayRadius();
    private String permission;
    private WardrobeLocation location;

    public Wardrobe(String id, WardrobeLocation location, @Nullable String permission, int distance) {
        this.id = id;
        this.location = location;
        if (permission != null) this.permission = permission;
        if (distance != -1) this.distance = distance;
    }

    public String getId() {
        return id;
    }

    public WardrobeLocation getLocation() {
        return location;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean hasPermission() {
        return permission != null;
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
        Location location = user.getEntity().getLocation();
        if (wardrobeLocation == null) return false;
        if (distance == -1) return true;
        if (!wardrobeLocation.getWorld().equals(location.getWorld())) return false;
        return wardrobeLocation.distanceSquared(location) <= distance * distance;
    }
}
