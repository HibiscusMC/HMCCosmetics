package io.github.fisher2911.hmccosmetics.config;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.Nullable;

import org.bukkit.Location;

public class WardrobeSettings {

    private static final String WARDROBE_PATH = "wardrobe";
    private static final String DISABLE_ON_DAMAGE_PATH = WARDROBE_PATH + ".disable-on-damage";
    private static final String DISPLAY_RADIUS_PATH = WARDROBE_PATH + ".display-radius";
    private static final String PORTABLE_PATH = WARDROBE_PATH + ".portable";
    private static final String ALWAYS_DISPLAY_PATH = WARDROBE_PATH + ".always-display";
    private static final String STATIC_RADIUS_PATH = WARDROBE_PATH + ".static-radius";
    private static final String STATIC_LOCATION_PATH = WARDROBE_PATH + ".wardrobe-location";
    private static final String WORLD_PATH = "world";
    private static final String X_PATH = "x";
    private static final String Y_PATH = "y";
    private static final String Z_PATH = "z";
    private static final String YAW_PATH = "yaw";
    private static final String PITCH_PATH = "pitch";


    private final HMCCosmetics plugin;

    private boolean disableOnDamage;
    private int displayRadius;
    private boolean portable;
    private boolean alwaysDisplay;
    private int staticRadius;
    private Location location;

    public WardrobeSettings(final HMCCosmetics plugin) {
        this.plugin = plugin;
    }

    public void load() {
        final FileConfiguration config = this.plugin.getConfig();
        this.disableOnDamage = config.getBoolean(DISABLE_ON_DAMAGE_PATH);
        this.displayRadius = config.getInt(DISPLAY_RADIUS_PATH);
        this.portable = config.getBoolean(PORTABLE_PATH);
        this.staticRadius = config.getInt(STATIC_RADIUS_PATH);
        this.alwaysDisplay = config.getBoolean(ALWAYS_DISPLAY_PATH);
        final ConfigurationSection locationSection = config.getConfigurationSection(STATIC_LOCATION_PATH);
        if (locationSection == null) return;
        this.location = this.loadLocation(locationSection);
    }

    @Nullable
    private Location loadLocation(final ConfigurationSection section) {
        final String worldName = section.getString(WORLD_PATH);
        final int x = section.getInt(X_PATH);
        final int y = section.getInt(Y_PATH);
        final int z = section.getInt(Z_PATH);
        final float yaw = (float) section.getDouble(YAW_PATH);
        final float pitch = (float) section.getDouble(PITCH_PATH);

        if (worldName == null || worldName.isBlank()) return null;
        final World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public boolean getDisableOnDamage() {
        return disableOnDamage;
    }

    public int getDisplayRadius() {
        return displayRadius;
    }

    public boolean isPortable() {
        return portable;
    }

    public boolean isAlwaysDisplay() {
        return alwaysDisplay;
    }

    public int getStaticRadius() {
        return staticRadius;
    }

    public Location getLocation() {
        return location.clone();
    }

    public boolean inDistanceOfWardrobe(final Location wardrobeLocation, final Location playerLocation) {
        if (this.displayRadius == -1) return true;
        return playerLocation.distanceSquared(wardrobeLocation) <= this.displayRadius * this.displayRadius;
    }

    public boolean inDistanceOfStatic(final Location location) {
        if (this.location == null) return false;
        if (this.staticRadius == -1) return false;
        return this.location.distanceSquared(location) <= this.staticRadius * this.staticRadius;
    }
}
