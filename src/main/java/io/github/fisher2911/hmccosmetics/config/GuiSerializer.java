package io.github.fisher2911.hmccosmetics.config;

import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.gui.CosmeticGui;
import io.github.fisher2911.hmccosmetics.message.Adventure;
import io.github.fisher2911.hmccosmetics.papi.PAPIHook;
import io.github.fisher2911.hmccosmetics.util.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GuiSerializer implements TypeSerializer<CosmeticGui> {

    private static final HMCCosmetics plugin;

    static {
        plugin = HMCCosmetics.getPlugin(HMCCosmetics.class);
    }

    public static final GuiSerializer INSTANCE = new GuiSerializer();

    private GuiSerializer() {}

    private static final String TITLE = "title";
    private static final String ROWS = "rows";
    private static final String ITEMS = "items";

    private ConfigurationNode nonVirtualNode(final ConfigurationNode source, final Object... path) throws SerializationException {
        if (!source.hasChild(path)) {
            throw new SerializationException("Required field " + Arrays.toString(path) + " was not present in node");
        }
        return source.node(path);
    }

    @Override
    public CosmeticGui deserialize(final Type type, final ConfigurationNode source) throws SerializationException {
        final ConfigurationNode titleNode = this.nonVirtualNode(source, TITLE);
        final ConfigurationNode rowsNode = this.nonVirtualNode(source, ROWS);
        final ConfigurationNode itemsNode = source.node(ITEMS);

        final var childrenMap = itemsNode.childrenMap();

        final Map<Integer, GuiItem> guiItemMap = new HashMap<>();

        for (final var entry : childrenMap.entrySet()) {
            if (!(entry.getKey() instanceof final Integer slot)) {
                continue;
            }

            final GuiItem guiItem = ItemSerializer.INSTANCE.deserialize(
                    GuiItem.class,
                    entry.getValue()
            );

            guiItemMap.put(slot, guiItem);
        }

        String title = titleNode.getString();

        if (title == null) title = "";

        return new CosmeticGui(plugin,
                Adventure.SERIALIZER.serialize(
                Adventure.MINI_MESSAGE.parse(title)),
                rowsNode.getInt(),
                guiItemMap);
    }

    @Override
    public void serialize(final Type type, @Nullable final CosmeticGui obj, final ConfigurationNode node) throws SerializationException {

    }
}
