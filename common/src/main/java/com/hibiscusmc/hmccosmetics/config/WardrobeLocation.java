package com.hibiscusmc.hmccosmetics.config;

import lombok.Setter;
import org.bukkit.Location;

public class WardrobeLocation {

    @Setter
    private Location npcLocation;
    @Setter
    private Location viewerLocation;
    @Setter
    private Location leaveLocation;

    public WardrobeLocation(Location npcLocation, Location viewerLocation, Location leaveLocation) {
        this.npcLocation = npcLocation;
        this.viewerLocation = viewerLocation;
        this.leaveLocation = leaveLocation;
    }

    public Location getNpcLocation() {
        return npcLocation.clone();
    }

    public Location getViewerLocation() {
        return viewerLocation.clone();
    }

    public Location getLeaveLocation() {
        return leaveLocation.clone();
    }

    public boolean hasAllLocations() {
        if (npcLocation == null || viewerLocation == null || leaveLocation == null) return false;
        return true;
    }
}
