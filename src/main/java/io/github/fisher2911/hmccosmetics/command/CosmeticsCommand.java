package io.github.fisher2911.hmccosmetics.command;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.CosmeticsMenu;
import io.github.fisher2911.hmccosmetics.message.Message;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Messages;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
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
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("cosmetics")
public class CosmeticsCommand extends CommandBase {

    private final HMCCosmetics plugin;
    private final UserManager userManager;
    private final MessageHandler messageHandler;
    private final CosmeticsMenu cosmeticsMenu;

    public CosmeticsCommand(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
        this.messageHandler = this.plugin.getMessageHandler();
        this.cosmeticsMenu = this.plugin.getCosmeticsMenu();
    }

    @Default
    @Permission(io.github.fisher2911.hmccosmetics.message.Permission.DEFAULT_COMMAND)
    public void defaultCommand(final Player player) {
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

            final java.awt.Color awtColor = java.awt.Color.decode(dyeColor);
            Color color = Color.fromRGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());

            final ArmorItem armorItem = user.getPlayerArmor().getItem(type);

            armorItem.setDye(color.asRGB());

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
    public void setCommand(final CommandSender sender, @Completion("#players") final Player player,
            @Completion("#ids") final String id) {
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

}
