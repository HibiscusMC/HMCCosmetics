package com.hibiscusmc.hmccosmetics.command;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticEmoteType;
import com.hibiscusmc.hmccosmetics.database.Database;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.gui.special.DyeMenu;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.ServerUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CosmeticCommand implements CommandExecutor {

    // cosmetics apply cosmetics playerName
    //             0      1        2

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        boolean silent = false;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                // Console
                return true;
            }
            if (!sender.hasPermission("hmccosmetics.cmd.default")) {
                MessagesUtil.sendMessage(sender, "no-permission");
                return true;
            }

            CosmeticUser user = CosmeticUsers.getUser(((Player) sender).getUniqueId());
            Menu menu = Menus.getDefaultMenu();

            if (user == null) {
                MessagesUtil.sendMessage(sender, "invalid-player");
                return true;
            }

            if (menu == null) {
                MessagesUtil.sendMessage(sender, "invalid-menu");
                return true;
            }

            menu.openMenu(user);
            return true;
        }
        Player player = sender instanceof Player ? (Player) sender : null;

        String firstArgs = args[0].toLowerCase();

        if (sender.hasPermission("HMCCosmetics.cmd.silent") || sender.isOp()) {
            for (String singleArg : args) {
                if (singleArg.equalsIgnoreCase("-s")) silent = true;
            }
        }

        switch (firstArgs) {
            case ("reload") -> {
                if (!sender.hasPermission("HMCCosmetics.cmd.reload") && !sender.isOp()) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }
                HMCCosmeticsPlugin.setup();
                if (!silent) MessagesUtil.sendMessage(sender, "reloaded");
                return true;
            }
            case ("apply") -> {
                if (!sender.hasPermission("hmccosmetics.cmd.apply")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }
                Cosmetic cosmetic;
                Color color = Color.WHITE;

                if (sender instanceof Player) player = ((Player) sender).getPlayer();
                if (sender.hasPermission("hmccosmetics.cmd.apply.other")) {
                    if (args.length >= 3) player = Bukkit.getPlayer(args[2]);
                }

                if (sender.hasPermission("hmccosmetics.cmd.apply.color")) {
                    if (args.length >= 4) color = ServerUtils.hex2Rgb(args[3]);
                }

                if (args.length == 1) {
                    if (!silent) MessagesUtil.sendMessage(player, "not-enough-args");
                    return true;
                }

                cosmetic = Cosmetics.getCosmetic(args[1]);

                if (cosmetic == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-cosmetic");
                    return true;
                }

                if (player == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-player");
                    return true;
                }

                CosmeticUser user = CosmeticUsers.getUser(player);

                if (!user.canEquipCosmetic(cosmetic)) {
                    if (!silent) MessagesUtil.sendMessage(player, "no-cosmetic-permission");
                    return true;
                }

                TagResolver placeholders =
                        TagResolver.resolver(Placeholder.parsed("cosmetic", cosmetic.getId()),
                                TagResolver.resolver(Placeholder.parsed("player", player.getName())),
                                TagResolver.resolver(Placeholder.parsed("cosmeticslot", cosmetic.getSlot().name())));

                if (!silent) MessagesUtil.sendMessage(player, "equip-cosmetic", placeholders);

                user.addPlayerCosmetic(cosmetic, color);
                user.updateCosmetic(cosmetic.getSlot());
                return true;
            }
            case ("unapply") -> {
                if (!sender.hasPermission("hmccosmetics.cmd.unapply")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }
                if (args.length == 1) {
                    if (!silent) MessagesUtil.sendMessage(player, "not-enough-args");
                    return true;
                }

                CosmeticSlot cosmeticSlot = null;

                if (sender instanceof Player) player = ((Player) sender).getPlayer();
                if (sender.hasPermission("hmccosmetics.cmd.unapply.other")) {
                    if (args.length >= 3) player = Bukkit.getPlayer(args[2]);
                }

                if (!EnumUtils.isValidEnum(CosmeticSlot.class, args[1].toUpperCase())) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-slot");
                    return true;
                }
                cosmeticSlot = CosmeticSlot.valueOf(args[1].toUpperCase());

                if (player == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-player");
                    return true;
                }

                CosmeticUser user = CosmeticUsers.getUser(player);

                if (user.getCosmetic(cosmeticSlot) == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-cosmetic-slot");
                    return true;
                }

                TagResolver placeholders =
                        TagResolver.resolver(Placeholder.parsed("cosmetic", user.getCosmetic(cosmeticSlot).getId()),
                                TagResolver.resolver(Placeholder.parsed("player", player.getName())),
                                TagResolver.resolver(Placeholder.parsed("cosmeticslot", cosmeticSlot.name())));

                if (!silent) MessagesUtil.sendMessage(player, "unequip-cosmetic", placeholders);

                user.removeCosmeticSlot(cosmeticSlot);
                user.updateCosmetic(cosmeticSlot);
                return true;
            }
            case ("wardrobe") -> {
                if (sender instanceof Player) player = ((Player) sender).getPlayer();
                if (sender.hasPermission("hmccosmetics.cmd.wardrobe.other")) {
                    if (args.length >= 2) player = Bukkit.getPlayer(args[1]);
                }

                if (!sender.hasPermission("hmccosmetics.cmd.wardrobe")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }

                if (player == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-player");
                    return true;
                }

                CosmeticUser user = CosmeticUsers.getUser(player);

                user.toggleWardrobe();
                return true;
            }
            // cosmetic menu exampleMenu playerName
            case ("menu") -> {
                if (args.length == 1) return true;
                if (!sender.hasPermission("hmccosmetics.cmd.menu")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }
                Menu menu = Menus.getMenu(args[1]);

                if (sender instanceof Player) player = ((Player) sender).getPlayer();
                if (sender.hasPermission("hmccosmetics.cmd.menu.other")) {
                    if (args.length >= 3) player = Bukkit.getPlayer(args[2]);
                }
                CosmeticUser user = CosmeticUsers.getUser(player);

                if (user == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-player");
                    return true;
                }

                if (menu == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-menu");
                    return true;
                }

                menu.openMenu(user);
                return true;
            }
            case ("dataclear") -> {
                if (args.length == 1) return true;
                OfflinePlayer selectedPlayer = Bukkit.getOfflinePlayer(args[1]);
                if (selectedPlayer == null) return true;
                if (!sender.hasPermission("hmccosmetics.cmd.dataclear") && !sender.isOp()) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }
                Database.clearData(selectedPlayer.getUniqueId());
                sender.sendMessage("Cleared data for " + selectedPlayer.getName());
                return true;
            }
            case ("dye") -> {
                if (player == null) return true;
                CosmeticUser user = CosmeticUsers.getUser(player);
                if (user == null) return true;
                if (!sender.hasPermission("hmccosmetics.cmd.dye") && !sender.isOp()) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }

                if (args.length == 1) {
                    if (!silent) MessagesUtil.sendMessage(player, "not-enough-args");
                    return true;
                }

                if (!EnumUtils.isValidEnum(CosmeticSlot.class, args[1])) {
                    if (!silent) MessagesUtil.sendMessage(player, "invalid-slot");
                    return true;
                }
                CosmeticSlot slot = CosmeticSlot.valueOf(args[1]);
                Cosmetic cosmetic = user.getCosmetic(slot);

                if (args.length >= 3) {
                    if (args[2].isEmpty()) {
                        if (!silent) MessagesUtil.sendMessage(player, "invalid-color");
                        return true;
                    }
                    Color color = ServerUtils.hex2Rgb(args[2]);
                    if (color == null) {
                        if (!silent) MessagesUtil.sendMessage(player, "invalid-color");
                        return true;
                    }
                    user.addPlayerCosmetic(cosmetic, color); // #FFFFFF
                } else {
                    DyeMenu.openMenu(user, cosmetic);
                }
            }
            case ("setlocation") -> {
                if (!sender.hasPermission("hmccosmetics.cmd.setlocation")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }

                if (player == null) return true;

                if (args.length < 2) {
                    if (!silent) MessagesUtil.sendMessage(player, "not-enough-args");
                    return true;
                }

                if (args[1].equalsIgnoreCase("wardrobelocation")) {
                    WardrobeSettings.setWardrobeLocation(player.getLocation());
                    if (!silent) MessagesUtil.sendMessage(player, "set-wardrobe-location");
                    return true;
                }

                if (args[1].equalsIgnoreCase("viewerlocation")) {
                    WardrobeSettings.setViewerLocation(player.getLocation());
                    if (!silent) MessagesUtil.sendMessage(player, "set-wardrobe-viewing");
                    return true;
                }

                if (args[1].equalsIgnoreCase("leavelocation")) {
                    WardrobeSettings.setLeaveLocation(player.getLocation());
                    if (!silent) MessagesUtil.sendMessage(player, "set-wardrobe-leaving");
                    return true;
                }
            }
            case ("dump") -> {
                if (player == null) return true;
                CosmeticUser user = CosmeticUsers.getUser(player);
                if (user == null) return true;
                if (!sender.hasPermission("HMCCosmetic.cmd.dump") && !sender.isOp()) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }
                player.sendMessage("Passengers -> " + player.getPassengers());
                if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
                    player.sendMessage("Backpack Location -> " + user.getUserBackpackManager().getArmorstand().getLocation());
                }
                player.sendMessage("Cosmetics -> " + user.getCosmetic());
                player.sendMessage("EntityId -> " + player.getEntityId());
                return true;
            }
            case ("hide") -> {
                if (sender instanceof Player) player = ((Player) sender).getPlayer();
                if (sender.hasPermission("hmccosmetics.cmd.hide.other")) {
                    if (args.length >= 2) player = Bukkit.getPlayer(args[1]);
                }

                if (!sender.hasPermission("hmccosmetics.cmd.hide")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }

                if (player == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-player");
                    return true;
                }

                CosmeticUser user = CosmeticUsers.getUser(player);
                if (!silent) MessagesUtil.sendMessage(sender, "hide-cosmetic");
                user.hideCosmetics(CosmeticUser.HiddenReason.COMMAND);
                return true;
            }
            case ("show") -> {
                if (sender instanceof Player) player = ((Player) sender).getPlayer();
                if (sender.hasPermission("hmccosmetics.cmd.show.other")) {
                    if (args.length >= 2) player = Bukkit.getPlayer(args[1]);
                }

                if (!sender.hasPermission("hmccosmetics.cmd.show")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }

                if (player == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-player");
                    return true;
                }

                CosmeticUser user = CosmeticUsers.getUser(player);

                if (!silent) MessagesUtil.sendMessage(sender, "show-cosmetic");
                user.showCosmetics();
                return true;
            }
            case ("debug") -> {
                if (!sender.hasPermission("hmccosmetics.cmd.debug")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }

                if (Settings.getDebugMode()) {
                    Settings.setDebugMode(false);
                    if (!silent) MessagesUtil.sendMessage(sender, "debug-disabled");
                } else {
                    Settings.setDebugMode(true);
                    if (!silent) MessagesUtil.sendMessage(sender, "debug-enabled");
                }
            }
            case ("emote") -> {
                if (!sender.hasPermission("hmccosmetics.cmd.emote")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }
                if (sender.hasPermission("hmccosmetics.cmd.emote.other")) {
                    if (args.length >= 2) player = Bukkit.getPlayer(args[1]);
                }
                if (player == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-player");
                    return true;
                }
                CosmeticUser user = CosmeticUsers.getUser(player);
                if (!user.hasCosmeticInSlot(CosmeticSlot.EMOTE)) {
                    if (!silent) MessagesUtil.sendMessage(sender, "emote-none");
                    return true;
                }

                CosmeticEmoteType cosmeticEmoteType = (CosmeticEmoteType) user.getCosmetic(CosmeticSlot.EMOTE);
                cosmeticEmoteType.run(user);
                return true;
            }
        }
        return true;
    }
}
