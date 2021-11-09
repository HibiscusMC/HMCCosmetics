package io.github.fisher2911.hmccosmetics.command;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.CosmeticsMenu;
import io.github.fisher2911.hmccosmetics.gui.DyeSelectorGui;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Default;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

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

    @SubCommand("dye")
    @Permission(io.github.fisher2911.hmccosmetics.message.Permission.DYE_COMMAND)
    public void dyeArmor(final Player player, String typeString) {

        final Optional<User> optionalUser = this.userManager.get(player.getUniqueId());

        if (optionalUser.isEmpty()) {
            return;
        }

        final ArmorItem.Type type = ArmorItem.Type.valueOf(typeString);

        final User user = optionalUser.get();

        final ArmorItem armorItem = switch (type) {
            case HAT -> user.getPlayerArmor().getHat();
            case BACKPACK -> user.getPlayerArmor().getBackpack();
        };

        new DyeSelectorGui("test", 3, Map.of(
                10, Color.fromRGB(1, 2, 3),
                11, Color.fromRGB(0, 0, 255)
        ), armorItem
        ).getGui(user).open(player);
    }

}
