package com.hibiscusmc.hmccosmetics.util;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import me.lojosho.hibiscuscommons.util.AdventureUtils;
import me.lojosho.shaded.configurate.ConfigurationNode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.logging.Level;

public class MessagesUtil {

    private static String prefix;
    private static final HashMap<String, String> MESSAGES = new HashMap<>();

    public static void setup(@NotNull ConfigurationNode config) {
        MESSAGES.clear();

        prefix = config.node("prefix").getString("");
        for (ConfigurationNode node : config.childrenMap().values()) {
            if (node.virtual()) continue;
            if (node.empty()) continue;
            MESSAGES.put(node.key().toString(), node.getString());
        }
     }

    public static void sendMessage(@NotNull CosmeticUser user, String key) {
        sendMessage(user.getPlayer(), key);
    }

    public static void sendMessage(Player player, String key) {
        Component finalMessage = processString(player, key);
        if (finalMessage == null) return;
        Audience target = BukkitAudiences.create(HMCCosmeticsPlugin.getInstance()).player(player);

        target.sendMessage(finalMessage);
    }

    public static void sendMessage(CommandSender sender, String key) {
        Component finalMessage = processString(null, key);
        if (finalMessage == null) return;
        Audience target = BukkitAudiences.create(HMCCosmeticsPlugin.getInstance()).sender(sender);

        target.sendMessage(finalMessage);
    }

    public static void sendMessage(Player player, String key, TagResolver placeholder) {
        Component finalMessage = processString(player, key, placeholder);
        if (finalMessage == null) return;
        Audience target = BukkitAudiences.create(HMCCosmeticsPlugin.getInstance()).player(player);

        target.sendMessage(finalMessage);
    }

    public static void sendMessageNoKey(Player player, String message) {
        Component finalMessage = processStringNoKey(player, message);
        if (finalMessage == null) return;
        Audience target = BukkitAudiences.create(HMCCosmeticsPlugin.getInstance()).player(player);

        target.sendMessage(finalMessage);
    }

    public static void sendActionBar(Player player, String key) {
        Component finalMessage = processString(player, key);
        if (finalMessage == null) return;
        Audience target = BukkitAudiences.create(HMCCosmeticsPlugin.getInstance()).player(player);

        target.sendActionBar(finalMessage);
    }

    public static void sendTitle(Player player, String message) {
        sendTitle(player, message, 2000, 2000, 2000);
    }

    public static void sendTitle(Player player, String message, int fadein, int stay, int fadeout) {
        Audience target = BukkitAudiences.create(HMCCosmeticsPlugin.getInstance()).player(player);

        Title.Times times = Title.Times.times(Duration.ofMillis(fadein), Duration.ofMillis(stay), Duration.ofMillis(fadeout));
        Title title = Title.title(processStringNoKey(player, message), Component.empty(), times);

        target.showTitle(title);
    }

    public static Component processString(Player player, String key) {
        return processString(player, key, null);
    }

    @Nullable
    public static Component processString(Player player, String key, TagResolver placeholders) {
        if (!MESSAGES.containsKey(key)) return null;
        if (MESSAGES.get(key) == null) return null;
        String message = MESSAGES.get(key);
        if (player != null) message = Hooks.processPlaceholders(player, message);
        message = message.replaceAll("%prefix%", prefix);
        if (placeholders != null ) {
            return AdventureUtils.MINI_MESSAGE.deserialize(message, placeholders);
        }
        return AdventureUtils.MINI_MESSAGE.deserialize(message);
    }

    @NotNull
    public static Component processStringNoKey(String message) {
        return processStringNoKey(null, message, null);
    }

    @NotNull
    public static Component processStringNoKey(Player player, String message) {
        return processStringNoKey(player, message, null);
    }

    @NotNull
    public static Component processStringNoKey(Player player, String message, TagResolver placeholders) {
        message = message.replaceAll("%prefix%", prefix);
        if (player != null) message = Hooks.processPlaceholders(player, message);
        if (placeholders != null ) {
            return AdventureUtils.MINI_MESSAGE.deserialize(message, placeholders);
        }
        return AdventureUtils.MINI_MESSAGE.deserialize(message);
    }

    public static String processStringNoKeyString(Player player, String message) {
        message = message.replaceAll("%prefix%", prefix);
        if (player != null) message = Hooks.processPlaceholders(player, message);
        return message;
    }

    public static void sendDebugMessages(String message) {
        sendDebugMessages(message, Level.INFO);
    }

    public static void sendDebugMessages(String message, Level level) {
        if (!Settings.isDebugMode() && level == Level.INFO) return;
        HMCCosmeticsPlugin.getInstance().getLogger().log(level, message);
    }
}
