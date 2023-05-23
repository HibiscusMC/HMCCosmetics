package com.hibiscusmc.hmccosmetics.config;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.serializer.LocationSerializer;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.misc.Utils;
import net.kyori.adventure.bossbar.BossBar;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;
import java.util.logging.Level;

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
    private static final String NPC_LOCATION_PATH = "npc-location";
    private static final String VIEWER_LOCATION_PATH = "viewer-location";
    private static final String LEAVE_LOCATION_PATH = "leave-location";
    private static final String EQUIP_PUMPKIN_WARDROBE = "equip-pumpkin";
    private static final String RETURN_LAST_LOCATION = "return-last-location";
    private static final String GAMEMODE_OPTIONS_PATH = "gamemode-options";
    private static final String FORCE_EXIT_GAMEMODE_PATH = "exit-gamemode-enabled";
    private static final String EXIT_GAMEMODE_PATH = "exit-gamemode";
    private static final String BOSSBAR_PATH = "bossbar";
    private static final String BOSSBAR_ENABLE_PATH = "enabled";
    private static final String BOSSBAR_TEXT_PATH = "text";
    private static final String BOSSBAR_PROGRESS_PATH = "progress";
    private static final String BOSSBAR_OVERLAY_PATH = "overlay";
    private static final String BOSSBAR_COLOR_PATH = "color";
    private static final String TRANSITION_PATH = "transition";
    private static final String TRANSITION_ENABLE_PATH = "enabled";
    private static final String TRANSITION_DELAY_PATH = "delay";
    private static final String TRANSITION_TEXT_PATH = "text";
    private static final String TRANSITION_FADE_IN_PATH = "title-fade-in";
    private static final String TRANSITION_STAY_PATH = "title-stay";
    private static final String TRANSITION_FADE_OUT_PATH = "title-fade-out";

    private static ConfigurationNode configRoot;
    private static boolean disableOnDamage;
    private static int displayRadius;
    private static boolean portable;
    private static boolean alwaysDisplay;
    private static int staticRadius;
    private static int rotationSpeed;
    private static int spawnDelay;
    private static int despawnDelay;
    private static float bossbarProgress;
    private static boolean applyCosmeticsOnClose;
    private static boolean equipPumpkin;
    private static boolean returnLastLocation;
    private static boolean enabledBossbar;
    private static boolean forceExitGamemode;
    private static GameMode exitGamemode;
    private static HashMap<String, Wardrobe> wardrobes;
    private static String bossbarMessage;
    private static BossBar.Overlay bossbarOverlay;
    private static BossBar.Color bossbarColor;
    private static boolean enabledTransition;
    private static String transitionText;
    private static int transitionDelay;
    private static int transitionFadeIn;
    private static int transitionStay;
    private static int transitionFadeOut;

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
        returnLastLocation = source.node(RETURN_LAST_LOCATION).getBoolean(false);

        ConfigurationNode gamemodeNode = source.node(GAMEMODE_OPTIONS_PATH);
        forceExitGamemode = gamemodeNode.node(FORCE_EXIT_GAMEMODE_PATH).getBoolean(false);
        exitGamemode = GameMode.valueOf(gamemodeNode.node(EXIT_GAMEMODE_PATH).getString("SURVIVAL"));

        ConfigurationNode bossBarNode = source.node(BOSSBAR_PATH);
        enabledBossbar = bossBarNode.node(BOSSBAR_ENABLE_PATH).getBoolean(false);
        bossbarProgress = bossBarNode.node(BOSSBAR_PROGRESS_PATH).getFloat(1.0f);
        bossbarMessage = bossBarNode.node(BOSSBAR_TEXT_PATH).getString("");
        if (EnumUtils.isValidEnum(BossBar.Overlay.class, bossBarNode.node(BOSSBAR_OVERLAY_PATH).getString(""))) {
            bossbarOverlay = BossBar.Overlay.valueOf(bossBarNode.node(BOSSBAR_OVERLAY_PATH).getString(""));
        } else {
            bossbarOverlay = BossBar.Overlay.PROGRESS;
        }

        if (EnumUtils.isValidEnum(BossBar.Color.class, bossBarNode.node(BOSSBAR_COLOR_PATH).getString())) {
            bossbarColor = BossBar.Color.valueOf(bossBarNode.node(BOSSBAR_COLOR_PATH).getString());
        } else {
            bossbarColor = BossBar.Color.YELLOW;
        }

        ConfigurationNode transitionNode = source.node(TRANSITION_PATH);
        enabledTransition = transitionNode.node(TRANSITION_ENABLE_PATH).getBoolean(false);
        transitionText = transitionNode.node(TRANSITION_TEXT_PATH).getString("");
        transitionDelay = transitionNode.node(TRANSITION_DELAY_PATH).getInt(1);
        transitionFadeIn = transitionNode.node(TRANSITION_FADE_IN_PATH).getInt(2000);
        transitionStay = transitionNode.node(TRANSITION_STAY_PATH).getInt(2000);
        transitionFadeOut = transitionNode.node(TRANSITION_FADE_OUT_PATH).getInt(2000);

        wardrobes = new HashMap<>();
        for (ConfigurationNode wardrobesNode : source.node("wardrobes").childrenMap().values()) {
            String id = wardrobesNode.key().toString();
            try {
                Location npcLocation = LocationSerializer.INSTANCE.deserialize(Location.class, wardrobesNode.node(NPC_LOCATION_PATH));
                MessagesUtil.sendDebugMessages("Wardrobe Location: " + npcLocation);
                Location viewerLocation = LocationSerializer.INSTANCE.deserialize(Location.class, wardrobesNode.node(VIEWER_LOCATION_PATH));
                MessagesUtil.sendDebugMessages("Viewer Location: " + viewerLocation);
                Location leaveLocation = Utils.replaceIfNull(LocationSerializer.INSTANCE.deserialize(Location.class, wardrobesNode.node(LEAVE_LOCATION_PATH)), viewerLocation);
                MessagesUtil.sendDebugMessages("Leave Location: " + leaveLocation);
                WardrobeLocation wardrobeLocation = new WardrobeLocation(npcLocation, viewerLocation, leaveLocation);

                String permission = null;
                int distance = -1;
                List<String> playerNames = null;
                if (!wardrobesNode.node("permission").virtual()) permission = wardrobesNode.node("permission").getString();
                if (!wardrobesNode.node("int").virtual()) distance = wardrobesNode.node("int").getInt();

                Wardrobe wardrobe = new Wardrobe(id, wardrobeLocation, permission, distance);
                wardrobes.put(id, wardrobe);
            } catch (Exception e) {
                MessagesUtil.sendDebugMessages("Unable to create wardrobe " + id, Level.SEVERE);
            }
        }

        //throw new RuntimeException(e);
    }

    public static int getDefaultDistance() {
        return staticRadius;
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

    public static Wardrobe getWardrobe(String key) {
        return wardrobes.get(key);
    }

    public static Set<String> getWardrobeNames() {
        return wardrobes.keySet();
    }

    public static Collection<Wardrobe> getWardrobes() {
        return wardrobes.values();
    }

    public static boolean inDistanceOfWardrobe(final Location wardrobeLocation, final Location playerLocation) {
        if (displayRadius == -1) return true;
        if (!wardrobeLocation.getWorld().equals(playerLocation.getWorld())) return false;
        return playerLocation.distanceSquared(wardrobeLocation) <= displayRadius * displayRadius;
    }

    public static boolean inDistanceOfStatic(Wardrobe wardrobe, final Location location) {
        Location wardrobeLocation = wardrobe.getLocation().getNpcLocation();
        if (wardrobeLocation == null) return false;
        if (staticRadius == -1) return true;
        if (!wardrobeLocation.getWorld().equals(location.getWorld())) return false;
        return wardrobeLocation.distanceSquared(location) <= staticRadius * staticRadius;
    }

    public static boolean getEnabledBossbar() {
        return enabledBossbar;
    }

    public static float getBossbarProgress() {
        return bossbarProgress;
    }

    public static String getBossbarText() {
        return bossbarMessage;
    }

    public static BossBar.Overlay getBossbarOverlay() {
        return bossbarOverlay;
    }

    public static BossBar.Color getBossbarColor() {
        return bossbarColor;
    }
    public static boolean isEnabledTransition() {
        return enabledTransition;
    }

    public static String getTransitionText() {
        return transitionText;
    }

    public static int getTransitionDelay() {
        return transitionDelay;
    }
    public static int getTransitionFadeIn() {
        return transitionFadeIn;
    }
    public static int getTransitionStay() {
        return transitionStay;
    }
    public static int getTransitionFadeOut() {
        return transitionFadeOut;
    }

    public static boolean isForceExitGamemode() {
        return forceExitGamemode;
    }

    public static GameMode getExitGamemode() {
        return exitGamemode;
    }

    /**
     * Sets where the NPC/Mannequin will spawn in the wardrobe
     * @param newLocation
     */
    public static void setNPCLocation(Wardrobe wardrobe, Location newLocation) {
        wardrobe.getLocation().setNPCLocation(newLocation);

        HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();

        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".npc-location." + "world", newLocation.getWorld().getName());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".npc-location." + "x", newLocation.getX());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".npc-location." + "y", newLocation.getY());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".npc-location." + "z", newLocation.getZ());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".npc-location." + "yaw", newLocation.getYaw());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".npc-location." + "pitch", newLocation.getPitch());

        HMCCosmeticsPlugin.getInstance().saveConfig();
    }

    /**
     * Sets where the player will view the wardrobe
     * @param newLocation
     */
    public static void setViewerLocation(Wardrobe wardrobe, Location newLocation) {
        wardrobe.getLocation().setViewerLocation(newLocation);

        HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();

        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".viewer-location.world", newLocation.getWorld().getName());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".viewer-location.x", newLocation.getX());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".viewer-location.y", newLocation.getY());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".viewer-location.z", newLocation.getZ());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".viewer-location.yaw", newLocation.getYaw());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".viewer-location.pitch", newLocation.getPitch());

        HMCCosmeticsPlugin.getInstance().saveConfig();
    }

    /**
     * Sets where a player will leave the wardrobe from
     * @param newLocation
     */
    public static void setLeaveLocation(Wardrobe wardrobe, Location newLocation) {
        wardrobe.getLocation().setLeaveLocation(newLocation);

        HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();

        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".leave-location.world", newLocation.getWorld().getName());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".leave-location.x", newLocation.getX());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".leave-location.y", newLocation.getY());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".leave-location.z", newLocation.getZ());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".leave-location.yaw", newLocation.getYaw());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".leave-location.pitch", newLocation.getPitch());

        HMCCosmeticsPlugin.getInstance().saveConfig();
    }
}
