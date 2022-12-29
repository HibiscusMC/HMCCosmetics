package com.hibiscusmc.hmccosmetics.config;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.serializer.LocationSerializer;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.misc.Utils;
import org.bukkit.Location;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class WardrobeSettings {

    private static final String WARDROBE_PATH = "wardrobe";
    private static final String DISABLE_ON_DAMAGE_PATH = "disable-on-damage";
    private static final String DISPLAY_RADIUS_PATH = "display-radius";
    private static final String PORTABLE_PATH = "portable";
    private static final String ALWAYS_DISPLAY_PATH = "always-display";
    private static final String STATIC_RADIUS_PATH = "static-radius";
    private static final String ROTATION_SPEED_PATH = "rotation-speed";
    private static final String SPAWN_DELAY_PATH = "spawn-delay";
    private static final String DESPAWN_DELAY_PATH = "despawn-delay";
    private static final String APPLY_COSMETICS_ON_CLOSE = "apply-cosmetics-on-close";
    private static final String OPEN_SOUND = "open-sound";
    private static final String CLOSE_SOUND = "close-sound";
    private static final String STATIC_LOCATION_PATH = "wardrobe-location";
    private static final String VIEWER_LOCATION_PATH = "viewer-location";
    private static final String LEAVE_LOCATION_PATH = "leave-location";
    private static final String EQUIP_PUMPKIN_WARDROBE = "equip-pumpkin";
    private static final String RETURN_LAST_LOCATION = "return-last-location";

    private static ConfigurationNode configRoot;
    private static boolean disableOnDamage;
    private static int displayRadius;
    private static boolean portable;
    private static boolean alwaysDisplay;
    private static int staticRadius;
    private static int rotationSpeed;
    private static int spawnDelay;
    private static int despawnDelay;
    private static boolean applyCosmeticsOnClose;
    private static boolean equipPumpkin;
    private static boolean returnLastLocation;
    private static Location wardrobeLocation;
    private static Location viewerLocation;
    private static Location leaveLocation;

    public static void load(ConfigurationNode source) {
        configRoot = source;

        disableOnDamage = source.node(DISABLE_ON_DAMAGE_PATH).getBoolean();
        displayRadius = source.node(DISPLAY_RADIUS_PATH).getInt();
        portable = source.node(PORTABLE_PATH).getBoolean();
        staticRadius = source.node(STATIC_RADIUS_PATH).getInt();
        alwaysDisplay = source.node(ALWAYS_DISPLAY_PATH).getBoolean();
        rotationSpeed = source.node(ROTATION_SPEED_PATH).getInt();
        spawnDelay = source.node(SPAWN_DELAY_PATH).getInt();
        despawnDelay = source.node(DESPAWN_DELAY_PATH).getInt();
        applyCosmeticsOnClose = source.node(APPLY_COSMETICS_ON_CLOSE).getBoolean();
        equipPumpkin = source.node(EQUIP_PUMPKIN_WARDROBE).getBoolean();
        returnLastLocation = source.node(RETURN_LAST_LOCATION).getBoolean();
        try {
            wardrobeLocation = LocationSerializer.INSTANCE.deserialize(Location.class, source.node(STATIC_LOCATION_PATH));
            MessagesUtil.sendDebugMessages("Wardrobe Location: " + wardrobeLocation);
            viewerLocation = LocationSerializer.INSTANCE.deserialize(Location.class, source.node(VIEWER_LOCATION_PATH));
            MessagesUtil.sendDebugMessages("Viewer Location: " + viewerLocation);
            leaveLocation = Utils.replaceIfNull(LocationSerializer.INSTANCE.deserialize(Location.class, source.node(LEAVE_LOCATION_PATH)), viewerLocation);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean getDisableOnDamage() {
        return disableOnDamage;
    }

    public static int getDisplayRadius() {
        return displayRadius;
    }

    public static boolean isPortable() {
        return portable;
    }

    public static boolean isAlwaysDisplay() {
        return alwaysDisplay;
    }

    public static int getStaticRadius() {
        return staticRadius;
    }

    public static int getRotationSpeed() {
        return rotationSpeed;
    }

    public static int getSpawnDelay() {
        return spawnDelay;
    }

    public static int getDespawnDelay() {
        return despawnDelay;
    }

    public static boolean isApplyCosmeticsOnClose() {
        return applyCosmeticsOnClose;
    }
    public static boolean isEquipPumpkin() {
        return equipPumpkin;
    }
    public static boolean isReturnLastLocation() {
        return returnLastLocation;
    }

    public static Location getWardrobeLocation() {
        return wardrobeLocation.clone();
    }

    public static Location getViewerLocation() {
        return viewerLocation;
    }

    public static Location getLeaveLocation() {
        return leaveLocation;
    }

    public static boolean inDistanceOfWardrobe(final Location wardrobeLocation, final Location playerLocation) {
        if (displayRadius == -1) return true;
        if (!wardrobeLocation.getWorld().equals(playerLocation.getWorld())) return false;
        return playerLocation.distanceSquared(wardrobeLocation) <= displayRadius * displayRadius;
    }

    public static boolean inDistanceOfStatic(final Location location) {
        if (wardrobeLocation == null) return false;
        if (staticRadius == -1) return false;
        if (!wardrobeLocation.getWorld().equals(location.getWorld())) return false;
        return wardrobeLocation.distanceSquared(location) <= staticRadius * staticRadius;
    }

    public static void setWardrobeLocation(Location newLocation) {
        wardrobeLocation = newLocation;

        HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();

        plugin.getConfig().set("wardrobe.wardrobe-location." + "world", newLocation.getWorld().getName());
        plugin.getConfig().set("wardrobe.wardrobe-location." + "x", newLocation.getX());
        plugin.getConfig().set("wardrobe.wardrobe-location." + "y", newLocation.getY());
        plugin.getConfig().set("wardrobe.wardrobe-location." + "z", newLocation.getZ());
        plugin.getConfig().set("wardrobe.wardrobe-location." + "yaw", newLocation.getYaw());
        plugin.getConfig().set("wardrobe.wardrobe-location." + "pitch", newLocation.getPitch());

        /* Configuration sets suck
        source.node(WORLD).set(loc.getWorld().getName());
        source.node(X).set(loc.getX());
        source.node(Y).set(loc.getY());
        source.node(Z).set(loc.getZ());
        source.node(YAW).set(loc.getYaw());
        source.node(PITCH).set(loc.getPitch());
         */

        HMCCosmeticsPlugin.getInstance().saveConfig();
    }

    public static void setViewerLocation(Location newLocation) {
        viewerLocation = newLocation;

        HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();

        plugin.getConfig().set("wardrobe.viewer-location." + "world", newLocation.getWorld().getName());
        plugin.getConfig().set("wardrobe.viewer-location." + "x", newLocation.getX());
        plugin.getConfig().set("wardrobe.viewer-location." + "y", newLocation.getY());
        plugin.getConfig().set("wardrobe.viewer-location." + "z", newLocation.getZ());
        plugin.getConfig().set("wardrobe.viewer-location." + "yaw", newLocation.getYaw());
        plugin.getConfig().set("wardrobe.viewer-location." + "pitch", newLocation.getPitch());

        HMCCosmeticsPlugin.getInstance().saveConfig();
    }

    public static void setLeaveLocation(Location newLocation) {
        leaveLocation = newLocation;

        HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();

        plugin.getConfig().set("wardrobe.leave-location." + "world", newLocation.getWorld().getName());
        plugin.getConfig().set("wardrobe.leave-location." + "x", newLocation.getX());
        plugin.getConfig().set("wardrobe.leave-location." + "y", newLocation.getY());
        plugin.getConfig().set("wardrobe.leave-location." + "z", newLocation.getZ());
        plugin.getConfig().set("wardrobe.leave-location." + "yaw", newLocation.getYaw());
        plugin.getConfig().set("wardrobe.leave-location." + "pitch", newLocation.getPitch());

        HMCCosmeticsPlugin.getInstance().saveConfig();
    }
}
