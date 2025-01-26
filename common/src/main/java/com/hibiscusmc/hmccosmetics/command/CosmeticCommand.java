package com.hibiscusmc.hmccosmetics.command;

import com.hibiscusmc.hmccolor.HMCColorConfig;
import com.hibiscusmc.hmccolor.HMCColorContextKt;
import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.config.Wardrobe;
import com.hibiscusmc.hmccosmetics.config.WardrobeLocation;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticEmoteType;
import com.hibiscusmc.hmccosmetics.database.Database;
import com.hibiscusmc.hmccosmetics.emotes.EmoteManager;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.gui.special.DyeMenu;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.HMCCServerUtils;
import me.lojosho.hibiscuscommons.hooks.Hooks;
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

import java.util.Set;

public class CosmeticCommand implements CommandExecutor {

    // cosmetics apply cosmetics playerName
    //             0      1        2

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        boolean silent = false;
        boolean console = false;

        if (!(sender instanceof Player)) {
            console = true;
        }

        if (args.length == 0) {
            if (console) {
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
                if (singleArg.equalsIgnoreCase("-s")) {
                    silent = true;
                    break;
                }
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
                Color color = null;

                if (sender instanceof Player) player = ((Player) sender).getPlayer();
                if (sender.hasPermission("hmccosmetics.cmd.apply.other")) {
                    if (args.length >= 3) player = Bukkit.getPlayer(args[2]);
                }

                if (sender.hasPermission("hmccosmetics.cmd.apply.color")) {
                    if (args.length >= 4) {
                        // TODO: Add sub-color support somehow... (and make this neater)
                        String textColor = args[3];
                        if (!textColor.contains("#") && Hooks.isActiveHook("HMCColor")) {
                            HMCColorConfig.Colors colors = HMCColorContextKt.getHmcColor().getConfig().getColors().get(textColor);
                            if (colors != null) {
                                color = colors.getBaseColor().getColor();
                            }
                        } else {
                            color = HMCCServerUtils.hex2Rgb(textColor);
                        }
                    }
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

                if (!user.canEquipCosmetic(cosmetic) && !console) {
                    if (!silent) MessagesUtil.sendMessage(player, "no-cosmetic-permission");
                    return true;
                }

                TagResolver placeholders =
                        TagResolver.resolver(Placeholder.parsed("cosmetic", cosmetic.getId()),
                                TagResolver.resolver(Placeholder.parsed("player", player.getName())),
                                TagResolver.resolver(Placeholder.parsed("cosmeticslot", cosmetic.getSlot().toString())));

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

                if (sender instanceof Player) player = ((Player) sender).getPlayer();
                if (sender.hasPermission("hmccosmetics.cmd.unapply.other")) {
                    if (args.length >= 3) player = Bukkit.getPlayer(args[2]);
                }

                if (player == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-player");
                    return true;
                }

                CosmeticUser user = CosmeticUsers.getUser(player);

                Set<CosmeticSlot> cosmeticSlots;

                if (args[1].equalsIgnoreCase("all")) {
                    cosmeticSlots = user.getSlotsWithCosmetics();
                } else {
                    String rawSlot = args[1].toUpperCase();
                    if (!CosmeticSlot.contains(rawSlot)) {
                        if (!silent) MessagesUtil.sendMessage(sender, "invalid-slot");
                        return true;
                    }
                    cosmeticSlots = Set.of(CosmeticSlot.valueOf(rawSlot));
                }

                for (CosmeticSlot cosmeticSlot : cosmeticSlots) {
                    if (user.getCosmetic(cosmeticSlot) == null) {
                        if (!silent) MessagesUtil.sendMessage(sender, "no-cosmetic-slot");
                        continue;
                    }

                    TagResolver placeholders =
                            TagResolver.resolver(Placeholder.parsed("cosmetic", user.getCosmetic(cosmeticSlot).getId()),
                                    TagResolver.resolver(Placeholder.parsed("player", player.getName())),
                                    TagResolver.resolver(Placeholder.parsed("cosmeticslot", cosmeticSlot.toString())));

                    if (!silent) MessagesUtil.sendMessage(player, "unequip-cosmetic", placeholders);

                    user.removeCosmeticSlot(cosmeticSlot);
                    user.updateCosmetic(cosmeticSlot);
                }
                return true;
            }
            case ("wardrobe") -> {
                if (sender instanceof Player) player = ((Player) sender).getPlayer();

                if (args.length == 1) {
                    if (!silent) MessagesUtil.sendMessage(player, "not-enough-args");
                    return true;
                }

                if (sender.hasPermission("hmccosmetics.cmd.wardrobe.other")) {
                    if (args.length >= 3) player = Bukkit.getPlayer(args[2]);
                }

                if (!sender.hasPermission("hmccosmetics.cmd.wardrobe")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }

                if (player == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-player");
                    return true;
                }

                if (!WardrobeSettings.getWardrobeNames().contains(args[1])) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-wardrobes");
                    return true;
                }
                Wardrobe wardrobe = WardrobeSettings.getWardrobe(args[1]);

                CosmeticUser user = CosmeticUsers.getUser(player);

                if (user.isInWardrobe()) {
                    user.leaveWardrobe(false);
                } else {
                    user.enterWardrobe(wardrobe, false);
                }
                return true;
            }
            // cosmetic menu exampleMenu playerName
            case ("menu") -> {
                if (!sender.hasPermission("hmccosmetics.cmd.menu")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }
                Menu menu;
                if (args.length == 1) {
                    menu = Menus.getDefaultMenu();
                } else {
                    menu = Menus.getMenu(args[1]);
                }

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

                String rawSlot = args[1];
                if (!CosmeticSlot.contains(rawSlot)) {
                    if (!silent) MessagesUtil.sendMessage(player, "invalid-slot");
                    return true;
                }
                CosmeticSlot slot = CosmeticSlot.valueOf(rawSlot);
                Cosmetic cosmetic = user.getCosmetic(slot);

                if (args.length >= 3) {
                    if (args[2].isEmpty()) {
                        if (!silent) MessagesUtil.sendMessage(player, "invalid-color");
                        return true;
                    }
                    Color color = HMCCServerUtils.hex2Rgb(args[2]);
                    if (color == null) {
                        if (!silent) MessagesUtil.sendMessage(player, "invalid-color");
                        return true;
                    }
                    user.addPlayerCosmetic(cosmetic, color); // #FFFFFF
                } else {
                    DyeMenu.openMenu(user, cosmetic);
                }
            }
            case ("setwardrobesetting") -> {
                if (!sender.hasPermission("hmccosmetics.cmd.setwardrobesetting")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }

                if (player == null) return true;

                if (args.length < 3) {
                    if (!silent) MessagesUtil.sendMessage(player, "not-enough-args");
                    return true;
                }
                Wardrobe wardrobe = WardrobeSettings.getWardrobe(args[1]);
                if (wardrobe == null) {
                    wardrobe = new Wardrobe(args[1], new WardrobeLocation(null, null, null), null, -1, null);
                    WardrobeSettings.addWardrobe(wardrobe);
                    //MessagesUtil.sendMessage(player, "no-wardrobes");
                    //return true;
                }

                if (args[2].equalsIgnoreCase("npclocation")) {
                    WardrobeSettings.setNPCLocation(wardrobe, player.getLocation());
                    if (!silent) MessagesUtil.sendMessage(player, "set-wardrobe-location");
                    return true;
                }

                if (args[2].equalsIgnoreCase("viewerlocation")) {
                    WardrobeSettings.setViewerLocation(wardrobe, player.getEyeLocation());
                    if (!silent) MessagesUtil.sendMessage(player, "set-wardrobe-viewing");
                    return true;
                }

                if (args[2].equalsIgnoreCase("leavelocation")) {
                    WardrobeSettings.setLeaveLocation(wardrobe, player.getLocation());
                    if (!silent) MessagesUtil.sendMessage(player, "set-wardrobe-leaving");
                    return true;
                }

                if (args.length >= 4) {
                    if (args[2].equalsIgnoreCase("permission")) {
                        WardrobeSettings.setWardrobePermission(wardrobe, args[3]);
                        if (!silent) MessagesUtil.sendMessage(player, "set-wardrobe-permission");
                        return true;
                    }
                    if (args[2].equalsIgnoreCase("distance")) {
                        WardrobeSettings.setWardrobeDistance(wardrobe, Integer.parseInt(args[3]));
                        if (!silent) MessagesUtil.sendMessage(player, "set-wardrobe-distance");
                        return true;
                    }
                    if (args[2].equalsIgnoreCase("defaultmenu")) {
                        WardrobeSettings.setWardrobeDefaultMenu(wardrobe, args[3]);
                        if (!silent) MessagesUtil.sendMessage(player, "set-wardrobe-menu");
                        return true;
                    }
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
                    player.sendMessage("Backpack Location -> " + user.getUserBackpackManager().getEntityManager().getLocation());
                }
                player.sendMessage("Cosmetics -> " + user.getCosmetics());
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
                user.showCosmetics(CosmeticUser.HiddenReason.COMMAND);
                return true;
            }
            case ("debug") -> {
                if (!sender.hasPermission("hmccosmetics.cmd.debug")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }

                if (Settings.isDebugMode()) {
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

            case ("playemote") -> {
                // /cosmetic playEmote <emoteId> [playerName]
                if (!sender.hasPermission("hmccosmetics.cmd.playemote")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }

                if (args.length < 2) {
                    if (!silent) MessagesUtil.sendMessage(player, "not-enough-args");
                    return true;
                }

                if (!EmoteManager.has(args[1])) {
                    MessagesUtil.sendDebugMessages("Did not contain " + args[1]);
                    if (!silent) MessagesUtil.sendMessage(sender, "emote-invalid");
                    return true;
                }

                if (sender.hasPermission("hmccosmetics.cmd.playemote.other")) {
                    if (args.length >= 3) player = Bukkit.getPlayer(args[2]);
                }
                if (player == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-player");
                    return true;
                }
                CosmeticUser user = CosmeticUsers.getUser(player);
                user.getUserEmoteManager().playEmote(args[1]);
                return true;
            }

            case "disableall" -> {
                if (!sender.hasPermission("hmccosmetics.cmd.disableall")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }
                if (args.length == 1) {
                    if (!silent) MessagesUtil.sendMessage(player, "not-enough-args");
                    return true;
                }
                if (args[1].equalsIgnoreCase("true")) {
                    Settings.setAllPlayersHidden(true);
                    for (CosmeticUser user : CosmeticUsers.values()) user.hideCosmetics(CosmeticUser.HiddenReason.DISABLED);
                    if (!silent) MessagesUtil.sendMessage(sender, "disabled-all");
                } else if (args[1].equalsIgnoreCase("false")) {
                    Settings.setAllPlayersHidden(false);
                    for (CosmeticUser user : CosmeticUsers.values()) user.showCosmetics(CosmeticUser.HiddenReason.DISABLED);
                    if (!silent) MessagesUtil.sendMessage(sender, "enabled-all");
                } else {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-args");
                }
                return true;
            }

            case "hiddenreasons" -> {
                if (!sender.hasPermission("hmccosmetics.cmd.hiddenreasons")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }
                if (args.length >= 2) {
                    player = Bukkit.getPlayer(args[1]);
                }
                if (player == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-player");
                    return true;
                }
                CosmeticUser user = CosmeticUsers.getUser(player);
                sender.sendMessage(user.getHiddenReasons().toString());
                return true;
            }

            case "clearhiddenreasons" -> {
                if (!sender.hasPermission("hmccosmetics.cmd.clearhiddenreasons")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }
                if (args.length >= 2) {
                    player = Bukkit.getPlayer(args[1]);
                }
                if (player == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-player");
                    return true;
                }
                CosmeticUser user = CosmeticUsers.getUser(player);
                user.clearHiddenReasons();
                return true;
            }
        }
        return true;
    }
}
