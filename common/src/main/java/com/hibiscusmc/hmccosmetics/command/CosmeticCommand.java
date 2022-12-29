package com.hibiscusmc.hmccosmetics.command;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.database.Database;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.gui.special.DyeMenu;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
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
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                // Console
                return true;
            }
            CosmeticUser user = CosmeticUsers.getUser(((Player) sender).getUniqueId());
            Menu menu = Menus.getMenu(Settings.getDefaultMenu());

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
        else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("HMCCosmetics.reload") || !sender.isOp()) {
                MessagesUtil.sendMessage(sender, "no-permission");
                return true;
            }
            HMCCosmeticsPlugin.setup();
            MessagesUtil.sendMessage(sender, "reloaded");
            return true;
        }
        else if (args[0].equalsIgnoreCase("apply")) {
            Player player = null;
            Cosmetic cosmetic;

            if (sender instanceof Player) player = ((Player) sender).getPlayer();
            if (args.length >= 3) player = Bukkit.getPlayer(args[2]);

            cosmetic = Cosmetics.getCosmetic(args[1]);

            if (cosmetic == null) {
                MessagesUtil.sendMessage(sender, "invalid-cosmetic");
                return true;
            }

            if (player == null) {
                MessagesUtil.sendMessage(sender, "invalid-player");
                return true;
            }

            CosmeticUser user = CosmeticUsers.getUser(player);

            if (!user.canEquipCosmetic(cosmetic)) {
                MessagesUtil.sendMessage(player, "no-cosmetic-permission");
                return true;
            }

            TagResolver placeholders =
                    TagResolver.resolver(Placeholder.parsed("cosmetic", cosmetic.getId()),
                            TagResolver.resolver(Placeholder.parsed("player", player.getName())),
                            TagResolver.resolver(Placeholder.parsed("cosmeticslot", cosmetic.getSlot().name())));

            MessagesUtil.sendMessage(player, "equip-cosmetic", placeholders);

            user.addPlayerCosmetic(cosmetic);
            user.updateCosmetic(cosmetic.getSlot());
            return true;
        }
        else if (args[0].equalsIgnoreCase("unapply")) {
            Player player = null;
            CosmeticSlot cosmeticSlot;

            if (sender instanceof Player) player = ((Player) sender).getPlayer();
            if (args.length >= 3) player = Bukkit.getPlayer(args[2]);

            cosmeticSlot = CosmeticSlot.valueOf(args[1]);

            if (cosmeticSlot == null) {
                MessagesUtil.sendMessage(sender, "invalid-slot");
                return true;
            }

            if (player == null) {
                MessagesUtil.sendMessage(sender, "invalid-player");
                return true;
            }

            CosmeticUser user = CosmeticUsers.getUser(player);

            if (user.getCosmetic(cosmeticSlot) == null) {
                MessagesUtil.sendMessage(sender, "no-cosmetic-slot");
                return true;
            }

            TagResolver placeholders =
                    TagResolver.resolver(Placeholder.parsed("cosmetic", user.getCosmetic(cosmeticSlot).getId()),
                    TagResolver.resolver(Placeholder.parsed("player", player.getName())),
                    TagResolver.resolver(Placeholder.parsed("cosmeticslot", cosmeticSlot.name())));

            MessagesUtil.sendMessage(player, "unequip-cosmetic", placeholders);

            user.removeCosmeticSlot(cosmeticSlot);
            user.updateCosmetic(cosmeticSlot);
            return true;
        }
        else if (args[0].equalsIgnoreCase("wardrobe")) {
            Player player = null;

            if (sender instanceof Player) player = ((Player) sender).getPlayer();
            if (args.length >= 3) player = Bukkit.getPlayer(args[2]);

            if (!player.hasPermission("HMCCosmetic.wardrobe")) {
                MessagesUtil.sendMessage(sender, "no-permission");
                return true;
            }

            if (player == null) {
                MessagesUtil.sendMessage(sender, "invalid-player");
                return true;
            }

            CosmeticUser user = CosmeticUsers.getUser(player);

            user.toggleWardrobe();
            return true;
        }
        // cosmetic menu exampleMenu playerName
        else if (args[0].equalsIgnoreCase("menu")) {
            if (args.length == 1) return true;
            Menu menu = Menus.getMenu(args[1]);
            Player player = null;

            if (sender instanceof Player) player = ((Player) sender).getPlayer();
            if (args.length >= 3) player = Bukkit.getPlayer(args[2]);
            CosmeticUser user = CosmeticUsers.getUser(player);

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

        else if (args[0].equalsIgnoreCase("dataclear")) {
            if (args.length == 1) return true;
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            if (player == null) return true;
            if (!sender.hasPermission("HMCCosmetic.dataclear") && !sender.isOp()) {
                MessagesUtil.sendMessage(sender, "no-permission");
                return true;
            }
            Database.clearData(player.getUniqueId());
            sender.sendMessage("Cleared data for " + player.getName());
            return true;
        }

        else if (args[0].equalsIgnoreCase("dye") && args.length == 2) {
            Player player = sender instanceof Player ? (Player) sender : null;
            if (player == null) return true;
            CosmeticUser user = CosmeticUsers.getUser(player);
            if (user == null) return true;
            if (!sender.hasPermission("HMCCosmetic.dye") && !sender.isOp()) {
                MessagesUtil.sendMessage(sender, "no-permission");
                return true;
            }
            DyeMenu.openMenu(user, user.getCosmetic(CosmeticSlot.valueOf(args[1])));
        }

        else if (args[0].equalsIgnoreCase("setlocation")) {

            Player player = sender instanceof Player ? (Player) sender : null;
            if (player == null) return true;

            if (args.length < 2) {
                MessagesUtil.sendMessage(player, "not-enough-args");
                return true;
            }

            if (args[1].equalsIgnoreCase("wardrobelocation")) {
                WardrobeSettings.setWardrobeLocation(player.getLocation());
                MessagesUtil.sendMessage(player, "set-wardrobe-location");
                return true;
            }

            if (args[1].equalsIgnoreCase("viewerlocation")) {
                WardrobeSettings.setViewerLocation(player.getLocation());
                MessagesUtil.sendMessage(player, "set-wardrobe-viewing");
                return true;
            }

            if (args[1].equalsIgnoreCase("leavelocation")) {
                WardrobeSettings.setLeaveLocation(player.getLocation());
                MessagesUtil.sendMessage(player, "set-wardrobe-leaving");
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("dump") && args.length == 1) {
            Player player = sender instanceof Player ? (Player) sender : null;
            if (player == null) return true;
            CosmeticUser user = CosmeticUsers.getUser(player);
            if (user == null) return true;
            if (!sender.hasPermission("HMCCosmetic.dump") && !sender.isOp()) {
                MessagesUtil.sendMessage(sender, "no-permission");
                return true;
            }
            player.sendMessage("Passengers -> " + player.getPassengers());
            if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
                player.sendMessage("Backpack Location -> " + user.getBackpackEntity().getLocation());
            }
            player.sendMessage("Cosmetics -> " + user.getCosmetic());
            player.sendMessage("EntityId -> " + player.getEntityId());
            return true;
        }
        return true;
    }
}
