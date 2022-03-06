package io.github.fisher2911.hmccosmetics.config;

import com.comphenix.protocol.events.PacketListener;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.gui.CosmeticGui;
import io.github.fisher2911.hmccosmetics.gui.DyeSelectorGui;
import io.github.fisher2911.hmccosmetics.gui.WrappedGuiItem;
import io.github.fisher2911.hmccosmetics.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;

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
    private static final String APPLY_COSMETICS_ON_CLOSE = "apply-cosmetcics-on-close";
    private static final String OPEN_SOUND = "open-sound";
    private static final String CLOSE_SOUND = "close-sound";
    private static final String STATIC_LOCATION_PATH = "wardrobe-location";
    private static final String VIEWER_LOCATION_PATH = "viewer-location";
    private static final String LEAVE_LOCATION_PATH = "leave-location";

    private final HMCCosmetics plugin;

    private boolean disableOnDamage;
    private int displayRadius;
    private boolean portable;
    private boolean alwaysDisplay;
    private int staticRadius;
    private int rotationSpeed;
    private int spawnDelay;
    private int despawnDelay;
    private boolean applyCosmeticsOnClose;
    private SoundData openSound;
    private SoundData closeSound;
    private Location wardrobeLocation;
    private Location viewerLocation;
    private Location leaveLocation;

    public WardrobeSettings(final HMCCosmetics plugin) {
        this.plugin = plugin;
    }

    public void load() {
        final File file = Path.of(this.plugin.getDataFolder().getPath(), "config.yml").toFile();
        final YamlConfigurationLoader loader = YamlConfigurationLoader.
                builder().
                path(file.toPath()).
                defaultOptions(opts ->
                        opts.serializers(build -> {
                            build.register(SoundData.class, SoundSerializer.INSTANCE);
                            build.register(Location.class, LocationSerializer.INSTANCE);
                        }))
                .build();
        try {
            final var source = loader.load().node(WARDROBE_PATH);
            this.disableOnDamage = source.node(DISABLE_ON_DAMAGE_PATH).getBoolean();
            this.displayRadius = source.node(DISPLAY_RADIUS_PATH).getInt();
            this.portable = source.node(PORTABLE_PATH).getBoolean();
            this.staticRadius = source.node(STATIC_RADIUS_PATH).getInt();
            this.alwaysDisplay = source.node(ALWAYS_DISPLAY_PATH).getBoolean();
            this.rotationSpeed = source.node(ROTATION_SPEED_PATH).getInt();
            this.spawnDelay = source.node(SPAWN_DELAY_PATH).getInt();
            this.despawnDelay = source.node(DESPAWN_DELAY_PATH).getInt();
            this.applyCosmeticsOnClose = source.node(APPLY_COSMETICS_ON_CLOSE).getBoolean();
            this.openSound = source.node(OPEN_SOUND).get(SoundData.class);
            this.closeSound = source.node(CLOSE_SOUND).get(SoundData.class);
            this.wardrobeLocation = source.node(STATIC_LOCATION_PATH).get(Location.class);
            this.viewerLocation = source.node(VIEWER_LOCATION_PATH).get(Location.class);
            this.leaveLocation = Utils.replaceIfNull(source.node(LEAVE_LOCATION_PATH).get(Location.class), this.viewerLocation);
        } catch (final ConfigurateException exception) {
            this.plugin.getLogger().severe("Error loading wardrobe settings");
        }
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

    public int getRotationSpeed() {
        return rotationSpeed;
    }

    public int getSpawnDelay() {
        return spawnDelay;
    }

    public int getDespawnDelay() {
        return despawnDelay;
    }

    public boolean isApplyCosmeticsOnClose() {
        return applyCosmeticsOnClose;
    }

    public Location getWardrobeLocation() {
        return this.wardrobeLocation.clone();
    }

    public Location getViewerLocation() {
        return viewerLocation;
    }

    public Location getLeaveLocation() {
        return leaveLocation;
    }

    public void playOpenSound(final Player player) {
        if (this.openSound == null) return;
        this.openSound.play(player);
    }

    public void playCloseSound(final Player player) {
        if (this.closeSound == null) return;
        this.closeSound.play(player);
    }

    public boolean inDistanceOfWardrobe(final Location wardrobeLocation, final Location playerLocation) {
        if (this.displayRadius == -1) return true;
        if (!wardrobeLocation.getWorld().equals(playerLocation.getWorld())) return false;
        return playerLocation.distanceSquared(wardrobeLocation) <= this.displayRadius * this.displayRadius;
    }

    public boolean inDistanceOfStatic(final Location location) {
        if (this.wardrobeLocation == null) return false;
        if (this.staticRadius == -1) return false;
        if (!this.wardrobeLocation.getWorld().equals(location.getWorld())) return false;
        return this.wardrobeLocation.distanceSquared(location) <= this.staticRadius * this.staticRadius;
    }
}
