package io.github.fisher2911.hmccosmetics.config;

import com.comphenix.protocol.wrappers.EnumWrappers;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.CosmeticGui;
import io.github.fisher2911.hmccosmetics.message.Message;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ActionSerializer implements TypeSerializer<List<CosmeticGuiAction>> {

    public static final ActionSerializer INSTANCE = new ActionSerializer();
    private static final HMCCosmetics plugin;

    private static final String OPEN_MENU = "open-menu";
    private static final String SET_ITEMS = "set-items";
    private static final String ON_EQUIP = "equip";
    private static final String ON_REMOVE = "unequip";
    private static final String ANY = "any";
    private static final String SEND_MESSAGE = "send-message";
    private static final String SEND_MESSAGES = "send-messages";
    private static final String SEND_COMMAND = "send-command";
    private static final String SEND_COMMANDS = "send-commands";
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
    public List<CosmeticGuiAction> deserialize(final Type type, final ConfigurationNode source) {
        return this.deserialize(type, new ArrayList<>(), source, CosmeticGuiAction.When.ALL);
    }

    private List<CosmeticGuiAction> deserialize(
            final Type type,
            final List<CosmeticGuiAction> consumers,
            final ConfigurationNode source,
            final CosmeticGuiAction.When when) {
        final var children = source.childrenMap();
        for (final var entry : children.entrySet()) {
            final String clickType = entry.getKey().toString();
            try {
                final CosmeticGuiAction.When nextWhen = CosmeticGuiAction.When.valueOf(clickType.toUpperCase());
                this.deserialize(type, consumers, entry.getValue(), nextWhen);
            } catch (final IllegalArgumentException exception) {
                consumers.add(this.parseAction(entry.getValue(), clickType.toUpperCase(Locale.ROOT), when));
            }
        }

        return consumers;
    }

    private CosmeticGuiAction parseAction(
            final ConfigurationNode node,
            final String clickType,
            final CosmeticGuiAction.When when
    ) {
        final ConfigurationNode openMenuNode = node.node(OPEN_MENU);
        final ConfigurationNode soundNode = node.node(SOUND);
        final ConfigurationNode soundNameNode = soundNode.node(SOUND_NAME);
        final ConfigurationNode volumeNode = soundNode.node(SOUND_VOLUME);
        final ConfigurationNode pitchNode = soundNode.node(SOUND_PITCH);
        final ConfigurationNode categoryNode = soundNode.node(SOUND_CATEGORY);
        final ConfigurationNode setItemsNode = node.node(SET_ITEMS);

        final String openMenu = openMenuNode.getString();

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
        final Map<Integer, GuiItem> setItems = this.loadSetItems(setItemsNode);

        final Consumer<Player> messageConsumer = this.loadMessages(node, plugin);

        return new CosmeticGuiAction(
                when,
                event -> {
                    if (click != ClickType.UNKNOWN && event.getClick() != click) return;
                    if (!(event.getWhoClicked() instanceof final Player player)) return;
                    if (soundData != null) {
                        soundData.play(player);
                    }

                    if (openMenu != null) plugin.getCosmeticsMenu().openMenu(openMenu, event.getWhoClicked());
                    messageConsumer.accept(player);
                    final Optional<User> optionalUser = plugin.getUserManager().get(player.getUniqueId());
                    if (optionalUser.isEmpty()) return;
                    final User user = optionalUser.get();
                    final CosmeticGui gui = user.getOpenGui();
                    if (gui != null) {
                        for (final var entry : setItems.entrySet()) {
                            final GuiItem item = entry.getValue();
                            gui.updateItem(entry.getKey(), item, user, player);
                        }
                    }
                }
        );
    }

    private Map<Integer, GuiItem> loadSetItems(final ConfigurationNode node) {
        if (node.virtual()) return Collections.emptyMap();
        final Map<Integer, GuiItem> setItems = new HashMap<>();
        try {
            for (final var entry : node.childrenMap().entrySet()) {
                if (!(entry.getKey() instanceof final Integer slot)) continue;
                final var key = entry.getValue();
                final GuiItem guiItem = ItemSerializer.INSTANCE.deserialize(GuiItem.class, key);
                if (guiItem == null) continue;
                setItems.put(slot, guiItem);
            }
        } catch (final SerializationException exception) {
            HMCCosmetics.getPlugin(HMCCosmetics.class).getLogger().severe(
                    "Error loading set-items"
            );
            return Collections.emptyMap();
        }

        return setItems;
    }

    private static final String CONSOLE = "console";
    private static final String PLAYER = "player";

    private Consumer<Player> loadMessages(final ConfigurationNode source, final HMCCosmetics plugin) {
        final ConfigurationNode messageNode = source.node(SEND_MESSAGE);
        final ConfigurationNode messagesNode = source.node(SEND_MESSAGES);
        final ConfigurationNode commandNode = source.node(SEND_COMMAND);
        final ConfigurationNode commandsNode = source.node(SEND_COMMANDS);

        final List<String> messages = new ArrayList<>();
        final List<String> commands = new ArrayList<>();

        final String message = messageNode.getString();
        if (message != null) messages.add(message);
        final String command = commandNode.getString();
        if (command != null) commands.add(command);

        for (final var node : messagesNode.childrenList()) {
            final String listMessage = node.getString();
            if (listMessage == null) continue;
            messages.add(listMessage);
        }

        for (final var node : commandsNode.childrenList()) {
            final String commandMessage = node.getString();
            if (commandMessage == null) continue;
            commands.add(commandMessage);
        }

        return player -> {
            final String playerName = player.getName();
            final Map<String, String> placeholders = Map.of(Placeholder.PLAYER, playerName);
            final MessageHandler messageHandler = plugin.getMessageHandler();
            for (final String sendMessage : messages) {
                messageHandler.sendMessage(
                        player,
                        new Message("", sendMessage),
                        placeholders
                );
            }

            for (final String sendCommand : commands) {
                final String[] parts = sendCommand.split(":");
                if (parts.length < 2) {
                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            sendCommand.replace(Placeholder.PLAYER, playerName)
                    );
                    continue;
                }


                final String sender = parts[0];
                final String commandToSend = parts[1];

                if (sender.equalsIgnoreCase(CONSOLE)) {
                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            commandToSend.replace(Placeholder.PLAYER, playerName)
                    );
                    continue;
                }


                player.chat("/" + commandToSend.replace(Placeholder.PLAYER, playerName));
            }
        };
    }

    @Override
    public void serialize(final Type type, @Nullable final List<CosmeticGuiAction> obj, final ConfigurationNode node) throws SerializationException {

    }

}
