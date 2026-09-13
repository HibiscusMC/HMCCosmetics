package com.hibiscusmc.hmccosmetics.gui.special.impl;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.gui.special.DyeMenu;
import com.hibiscusmc.hmccosmetics.util.HMCCServerUtils;
import dev.triumphteam.gui.builder.gui.ChestGuiBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.lojosho.hibiscuscommons.config.serializer.ItemSerializer;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import me.lojosho.hibiscuscommons.nms.NMSHandlers;
import me.lojosho.hibiscuscommons.util.MessagesUtil;
import me.lojosho.shaded.configurate.CommentedConfigurationNode;
import me.lojosho.shaded.configurate.ConfigurateException;
import me.lojosho.shaded.configurate.ConfigurationNode;
import me.lojosho.shaded.configurate.serialize.SerializationException;
import me.lojosho.shaded.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class InternalDyeMenu implements DyeMenu {

    private List<PrimaryColor> PRIMARY_COLORS;

    private List<String> FORMAT = List.of();
    private int ROWS;
    private final ArrayList<Integer> PRIMARY_COLORS_SLOTS = new ArrayList<>();
    private final ArrayList<Integer> SECONDARY_COLORS_SLOTS = new ArrayList<>();

    private int INPUT_SLOT;
    private int OUTPUT_SLOT;

    private @Nullable ItemStack PRIMARY_COLOR_ITEM = null;
    private @Nullable ItemStack SECONDARY_COLOR_ITEM = null;

    @Override
    public void reload() {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(Path.of(HMCCosmeticsPlugin.getInstance().getDataFolder() + "/menus/functional/internal_dye_menu.yml")).build();
        CommentedConfigurationNode config;
        try {
            config = loader.load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }


        try {
            FORMAT = config.node("format").getList(String.class);
        } catch (SerializationException e) {
            e.printStackTrace();
        }
        if (FORMAT == null) {
            MessagesUtil.sendDebugMessages("Format for the internal dye menu is invalid!", Level.WARNING);
            throw new RuntimeException();
        }

        ROWS = FORMAT.size();
        StringBuilder builder = new StringBuilder();
        for (String row : FORMAT) {
            builder.append(row);
        }
        final String formatString = builder.toString();

        PRIMARY_COLORS_SLOTS.clear();
        SECONDARY_COLORS_SLOTS.clear();
        for (int i = 0; i < (ROWS * 9) - 1; i++) {
            char character = formatString.charAt(i);
            switch (character) {
                case '$' -> PRIMARY_COLORS_SLOTS.add(i);
                case '%' -> SECONDARY_COLORS_SLOTS.add(i);
            }
        }

        if (!config.node("primary-color-item").virtual()) {
            try {
                PRIMARY_COLOR_ITEM = ItemSerializer.INSTANCE.deserialize(ItemStack.class, config.node("primary-color-item"));
                if (PRIMARY_COLOR_ITEM.getType() == Material.AIR) {
                    MessagesUtil.sendDebugMessages("Internal Dye Menu Primary Color Item has returned AIR, defaulting to use the cosmetic item itself", Level.WARNING);
                    PRIMARY_COLOR_ITEM = null;
                }
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        }
        if (!config.node("secondary-color-item").virtual()) {
            try {
                SECONDARY_COLOR_ITEM = ItemSerializer.INSTANCE.deserialize(ItemStack.class, config.node("secondary-color-item"));
                if (SECONDARY_COLOR_ITEM.getType() == Material.AIR) {
                    MessagesUtil.sendDebugMessages("Internal Dye Menu Secondary Color Item has returned AIR, defaulting to use the cosmetic item itself", Level.WARNING);
                    SECONDARY_COLOR_ITEM = null;
                }
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        }

        PRIMARY_COLORS = loadColorsFromConfig(config.node("colors"));

        INPUT_SLOT = Settings.getDyeMenuInputSlot();
        OUTPUT_SLOT = Settings.getDyeMenuOutputSlot();
    }

    @Override
    public void openMenu(@NotNull Player viewer, @NotNull CosmeticHolder cosmeticHolder, @NotNull Cosmetic cosmetic) {
        if (ROWS == 0 || ROWS >= 7) {
            MessagesUtil.sendDebugMessages("Internal Dye Menu formatting is not returning the correct amount of rows (Rows found: " + ROWS + "). Check your internal dye menu config.", Level.WARNING);
            cosmeticHolder.addCosmetic(cosmetic);
            return;
        }

        Gui gui = new ChestGuiBuilder().rows(ROWS).title(MiniMessage.miniMessage().deserialize(Hooks.processPlaceholders(viewer, Settings.getDyeMenuName()))).create();
        gui.setUpdating(true);
        gui.setDefaultClickAction(event -> event.setCancelled(true));
        gui.setDefaultTopClickAction(event -> {
            event.setCancelled(true);
            if (event.getSlot() == OUTPUT_SLOT) {
                gui.close(event.getWhoClicked());
                ItemStack outputItem = event.getInventory().getItem(OUTPUT_SLOT);
                Color color = NMSHandlers.getHandler().getUtilHandler().getColor(outputItem);
                if (color != null) cosmeticHolder.addCosmetic(cosmetic, color);
                else cosmeticHolder.addCosmetic(cosmetic);
            }
        });

        final ItemStack dyingItemStack = cosmetic.getItem();

        gui.setItem(INPUT_SLOT, new GuiItem(dyingItemStack));
        gui.setItem(OUTPUT_SLOT, new GuiItem(dyingItemStack));

        AtomicInteger ran = new AtomicInteger(0);
        for (Integer i : PRIMARY_COLORS_SLOTS) {
            int pRan = ran.getAndAdd(1);
            if (pRan >= PRIMARY_COLORS.size()) {
                MessagesUtil.sendDebugMessages("There are less primary colors than slots for primary colors!", Level.WARNING);
                continue;
            }
            PrimaryColor primaryColor = PRIMARY_COLORS.get(pRan);

            ItemStack primaryColorItem = PRIMARY_COLOR_ITEM != null ? PRIMARY_COLOR_ITEM.clone() : cosmetic.getItem();
            if (primaryColorItem == null) continue;
            primaryColorItem = NMSHandlers.getHandler().getUtilHandler().setColor(primaryColorItem, primaryColor.color);
            primaryColorItem.editMeta(itemMeta -> itemMeta.displayName(MiniMessage.miniMessage().deserialize(primaryColor.name()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
            GuiItem guiItem = new GuiItem(primaryColorItem);

            guiItem.setAction(event -> {
                event.setCancelled(true);

                ItemStack cosmeticItem = NMSHandlers.getHandler().getUtilHandler().setColor(cosmetic.getItem(), primaryColor.color);
                gui.updateItem(OUTPUT_SLOT, new GuiItem(cosmeticItem));

                List<SecondaryColor> secondaryColors = primaryColor.secondaryColors();
                AtomicInteger secondaryRan = new AtomicInteger(0);
                SECONDARY_COLORS_SLOTS.forEach(slot -> {
                    int sRan = secondaryRan.getAndAdd(1);
                    if (sRan >= secondaryColors.size()) {
                        MessagesUtil.sendDebugMessages("There are less secondary colors than slots for primary color " + primaryColor.name + "!", Level.WARNING);
                        return;
                    }
                    SecondaryColor secondaryColor = secondaryColors.get(sRan);

                    ItemStack secondaryItem = SECONDARY_COLOR_ITEM != null ? SECONDARY_COLOR_ITEM.clone() : cosmetic.getItem();
                    secondaryItem = NMSHandlers.getHandler().getUtilHandler().setColor(secondaryItem, secondaryColor.color);
                    secondaryItem.editMeta(itemMeta -> itemMeta.displayName(MiniMessage.miniMessage().deserialize(secondaryColor.name()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
                    GuiItem secondaryGuiItem = new GuiItem(secondaryItem);
                    secondaryGuiItem.setAction(secondaryEvent -> {
                        ItemStack secondaryItemStack = NMSHandlers.getHandler().getUtilHandler().setColor(dyingItemStack.clone(), secondaryColor.color);
                        gui.updateItem(OUTPUT_SLOT, new GuiItem(secondaryItemStack));
                    });

                    gui.updateItem(slot, secondaryGuiItem);
                });
            });
            gui.setItem(i, guiItem);
        }


        gui.open(viewer);
    }

    /**
     * Loads all primary colors from the configuration node
     * @param rootNode The root configuration node containing color definitions
     * @return List of PrimaryColor objects
     */
    public static List<PrimaryColor> loadColorsFromConfig(ConfigurationNode rootNode) {
        List<PrimaryColor> primaryColors = new ArrayList<>();

        // Iterate through all child nodes (each represents a primary color)
        Map<Object, ? extends ConfigurationNode> colorNodes = rootNode.childrenMap();

        for (Map.Entry<Object, ? extends ConfigurationNode> entry : colorNodes.entrySet()) {
            ConfigurationNode colorNode = entry.getValue();

            try {
                PrimaryColor primaryColor = parsePrimaryColor(colorNode);
                primaryColors.add(primaryColor);
            } catch (Exception e) {
                // Log error but continue processing other colors
                System.err.println("Failed to parse primary color: " + entry.getKey() + " - " + e.getMessage());
            }
        }

        return primaryColors;
    }

    /**
     * Parses a single primary color from a configuration node
     */
    private static PrimaryColor parsePrimaryColor(ConfigurationNode node) {
        // Get primary color name (with MiniMessage formatting)
        String nameWithFormatting = node.node("name").getString("");
        Component nameComponent = MiniMessage.miniMessage().deserialize(nameWithFormatting);
        String plainName = extractPlainText(nameComponent);

        // Get primary color hex value
        String colorHex = node.node("color").getString("#FFFFFF");
        Color primaryColor = HMCCServerUtils.hex2Rgb(colorHex);

        // Parse secondary colors
        List<SecondaryColor> secondaryColors = new ArrayList<>();
        ConfigurationNode subcolorsNode = node.node("subcolors");

        if (!subcolorsNode.virtual() && subcolorsNode.isList()) {
            List<? extends ConfigurationNode> subcolorList = subcolorsNode.childrenList();

            for (ConfigurationNode subcolorNode : subcolorList) {
                try {
                    SecondaryColor secondaryColor = parseSecondaryColor(subcolorNode);
                    secondaryColors.add(secondaryColor);
                } catch (Exception e) {
                    System.err.println("Failed to parse secondary color: " + e.getMessage());
                }
            }
        }

        return new PrimaryColor(plainName, primaryColor, secondaryColors);
    }

    /**
     * Parses a single secondary color from a configuration node
     */
    private static SecondaryColor parseSecondaryColor(ConfigurationNode node) {
        // Get secondary color name (with MiniMessage formatting)
        String nameWithFormatting = node.node("name").getString("");
        Component nameComponent = MiniMessage.miniMessage().deserialize(nameWithFormatting);
        String plainName = extractPlainText(nameComponent);

        // Get secondary color hex value
        String colorHex = node.node("color").getString("#FFFFFF");
        Color secondaryColor = HMCCServerUtils.hex2Rgb(colorHex);

        return new SecondaryColor(plainName, secondaryColor);
    }

    /**
     * Extracts plain text from a Component (removes MiniMessage formatting)
     */
    private static String extractPlainText(Component component) {
        // Use PlainTextComponentSerializer to extract plain text
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(component);
    }


    public record PrimaryColor(String name, Color color, List<SecondaryColor> secondaryColors) {}

    public record SecondaryColor(String name, Color color) {}
}

