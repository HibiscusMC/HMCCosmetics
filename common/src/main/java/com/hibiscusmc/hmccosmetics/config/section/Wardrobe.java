package com.hibiscusmc.hmccosmetics.config.section;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;

public class Wardrobe {

    @Getter
    private final String id;
    @Getter @Setter
    private int distance;
    @Getter @Setter @Nullable
    private String permission;
    @Getter @Setter
    private WardrobeLocation location;
    @Getter @Setter @Nullable
    private String defaultMenu;
    @Getter @Nullable
    private File wardrobeFile;

    /**
     * This creates a Wardrobe object with all the information that a user will need when entering.
     * @param id The id of the wardrobe
     * @param location The locations of the Wardrobe, npcLocation and viewerLocation are required, leaveLocation is optional (if null, player will return to their original position)
     * @param permission The permission required to enter the wardrobe, if null, no permission is required
     * @param distance The distance from the wardrobe that the player can be to enter, if -1, the player can enter from any distance
     * @param defaultMenu The default menu that the player will open when entering the wardrobe.
     */
    public Wardrobe(@NotNull String id, @NotNull WardrobeLocation location, @Nullable String permission, int distance, @Nullable String defaultMenu, @Nullable File wardrobeFile) {
        this.id = id;
        this.location = location;
        this.distance = distance;
        this.permission = permission;
        this.defaultMenu = defaultMenu;
        this.wardrobeFile = wardrobeFile;
    }

    /**
     * This checks if the wardrobe has a permission. If it's null, no permission is required and will return false. If it's not null, it will return true.
     * @return if the wardrobe has a permission
     */
    public boolean hasPermission() {
        return permission != null;
    }

    /**
     * Calculates if a player can enter a wardrobe. Will return true if the player can enter, else false.
     * @param user The user that is trying to enter the wardrobe
     * @return if the player can enter the wardrobe
     */
    public boolean canEnter(@NotNull CosmeticUser user) {
        Location wardrobeLocation = location.getNpcLocation();
        Location location = user.getEntity().getLocation();
        if (wardrobeLocation == null) return false;
        if (distance <= 0) return true;
        if (!wardrobeLocation.getWorld().equals(location.getWorld())) return false;
        return wardrobeLocation.distanceSquared(location) <= distance * distance;
    }
}