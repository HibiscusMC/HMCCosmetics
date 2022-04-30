package io.github.fisher2911.hmccosmetics.config;

import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.CosmeticGui;
import io.github.fisher2911.hmccosmetics.message.Message;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import io.github.fisher2911.hmccosmetics.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
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
import java.util.stream.Collectors;

public class ActionSerializer implements TypeSerializer<List<CosmeticGuiAction>> {

    public static final ActionSerializer INSTANCE = new ActionSerializer();
    private static final HMCCosmetics plugin;

    private static final String OPEN_MENU = "open-menu";
    private static final String CLOSE_MENU = "close-menu";
    private static final String SET_ITEMS = "set-items";
    private static final String REMOVE_COSMETICS = "remove-cosmetics";
    private static final String SET_COSMETICS = "set-cosmetics";
    private static final String SEND_MESSAGE = "send-message";
    private static final String SEND_MESSAGES = "send-messages";
    private static final String SEND_COMMAND = "send-command";
    private static final String SEND_COMMANDS = "send-commands";
    private static final String SOUND = "sound";

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
        final ConfigurationNode setItemsNode = node.node(SET_ITEMS);
        final ConfigurationNode removeItemsNode = node.node(REMOVE_COSMETICS);
        final ConfigurationNode setCosmeticsNode = node.node(SET_COSMETICS);
        final ConfigurationNode soundNode = node.node(SOUND);
        final boolean closeMenu = node.node(CLOSE_MENU).getBoolean(false);

        final String openMenu = openMenuNode.getString();

        final List<ArmorItem.Type> removeCosmeticTypes = this.loadRemoveTypes(removeItemsNode);
        final int totalRemoveCosmetics = removeCosmeticTypes.size();
        final List<String> setCosmetics = this.loadSetCosmetics(setCosmeticsNode);
        final int totalSetCosmetics = setCosmetics.size();

        final ClickType click = Utils.stringToEnum(clickType, ClickType.class, ClickType.UNKNOWN);
        final Map<Integer, GuiItem> setItems = this.loadSetItems(setItemsNode);

        final Consumer<Player> messageConsumer = this.loadMessages(node, plugin);
        final SoundData soundData = this.loadSoundData(soundNode);

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
                    final UserManager userManager = plugin.getUserManager();
                    final Optional<User> optionalUser = userManager.get(player.getUniqueId());
                    if (optionalUser.isEmpty()) return;
                    final User user = optionalUser.get();
                    final CosmeticManager cosmeticManager = plugin.getCosmeticManager();
                    int index = 0;
                    for (final String id : setCosmetics) {
                        index++;
                        final boolean sendPacket = index == totalSetCosmetics ;
                        final ArmorItem armorItem = cosmeticManager.getArmorItem(id);
                        if (armorItem == null) continue;
                        userManager.setItem(user, armorItem, sendPacket);
                    }
                    index = 0;
                    for (final ArmorItem.Type type : removeCosmeticTypes) {
                        index++;
                        final boolean sendPacket = index == totalSetCosmetics ;
                        userManager.removeItem(user, type, sendPacket);
                    }
                    final CosmeticGui gui = user.getOpenGui();
                    if (gui != null) {
                        for (final var entry : setItems.entrySet()) {
                            final GuiItem item = entry.getValue();
                            gui.updateItem(entry.getKey(), item, user, player);
                        }
                    }
                    if (closeMenu) player.closeInventory();
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
                final GuiItem guiItem = ArmorItemSerializer.INSTANCE.deserialize(GuiItem.class, key);
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

    private List<ArmorItem.Type> loadRemoveTypes(final ConfigurationNode node) {
        try {
            final List<String> typeStrings = node.getList(String.class);
            if (typeStrings == null) return new ArrayList<>();
            return typeStrings.stream().map(
                    string -> {
                        try {
                            return ArmorItem.Type.valueOf(string.toUpperCase(Locale.ROOT));
                        } catch (final IllegalArgumentException exception) {
                            plugin.getLogger().severe(string + " is not a valid cosmetic type.");
                        }
                        return null;
                    }
            ).
                    filter(type -> type != null).
                    collect(Collectors.toList());
        } catch (final SerializationException exception) {
            exception.printStackTrace();
        }
        return new ArrayList<>();
    }

    private SoundData loadSoundData(final ConfigurationNode node) {
        try {
            return SoundSerializer.INSTANCE.deserialize(SoundData.class, node);
        } catch (final ConfigurateException exception) {
            return null;
        }
    }

    private List<String> loadSetCosmetics(final ConfigurationNode node) {
        try {
            return node.getList(String.class);
        } catch (final ConfigurateException exception) {
            return new ArrayList<>();
        }
    }

    @Override
    public void serialize(final Type type, @Nullable final List<CosmeticGuiAction> obj, final ConfigurationNode node) throws SerializationException {
    }

}
