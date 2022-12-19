package com.hibiscusmc.hmccosmetics.gui;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import org.apache.commons.io.FilenameUtils;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Menus {

    private static HashMap<String, Menu> MENUS = new HashMap<>();

    public static void addMenu(Menu menu) {
        MENUS.put(menu.getId().toUpperCase(), menu);
    }

    public static Menu getMenu(String id) {
        return MENUS.get(id.toUpperCase());
    }

    public static Collection<Menu> getMenu() {
        return MENUS.values();
    }

    public static boolean hasMenu(String id) {
        return MENUS.containsKey(id.toUpperCase());
    }

    public static boolean hasMenu(Menu menu) {
        return MENUS.containsValue(menu);
    }

    public static List<String> getMenuNames() {
        List<String> names = new ArrayList<>();

        for (Menu menu : MENUS.values()) {
            names.add(menu.getId());
        }

        return names;
    }

    public static void setup() {
        MENUS.clear();

        File cosmeticFolder = new File(HMCCosmeticsPlugin.getInstance().getDataFolder() + "/menus");
        if (!cosmeticFolder.exists()) cosmeticFolder.mkdir();

        File[] directoryListing = cosmeticFolder.listFiles();
        if (directoryListing == null) return;

        for (File child : directoryListing) {
            if (child.toString().contains(".yml") || child.toString().contains(".yaml")) {
                HMCCosmeticsPlugin.getInstance().getLogger().info("Scanning " + child);
                // Loads file
                YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(child.toPath()).build();
                CommentedConfigurationNode root;
                try {
                    root = loader.load();
                } catch (ConfigurateException e) {
                    throw new RuntimeException(e);
                }
                new Menu(FilenameUtils.removeExtension(child.getName()), root);
            }
        }
    }
}
