package io.github.fisher2911.hmccosmetics.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.ColorItem;
import io.github.fisher2911.hmccosmetics.gui.DyeSelectorGui;
import io.github.fisher2911.hmccosmetics.message.Adventure;
import io.github.fisher2911.hmccosmetics.util.StringUtils;
import io.th0rgal.oraxen.utils.armorequipevent.ArmorType;
import org.bukkit.Color;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class DyeGuiSerializer implements TypeSerializer<DyeSelectorGui> {

    private static final HMCCosmetics plugin;

    static {
        plugin = HMCCosmetics.getPlugin(HMCCosmetics.class);
    }

    public static final DyeGuiSerializer INSTANCE = new DyeGuiSerializer();

    private DyeGuiSerializer() {}

    private static final String TITLE = "title";
    private static final String ROWS = "rows";
    private static final String ITEMS = "items";
    private static final String COSMETICS_SLOTS = "cosmetics-slots";
    private static final String SET_COLOR = "set-color";
    private static final String RED = "red";
    private static final String GREEN = "green";
    private static final String BLUE = "blue";

    private ConfigurationNode nonVirtualNode(final ConfigurationNode source, final Object... path) throws SerializationException {
        if (!source.hasChild(path)) {
            throw new SerializationException("Required field " + Arrays.toString(path) + " was not present in node");
        }
        return source.node(path);
    }

    @Override
    public DyeSelectorGui deserialize(final Type type, final ConfigurationNode source) throws SerializationException {
        final ConfigurationNode titleNode = this.nonVirtualNode(source, TITLE);
        final ConfigurationNode rowsNode = this.nonVirtualNode(source, ROWS);
        final ConfigurationNode itemsNode = source.node(ITEMS);
        final ConfigurationNode cosmeticSlotsNode = source.node(COSMETICS_SLOTS);


        final Map<Integer, GuiItem> guiItemMap = new HashMap<>();

        final var itemMap = itemsNode.childrenMap();
        final var cosmeticSlotsMap = cosmeticSlotsNode.childrenMap();

        for (final var entry : itemMap.entrySet()) {
            if (!(entry.getKey() instanceof final Integer slot)) {
                continue;
            }

            final var node = entry.getValue();

            final GuiItem guiItem = ItemSerializer.INSTANCE.deserialize(
                    GuiItem.class,
                    node
            );

            final ConfigurationNode colorNode = node.node(SET_COLOR);

            if (colorNode.virtual()) {
                guiItemMap.put(slot, guiItem);
                continue;
            }

            final int red = colorNode.node(RED).getInt();
            final int green = colorNode.node(GREEN).getInt();
            final int blue = colorNode.node(BLUE).getInt();

            guiItemMap.put(slot, new ColorItem(guiItem.getItemStack(), Color.fromRGB(red, green, blue)));
        }

        final BiMap<Integer, ArmorItem.Type> cosmeticSlots = HashBiMap.create();
        int selectedCosmetic = -1;
        for (final var entry : cosmeticSlotsMap.entrySet()) {
            if (!(entry.getKey() instanceof final Integer slot)) {
                continue;
            }

            selectedCosmetic = selectedCosmetic == -1 ? slot : selectedCosmetic;

            final var node = entry.getValue();

            final String typeStr = node.getString();

            try {
                final ArmorItem.Type itemType = ArmorItem.Type.valueOf(typeStr);
                cosmeticSlots.put(slot, itemType);
            } catch (final IllegalArgumentException | NullPointerException exception) {
                plugin.getLogger().severe(typeStr + " is not a valid ArmorItem type in DyeGui!");
            }
        }

        String title = titleNode.getString();

        if (title == null) title = "";

        return new DyeSelectorGui(
                plugin,
                Adventure.SERIALIZER.serialize(
                        Adventure.MINI_MESSAGE.parse(title)),
                rowsNode.getInt(),
                guiItemMap,
                cosmeticSlots,
                selectedCosmetic);
    }

    @Override
    public void serialize(final Type type, @Nullable final DyeSelectorGui obj, final ConfigurationNode node) throws SerializationException {

    }
}
