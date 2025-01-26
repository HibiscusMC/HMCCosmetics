package com.hibiscusmc.hmccosmetics.gui;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import lombok.Getter;
import lombok.Setter;
import me.lojosho.shaded.configurate.CommentedConfigurationNode;
import me.lojosho.shaded.configurate.ConfigurateException;
import me.lojosho.shaded.configurate.yaml.YamlConfigurationLoader;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

public class Menus {

    private static final HashMap<String, Menu> MENUS = new HashMap<>();
    private static final HashMap<UUID, Long> COOLDOWNS = new HashMap<>();

    public static void addMenu(@NotNull Menu menu) {
        MENUS.put(menu.getId().toUpperCase(), menu);
    }

    @Nullable
    public static Menu getMenu(@NotNull String id) {
        return MENUS.get(id.toUpperCase());
    }

    @Contract(pure = true)
    @NotNull
    public static Collection<Menu> getMenu() {
        return MENUS.values();
    }

    public static boolean hasMenu(@NotNull String id) {
        return MENUS.containsKey(id.toUpperCase());
    }

    public static boolean hasMenu(@NotNull Menu menu) {
        return MENUS.containsValue(menu);
    }

    public static boolean hasDefaultMenu() {
        return MENUS.containsKey(Settings.getDefaultMenu());
    }

    @Nullable
    public static Menu getDefaultMenu() {
        return Menus.getMenu(Settings.getDefaultMenu());
    }

    @NotNull
    public static List<String> getMenuNames() {
        List<String> names = new ArrayList<>();

        for (Menu menu : MENUS.values()) {
            names.add(menu.getId());
        }

        return names;
    }

    public static Collection<Menu> values() {
        return MENUS.values();
    }

    public static void addCooldown(UUID uuid, long time) {
        COOLDOWNS.put(uuid, time);
    }

    public static Long getCooldown(UUID uuid) {
        return COOLDOWNS.getOrDefault(uuid, 0L);
    }

    public static void removeCooldown(UUID uuid) {
        COOLDOWNS.remove(uuid);
    }

    public static void setup() {
        MENUS.clear();
        COOLDOWNS.clear();

        File cosmeticFolder = new File(HMCCosmeticsPlugin.getInstance().getDataFolder() + "/menus");
        if (!cosmeticFolder.exists()) cosmeticFolder.mkdir();

        // Recursive file lookup
        try (Stream<Path> walkStream = Files.walk(cosmeticFolder.toPath())) {
            walkStream.filter(p -> p.toFile().isFile()).forEach(child -> {
                if (child.toString().endsWith("yml") || child.toString().endsWith("yaml")) {
                    MessagesUtil.sendDebugMessages("Scanning " + child);
                    // Loads file
                    YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(child).build();
                    CommentedConfigurationNode root;
                    try {
                        root = loader.load();
                    } catch (ConfigurateException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        new Menu(FilenameUtils.removeExtension(child.getFileName().toString()), root);
                    } catch (Exception e) {
                        MessagesUtil.sendDebugMessages("Unable to create menu in " + child.getFileName().toString(), Level.WARNING);
                        if (Settings.isDebugMode()) e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
