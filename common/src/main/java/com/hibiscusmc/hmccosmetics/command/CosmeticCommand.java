package com.hibiscusmc.hmccosmetics.command;

import com.hibiscusmc.hmccolor.HMCColorConfig;
import com.hibiscusmc.hmccolor.HMCColorContextKt;
import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.config.section.Wardrobe;
import com.hibiscusmc.hmccosmetics.config.section.WardrobeLocation;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.database.Database;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.gui.special.DyeMenuProvider;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.BalloonStressTest;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.HMCCServerUtils;
import me.lojosho.hibiscuscommons.HibiscusCommonsPlugin;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

                if (user == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-player");
                    return true;
                }

                if (!user.canEquipCosmetic(cosmetic) && !console) {
                    if (!silent) MessagesUtil.sendMessage(player, "no-cosmetic-permission");
                    return true;
                }

                final ItemStack cosmeticItem = cosmetic.getItem();
                Component itemName = Component.text(cosmetic.getId());
                if (HibiscusCommonsPlugin.isOnPaper() && cosmeticItem != null) {
                    itemName = cosmeticItem.effectiveName();
                }

                TagResolver placeholders =
                        TagResolver.resolver(Placeholder.parsed("cosmetic", cosmetic.getId()),
                                TagResolver.resolver(Placeholder.parsed("player", player.getName())),
                                TagResolver.resolver(Placeholder.parsed("cosmeticslot", cosmetic.getSlot().toString())),
                                TagResolver.resolver(Placeholder.component("cosmetic_item_name", itemName))
                        );

                if (!silent) MessagesUtil.sendMessage(player, "equip-cosmetic", placeholders);

                user.addCosmetic(cosmetic, color);
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
                    final Cosmetic cosmetic = user.getCosmetic(cosmeticSlot);
                    if (cosmetic == null) {
                        if (!silent) MessagesUtil.sendMessage(sender, "no-cosmetic-slot");
                        continue;
                    }

                    final ItemStack cosmeticItem = cosmetic.getItem();
                    Component itemName = Component.text(cosmetic.getId());
                    if (HibiscusCommonsPlugin.isOnPaper() && cosmeticItem != null) {
                        itemName = cosmeticItem.effectiveName();
                    }

                    TagResolver placeholders =
                            TagResolver.resolver(Placeholder.parsed("cosmetic", cosmetic.getId()),
                                    TagResolver.resolver(Placeholder.parsed("player", player.getName())),
                                    TagResolver.resolver(Placeholder.parsed("cosmeticslot", cosmeticSlot.toString())),
                                    TagResolver.resolver(Placeholder.component("cosmetic_item_name", itemName)));

                    if (!silent) MessagesUtil.sendMessage(player, "unequip-cosmetic", placeholders);

                    user.removeCosmeticSlot(cosmeticSlot);
                    user.updateCosmetic(cosmeticSlot);
                }
                return true;
            }
            case ("wardrobes") -> {
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

                final String rawSlot = args[1];
                if (!CosmeticSlot.contains(rawSlot)) {
                    if (!silent) MessagesUtil.sendMessage(player, "invalid-slot");
                    return true;
                }
                final CosmeticSlot slot = CosmeticSlot.valueOf(rawSlot); // This is checked above. While IDEs may say the slot might be null, it will not be.
                final Cosmetic cosmetic = user.getCosmetic(slot);
                if (cosmetic == null) {
                    if (!silent) MessagesUtil.sendMessage(player, "invalid-slot");
                    return true;
                }

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
                    user.addCosmetic(cosmetic, color); // #FFFFFF
                } else {
                    if (DyeMenuProvider.canOpenDyeMenu()) {
                        DyeMenuProvider.openMenu(player, user, cosmetic);
                    } else {
                        if (!silent) MessagesUtil.sendMessage(player, "invalid-color");
                    }
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
                String wardrobeId = args[1];
                Wardrobe wardrobe = WardrobeSettings.getWardrobe(wardrobeId);
                if (wardrobe == null) {
                    wardrobe = new Wardrobe(wardrobeId, new WardrobeLocation(null, null, null), null, -1, null, WardrobeSettings.getWardrobeDefaultFile());
                    WardrobeSettings.addWardrobe(wardrobe);
                    //MessagesUtil.sendMessage(player, "no-wardrobes");
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
                if (user.getUserBackpackManager() != null) {
                    player.sendMessage("Backpack Location -> " + user.getUserBackpackManager().getEntityManager().getLocation());
                    player.sendMessage("Cosmetic Passengers -> " + user.getUserBackpackManager().getAreaEffectEntityId());
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
            case ("toggle") -> {
                if (sender instanceof Player) player = ((Player) sender).getPlayer();
                if (sender.hasPermission("hmccosmetics.cmd.toggle.other")) {
                    if (args.length >= 2) player = Bukkit.getPlayer(args[1]);
                }

                if (!sender.hasPermission("hmccosmetics.cmd.toggle")) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }

                if (player == null) {
                    if (!silent) MessagesUtil.sendMessage(sender, "invalid-player");
                    return true;
                }

                CosmeticUser user = CosmeticUsers.getUser(player);
                if (user.isHidden(CosmeticUser.HiddenReason.COMMAND)) {
                    if (!silent) MessagesUtil.sendMessage(sender, "show-cosmetic");
                    user.showCosmetics(CosmeticUser.HiddenReason.COMMAND);
                } else {
                    if (!silent) MessagesUtil.sendMessage(sender, "hide-cosmetic");
                    user.hideCosmetics(CosmeticUser.HiddenReason.COMMAND);
                }
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

            case "stresstest" -> {
                if (!sender.hasPermission("hmccosmetics.cmd.stresstest") && !sender.isOp()) {
                    if (!silent) MessagesUtil.sendMessage(sender, "no-permission");
                    return true;
                }
                if (args.length >= 2 && args[1].equalsIgnoreCase("stop")) {
                    BalloonStressTest.stop();
                    sender.sendMessage("Balloon stress test stopped.");
                    return true;
                }
                if (player == null) {
                    sender.sendMessage("Run /hmccosmetics stresstest as a player (it spawns balloons at your location).");
                    return true;
                }
                int count = 100;
                if (args.length >= 2) {
                    try {
                        count = Math.max(1, Math.min(2000, Integer.parseInt(args[1])));
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Usage: /hmccosmetics stresstest <count|stop>");
                        return true;
                    }
                }
                // Clone the balloon the caller has equipped so each stress entity renders the real
                // model (ModelEngine or item), not a placeholder. Falls back to a visible helmet if none.
                CosmeticUser stressUser = CosmeticUsers.getUser(player.getUniqueId());
                CosmeticBalloonType balloon = stressUser != null
                        && stressUser.getCosmetic(CosmeticSlot.BALLOON) instanceof CosmeticBalloonType b ? b : null;
                ItemStack fallback = player.getInventory().getItemInMainHand();
                if (fallback.getType().isAir()) fallback = new ItemStack(org.bukkit.Material.CARVED_PUMPKIN);
                BalloonStressTest.start(HMCCosmeticsPlugin.getInstance(), player.getLocation(), count, balloon, fallback, sender);
                sender.sendMessage("Spawned " + count + " stress balloons"
                        + (balloon != null ? " of your equipped cosmetic" : " (no balloon equipped, using fallback item)")
                        + " (period=" + Math.max(1, Settings.getBalloonLerpPeriod())
                        + "). Timing prints every ~5s. Run /hmccosmetics stresstest stop to end.");
                return true;
            }
        }
        return true;
    }
}
