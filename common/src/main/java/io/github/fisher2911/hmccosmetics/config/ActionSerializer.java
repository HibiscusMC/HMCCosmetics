package io.github.fisher2911.hmccosmetics.config;

import com.comphenix.protocol.wrappers.EnumWrappers;
import dev.triumphteam.gui.components.GuiAction;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.message.Message;
import io.github.fisher2911.hmccosmetics.util.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ActionSerializer implements TypeSerializer<GuiAction<InventoryClickEvent>> {

    public static final ActionSerializer INSTANCE = new ActionSerializer();
    private static final HMCCosmetics plugin;

    private static final String OPEN_MENU = "open-menu";
    private static final String SEND_MESSAGE = "send-message";
    private static final String SOUND = "sound";
    private static final String SOUND_NAME = "name";
    private static final String SOUND_VOLUME = "volume";
    private static final String SOUND_PITCH = "pitch";
    private static final String SOUND_CATEGORY = "category";

    static {
        plugin = HMCCosmetics.getPlugin(HMCCosmetics.class);
    }

    private ActionSerializer() {
    }

    private ConfigurationNode nonVirtualNode(final ConfigurationNode source, final Object... path)
            throws SerializationException {
        if (!source.hasChild(path)) {
            throw new SerializationException(
                    "Required field " + Arrays.toString(path) + " was not present in node");
        }

        return source.node(path);
    }

    @Override
    public GuiAction<InventoryClickEvent> deserialize(final Type type, final ConfigurationNode source) {
        final var children = source.childrenMap();
        final List<Consumer<InventoryClickEvent>> consumers = new ArrayList<>();
        for (final var entry : children.entrySet()) {
            final String clickType = entry.getKey().toString();
            if (clickType == null) continue;
            consumers.add(this.parseAction(entry.getValue(), clickType.toUpperCase(Locale.ROOT)));
        }

        return event -> {
            for (final Consumer<InventoryClickEvent> consumer : consumers) {
                consumer.accept(event);
            }
        };
    }

    private Consumer<InventoryClickEvent> parseAction(final ConfigurationNode node, final String clickType) {
        final ConfigurationNode openMenuNode = node.node(OPEN_MENU);
        final ConfigurationNode sendMessageNode = node.node(SEND_MESSAGE);
        final ConfigurationNode soundNode = node.node(SOUND);
        final ConfigurationNode soundNameNode = soundNode.node(SOUND_NAME);
        final ConfigurationNode volumeNode = soundNode.node(SOUND_VOLUME);
        final ConfigurationNode pitchNode = soundNode.node(SOUND_PITCH);
        final ConfigurationNode categoryNode = soundNode.node(SOUND_CATEGORY);

        final String openMenu = openMenuNode.getString();
        final String sendMessage = sendMessageNode.getString();

        final SoundData soundData;

        final String soundName = soundNameNode.getString();
        final String category = categoryNode.getString();
        final int volume = volumeNode.getInt();
        final int pitch = pitchNode.getInt();
        if (soundName == null || category == null) {
            soundData = null;
        } else {
            soundData = new SoundData(
                    soundName,
                    EnumWrappers.SoundCategory.valueOf(category),
                    volume,
                    pitch
            );
        }

        final ClickType click = Utils.stringToEnum(clickType, ClickType.class, ClickType.UNKNOWN);

        return event -> {
            if (click != ClickType.UNKNOWN && event.getClick() != click) return;
            if (!(event.getWhoClicked() instanceof final Player player)) return;
            if (soundData != null) {
                soundData.play(player);
            }

            if (sendMessage != null) plugin.getMessageHandler().sendMessage(
                    player,
                    new Message("", sendMessage)
            );
            if (openMenu != null) plugin.getCosmeticsMenu().openMenu(openMenu, event.getWhoClicked());
        };
    }

    @Override
    public void serialize(final Type type, @Nullable final GuiAction<InventoryClickEvent> obj, final ConfigurationNode node) throws SerializationException {

    }

}
