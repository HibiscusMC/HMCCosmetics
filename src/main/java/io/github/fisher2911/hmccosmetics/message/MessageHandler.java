package io.github.fisher2911.hmccosmetics.message;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.util.StringUtils;
import io.github.fisher2911.hmccosmetics.util.Utils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MessageHandler {

    private final HMCCosmetics plugin;
    private final Logger logger;
    private final BukkitAudiences adventure;
    private final Map<String, Message> messageMap = new HashMap<>();

    public MessageHandler(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.logger = this.plugin.getLogger();
        this.adventure = BukkitAudiences.create(this.plugin);
    }


    /**
     * Closes adventure
     */

    public void close() {
        adventure.close();
    }

    /**
     *
     * @param sender receiver of message
     * @param key message key
     * @param placeholders placeholders
     */

    public void sendMessage(final CommandSender sender, final Message key, final Map<String, String> placeholders) {
           final String message = StringUtils.applyPlaceholders(this.getMessage(key), placeholders);
           final Component component = Adventure.MINI_MESSAGE.parse(message);
           this.adventure.sender(sender).sendMessage(component);
    }

    /**
     *
     * @param sender receiver of message
     * @param key message key
     */

    public void sendMessage(final CommandSender sender, final Message key) {
        this.sendMessage(sender, key, Collections.emptyMap());
    }

    /**
     *
     * @param player receiver of message
     * @param key message key
     * @param placeholders placeholders
     */

    public void sendActionBar(final Player player, final Message key, final Map<String, String> placeholders) {
        final String message = StringUtils.applyPlaceholders(this.getMessage(key), placeholders);
        Component component = Adventure.MINI_MESSAGE.parse(message);
        this.adventure.player(player).sendActionBar(component);
    }

    /**
     *
     * @param player receiver of message
     * @param key message key
     */

    public void sendActionBar(final Player player, final Message key) {
        this.sendActionBar(player, key, Collections.emptyMap());
    }

    /**
     *
     * @param player receiver of message
     * @param key message key
     * @param placeholders placeholders
     */

    public void sendTitle(final Player player, final Message key, final Map<String, String> placeholders) {
        final String message = StringUtils.applyPlaceholders(this.getMessage(key), placeholders);
        Component component = Adventure.MINI_MESSAGE.parse(message);
        this.adventure.player(player).showTitle(Title.title(component, Component.empty()));
    }

    /**
     *
     * @param player receiver of message
     * @param key message key
     */

    public void sendTitle(final Player player, final Message key) {
        this.sendTitle(player, key, Collections.emptyMap());
    }

    /**
     *
     * @param key message key
     * @return message, or empty string if message not found
     */

    public String getMessage(final Message key) {
        return this.messageMap.getOrDefault(key.getKey(), key).getMessage();
    }

    /**
     * Loads all messages from messages.yml
     */

    public void load() {
        final String fileName = "messages.yml";

        final File file = new File(this.plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            this.plugin.saveResource(fileName, false);
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        String prefix = config.getString("prefix");

        if (prefix == null) {
            prefix = "";
        }

        for (final String key : config.getKeys(false)) {

            final String message = Utils.replaceIfNull(config.getString(key), "", value -> {
                if (value == null) {
                    this.logger.warning(String.format(ErrorMessages.ITEM_NOT_FOUND, "message", fileName));
                }
            }).replace(Placeholder.PREFIX, prefix);

            final Message.Type messageType = Utils.stringToEnum(
                    Utils.replaceIfNull(config.getString("type"), "")
                    , Message.Type.class, Message.Type.MESSAGE
            );

            this.messageMap.put(key, new Message(key, message, messageType));
        }
    }
}
