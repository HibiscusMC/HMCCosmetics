package com.hibiscusmc.hmccosmetics.config;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import lombok.Getter;
import me.lojosho.hibiscuscommons.config.serializer.LocationSerializer;
import me.lojosho.shaded.configurate.ConfigurationNode;
import net.kyori.adventure.bossbar.BossBar;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

public class WardrobeSettings {

    private static final String DISABLE_ON_DAMAGE_PATH = "disable-on-damage";
    private static final String PORTABLE_PATH = "portable";
    private static final String ALWAYS_DISPLAY_PATH = "always-display";
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
    private static final String TRY_COSMETICS_WARDROBE = "unchecked-wardrobe-cosmetics";
    private static final String RETURN_LAST_LOCATION = "return-last-location";
    private static final String DAMAGE_KICK_PATH = "damage-kicked";
    private static final String PREVENT_DAMAGE_PATH = "prevent-damage";

    private static final String WARDROBE_MENU_OPTIONS = "menu-options";
    private static final String WARDROBE_ENTER_OPEN_MENU_PATH = "enter-open-menu";

    private static final String GAMEMODE_OPTIONS_PATH = "gamemode-options";
    private static final String FORCE_EXIT_GAMEMODE_PATH = "exit-gamemode-enabled";
    private static final String EXIT_GAMEMODE_PATH = "exit-gamemode";
    private static final String WARDROBES_PATH = "wardrobes";
    private static final String PERMISSION_PATH = "permission";
    private static final String DISTANCE_PATH = "distance";
    private static final String WARDROBE_DEFAULT_MENU = "default-menu";
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
    @Getter
    private static boolean disableOnDamage;
    @Getter
    private static boolean portable;
    @Getter
    private static boolean alwaysDisplay;
    @Getter
    private static int rotationSpeed;
    @Getter
    private static int spawnDelay;
    @Getter
    private static int despawnDelay;
    @Getter
    private static float bossbarProgress;
    @Getter
    private static boolean applyCosmeticsOnClose;
    @Getter
    private static boolean tryCosmeticsInWardrobe;
    @Getter
    private static boolean equipPumpkin;
    @Getter
    private static boolean returnLastLocation;
    @Getter
    private static boolean enabledBossbar;
    @Getter
    private static boolean enterOpenMenu;
    @Getter
    private static boolean forceExitGamemode;
    @Getter
    private static boolean damagedKicked;
    @Getter
    private static boolean preventDamage;
    @Getter
    private static GameMode exitGamemode;
    private static HashMap<String, Wardrobe> wardrobes;
    @Getter
    private static String bossbarMessage;
    @Getter
    private static BossBar.Overlay bossbarOverlay;
    @Getter
    private static BossBar.Color bossbarColor;
    @Getter
    private static boolean enabledTransition;
    @Getter
    private static String transitionText;
    @Getter
    private static int transitionDelay;
    @Getter
    private static int transitionFadeIn;
    @Getter
    private static int transitionStay;
    @Getter
    private static int transitionFadeOut;

    public static void load(ConfigurationNode source) {
        configRoot = source;

        disableOnDamage = source.node(DISABLE_ON_DAMAGE_PATH).getBoolean();
        portable = source.node(PORTABLE_PATH).getBoolean();
        alwaysDisplay = source.node(ALWAYS_DISPLAY_PATH).getBoolean();
        rotationSpeed = source.node(ROTATION_SPEED_PATH).getInt();
        spawnDelay = source.node(SPAWN_DELAY_PATH).getInt();
        despawnDelay = source.node(DESPAWN_DELAY_PATH).getInt();
        applyCosmeticsOnClose = source.node(APPLY_COSMETICS_ON_CLOSE).getBoolean();
        equipPumpkin = source.node(EQUIP_PUMPKIN_WARDROBE).getBoolean();
        returnLastLocation = source.node(RETURN_LAST_LOCATION).getBoolean(false);
        tryCosmeticsInWardrobe = source.node(TRY_COSMETICS_WARDROBE).getBoolean(false);
        damagedKicked = source.node(DAMAGE_KICK_PATH).getBoolean(false);
        preventDamage = source.node(PREVENT_DAMAGE_PATH).getBoolean(true);

        ConfigurationNode menuOptionsNode = source.node(WARDROBE_MENU_OPTIONS);
        enterOpenMenu = menuOptionsNode.node(WARDROBE_ENTER_OPEN_MENU_PATH).getBoolean(false);

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
        for (ConfigurationNode wardrobesNode : source.node(WARDROBES_PATH).childrenMap().values()) {
            String id = wardrobesNode.key().toString();
            try {
                Location npcLocation = LocationSerializer.INSTANCE.deserialize(Location.class, wardrobesNode.node(NPC_LOCATION_PATH));
                MessagesUtil.sendDebugMessages("Wardrobe Location: " + npcLocation);
                Location viewerLocation = LocationSerializer.INSTANCE.deserialize(Location.class, wardrobesNode.node(VIEWER_LOCATION_PATH));
                MessagesUtil.sendDebugMessages("Viewer Location: " + viewerLocation);
                Location leaveLocation = LocationSerializer.INSTANCE.deserialize(Location.class, wardrobesNode.node(LEAVE_LOCATION_PATH));
                if (leaveLocation == null) leaveLocation = viewerLocation;
                MessagesUtil.sendDebugMessages("Leave Location: " + leaveLocation);
                WardrobeLocation wardrobeLocation = new WardrobeLocation(npcLocation, viewerLocation, leaveLocation);

                String permission = wardrobesNode.node(PERMISSION_PATH).getString();
                String defaultMenu = wardrobesNode.node(WARDROBE_DEFAULT_MENU).getString();
                int distance = wardrobesNode.node(DISTANCE_PATH).getInt(-1);

                Wardrobe wardrobe = new Wardrobe(id, wardrobeLocation, permission, distance, defaultMenu);
                addWardrobe(wardrobe);
            } catch (Exception e) {
                MessagesUtil.sendDebugMessages("Unable to create wardrobe " + id, Level.SEVERE);
            }
        }
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

    public static void addWardrobe(Wardrobe wardrobe) {
        wardrobes.put(wardrobe.getId(), wardrobe);
    }

    public static void removeWardrobe(String id) {
        wardrobes.remove(id);
    }

    /**
     * Sets where the NPC/Mannequin will spawn in the wardrobe
     * @param newLocation
     */
    public static void setNPCLocation(Wardrobe wardrobe, Location newLocation) {
        wardrobe.getLocation().setNpcLocation(newLocation);

        HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();

        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".npc-location." + "world", newLocation.getWorld().getName());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".npc-location." + "x", newLocation.getX());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".npc-location." + "y", newLocation.getY());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".npc-location." + "z", newLocation.getZ());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".npc-location." + "yaw", newLocation.getYaw());
        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".npc-location." + "pitch", newLocation.getPitch());

        plugin.saveConfig();
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

        plugin.saveConfig();
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

        plugin.saveConfig();
    }

    public static void setWardrobePermission(Wardrobe wardrobe, String permission) {
        wardrobe.setPermission(permission);

        HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();

        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".permission", permission);

        plugin.saveConfig();
    }

    public static void setWardrobeDistance(Wardrobe wardrobe, int distance) {
        wardrobe.setDistance(distance);

        HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();

        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".distance", distance);

        plugin.saveConfig();
    }

    public static void setWardrobeDefaultMenu(Wardrobe wardrobe, String defaultMenu) {
        wardrobe.setDefaultMenu(defaultMenu);

        HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();

        plugin.getConfig().set("wardrobe.wardrobes." + wardrobe.getId() + ".default-menu", defaultMenu);

        plugin.saveConfig();
    }
}
