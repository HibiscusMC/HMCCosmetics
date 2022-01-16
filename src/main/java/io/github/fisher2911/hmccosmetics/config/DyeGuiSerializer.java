package io.github.fisher2911.hmccosmetics.config;

import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.gui.ColorItem;
import io.github.fisher2911.hmccosmetics.gui.DyeSelectorGui;
import io.github.fisher2911.hmccosmetics.message.Adventure;
import io.github.fisher2911.hmccosmetics.util.StringUtils;
import org.bukkit.Color;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
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


        final Map<Integer, GuiItem> guiItemMap = new HashMap<>();

        final var map = itemsNode.childrenMap();

        for (final var entry : map.entrySet()) {
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

        String title = titleNode.getString();

        if (title == null) title = "";

        return new DyeSelectorGui(
                plugin,
                Adventure.SERIALIZER.serialize(
                        Adventure.MINI_MESSAGE.parse(title)),
                rowsNode.getInt(),
                guiItemMap);
    }

    @Override
    public void serialize(final Type type, @Nullable final DyeSelectorGui obj, final ConfigurationNode node) throws SerializationException {

    }
}
