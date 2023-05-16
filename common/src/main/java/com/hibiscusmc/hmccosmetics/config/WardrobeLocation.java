package com.hibiscusmc.hmccosmetics.config;

import org.bukkit.Location;

public class WardrobeLocation {

    private Location npcLocation;
    private Location viewerLocation;
    private Location leaveLocation;

    public WardrobeLocation(Location npcLocation, Location viewerLocation, Location leaveLocation) {
        this.npcLocation = npcLocation;
        this.viewerLocation = viewerLocation;
        this.leaveLocation = leaveLocation;
    }

    public Location getNpcLocation() {
        return npcLocation;
    }

    public Location getViewerLocation() {
        return viewerLocation;
    }

    public Location getLeaveLocation() {
        return leaveLocation;
    }

    public void setNPCLocation(Location wardrobeLocation) {
        this.npcLocation = wardrobeLocation;
    }

    public void setViewerLocation(Location viewerLocation) {
        this.viewerLocation = viewerLocation;
    }

    public void setLeaveLocation(Location leaveLocation) {
        this.leaveLocation = leaveLocation;
    }
}
