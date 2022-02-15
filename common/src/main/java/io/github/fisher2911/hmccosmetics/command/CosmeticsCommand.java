package io.github.fisher2911.hmccosmetics.command;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.Settings;
import io.github.fisher2911.hmccosmetics.config.WardrobeSettings;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.CosmeticsMenu;
import io.github.fisher2911.hmccosmetics.message.Message;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Messages;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.task.TaskChain;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import io.github.fisher2911.hmccosmetics.user.Wardrobe;
import io.github.fisher2911.hmccosmetics.util.StringUtils;

import java.util.Map;
import java.util.Optional;

import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.Default;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("cosmetics")
public class CosmeticsCommand extends CommandBase {

    private final HMCCosmetics plugin;
    private final UserManager userManager;
    private final MessageHandler messageHandler;
    private final CosmeticsMenu cosmeticsMenu;
    private final Settings settings;

    public CosmeticsCommand(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
        this.messageHandler = this.plugin.getMessageHandler();
        this.cosmeticsMenu = this.plugin.getCosmeticsMenu();
        this.settings = this.plugin.getSettings();
    }

    @Default
    @Permission(io.github.fisher2911.hmccosmetics.message.Permission.DEFAULT_COMMAND)
    public void defaultCommand(final Player player) {
        final Optional<User> optionalUser = this.userManager.get(player.getUniqueId());
        if (optionalUser.isEmpty()) {
            this.cosmeticsMenu.openDefault(player);
            return;
        }
        final User user = optionalUser.get();
        final Wardrobe wardrobe = user.getWardrobe();
        if (wardrobe.isActive() &&
                !this.settings.getWardrobeSettings().inDistanceOfWardrobe(wardrobe.getCurrentLocation(), player.getLocation())) {
            wardrobe.setActive(false);
            wardrobe.despawnFakePlayer(player);
            this.messageHandler.sendMessage(
                    player,
                    Messages.CLOSED_WARDROBE
            );
        }
        this.cosmeticsMenu.openDefault(player);
    }

    @SubCommand("reload")
    @Permission(io.github.fisher2911.hmccosmetics.message.Permission.RELOAD_COMMAND)
    public void reloadCommand(final CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(
                this.plugin,
                () -> {
                    this.plugin.reload();
                    this.messageHandler.sendMessage(
                            sender,
                            Messages.RELOADED
                    );
                }
        );
    }

    @SubCommand("dye")
    @Permission(io.github.fisher2911.hmccosmetics.message.Permission.DYE_COMMAND)
    public void dyeArmor(final Player player, @Completion("#types") String typeString,
                         final @me.mattstudios.mf.annotations.Optional String dyeColor) {

        final Optional<User> optionalUser = this.userManager.get(player.getUniqueId());

        if (optionalUser.isEmpty()) {
            return;
        }

        try {
            final ArmorItem.Type type = ArmorItem.Type.valueOf(typeString.toUpperCase());

            final User user = optionalUser.get();

            if (dyeColor == null) {
                this.cosmeticsMenu.openDyeSelectorGui(user, type);
                return;
            }

            final ArmorItem armorItem = user.getPlayerArmor().getItem(type);

            if (dyeColor != null) {
                this.setDyeColor(dyeColor, armorItem, player);
            }

            this.userManager.setItem(user, armorItem);

            this.messageHandler.sendMessage(
                    player,
                    Messages.SET_DYE_COLOR,
                    Map.of(Placeholder.ITEM, StringUtils.formatArmorItemType(typeString))
            );

        } catch (final IllegalArgumentException exception) {
            this.messageHandler.sendMessage(
                    player,
                    Messages.INVALID_TYPE);
        }
    }

    @SubCommand("help") // WORK IN PROGRESS (WIP)
    @Permission(io.github.fisher2911.hmccosmetics.message.Permission.HELP_COMMAND)
    public void helpCommand(final CommandSender sender) {
        this.messageHandler.sendMessage(
                sender,
                Messages.HELP_COMMAND
        );
    }

    @SubCommand("add")
    @Permission(io.github.fisher2911.hmccosmetics.message.Permission.SET_COSMETIC_COMMAND)
    public void setCommand(
            final CommandSender sender,
            @Completion("#players") final Player player,
            @Completion("#ids") final String id,
            final @me.mattstudios.mf.annotations.Optional String dyeColor) {
        final Optional<User> userOptional = this.userManager.get(player.getUniqueId());

        if (userOptional.isEmpty()) {
            this.messageHandler.sendMessage(
                    sender,
                    Messages.INVALID_USER
            );
            return;
        }

        final User user = userOptional.get();
        final ArmorItem armorItem = this.plugin.getCosmeticManager().getArmorItem(id);

        if (armorItem == null) {
            this.messageHandler.sendMessage(
                    sender,
                    Messages.ITEM_NOT_FOUND
            );
            return;
        }

        if (dyeColor != null) {
            this.setDyeColor(dyeColor, armorItem, player);
        }

        final Message setMessage = Messages.getSetMessage(armorItem.getType());
        final Message setOtherMessage = Messages.getSetOtherMessage(armorItem.getType());
        this.userManager.setItem(user, armorItem);
        this.messageHandler.sendMessage(
                player,
                setMessage
        );
        this.messageHandler.sendMessage(
                sender,
                setOtherMessage,
                Map.of(Placeholder.PLAYER, player.getName(),
                        Placeholder.TYPE, id)
        );
    }

    @SubCommand("remove")
    @Permission(io.github.fisher2911.hmccosmetics.message.Permission.SET_COSMETIC_COMMAND)
    public void removeCommand(final CommandSender sender,
                              @Completion("#players") final Player player, @Completion("#types") String typeString) {
        final Optional<User> userOptional = this.userManager.get(player.getUniqueId());

        if (userOptional.isEmpty()) {
            this.messageHandler.sendMessage(
                    sender,
                    Messages.INVALID_USER
            );
            return;
        }

        final User user = userOptional.get();

        try {
            final ArmorItem.Type type = ArmorItem.Type.valueOf(typeString.toUpperCase());

            final Message setOtherMessage = Messages.getSetOtherMessage(type);
            this.userManager.removeItem(user, type);
            this.messageHandler.sendMessage(
                    sender,
                    setOtherMessage,
                    Map.of(Placeholder.PLAYER, player.getName(),
                            Placeholder.TYPE, "none")
            );
        } catch (final IllegalArgumentException exception) {
            this.messageHandler.sendMessage(player, Messages.INVALID_TYPE);
        }
    }

    @SubCommand("wardrobe")
    @Permission(io.github.fisher2911.hmccosmetics.message.Permission.WARDROBE)
    public void openWardrobe(Player player, @me.mattstudios.mf.annotations.Optional final Player other) {
        if (other != null) {
            if (!player.hasPermission(io.github.fisher2911.hmccosmetics.message.Permission.OPEN_OTHER_WARDROBE)) {
                this.messageHandler.sendMessage(
                        player,
                        Messages.NO_PERMISSION
                );
                return;
            }
            this.messageHandler.sendMessage(
                    player,
                    Messages.OPENED_OTHER_WARDROBE,
                    Map.of(Placeholder.PLAYER, other.getName())
            );
            player = other;
        }
        final Optional<User> optionalUser = this.plugin.getUserManager().get(player.getUniqueId());
        if (optionalUser.isEmpty()) return;

        final User user = optionalUser.get();

        final Wardrobe wardrobe = user.getWardrobe();
        if (wardrobe.isActive()) {
            this.messageHandler.sendMessage(
                    player,
                    Messages.WARDROBE_ALREADY_OPEN
            );
            return;
        }

        final WardrobeSettings settings = this.settings.getWardrobeSettings();

        final boolean inDistanceOfStatic = settings.inDistanceOfStatic(player.getLocation());

        if (!settings.isPortable() && !inDistanceOfStatic) {
            this.messageHandler.sendMessage(
                    player,
                    Messages.NOT_NEAR_WARDROBE
            );
            return;
        }

        if (settings.isPortable() && !inDistanceOfStatic) {
            if (!player.hasPermission(io.github.fisher2911.hmccosmetics.message.Permission.PORTABLE_WARDROBE)) {
                this.messageHandler.sendMessage(
                        player,
                        Messages.CANNOT_USE_PORTABLE_WARDROBE
                );
                return;
            }
            wardrobe.setCurrentLocation(null);
        }

        wardrobe.setActive(true);

        final Player finalPlayer = player;
        new TaskChain(this.plugin).
                chain(() -> {
                    wardrobe.spawnFakePlayer(finalPlayer);
                }, true).
                chain(() -> {
//                    this.cosmeticsMenu.openDefault(finalPlayer);
                    this.messageHandler.sendMessage(
                            finalPlayer,
                            Messages.OPENED_WARDROBE
                    );
                }).execute();
    }

    private void setDyeColor(final String dyeColor, final ArmorItem armorItem, final CommandSender sender) {
        try {
            final java.awt.Color awtColor = java.awt.Color.decode(dyeColor);
            Color color = Color.fromRGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
            armorItem.setDye(color.asRGB());
        } catch (final NumberFormatException exception) {
            this.messageHandler.sendMessage(
                    sender,
                    Messages.INVALID_COLOR,
                    Map.of(Placeholder.ITEM, dyeColor)
            );
        }
    }
}
