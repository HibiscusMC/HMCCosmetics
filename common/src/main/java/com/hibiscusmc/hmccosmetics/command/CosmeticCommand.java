package com.hibiscusmc.hmccosmetics.command;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
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
import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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
            if (user == null || menu == null) {
                sender.sendMessage("Invalid Menu");
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
            sender.sendMessage("Reloaded.");
            return true;
        }
        else if (args[0].equalsIgnoreCase("apply")) {
            sender.sendMessage("Applying - Begin");
            Player player = null;
            Cosmetic cosmetic;

            if (sender instanceof Player) player = ((Player) sender).getPlayer();
            if (args.length >= 3) player = Bukkit.getPlayer(args[2]);

            cosmetic = Cosmetics.getCosmetic(args[1]);

            if (player == null || cosmetic == null) {
                sender.sendMessage("Something was null");
                return true;
            }

            CosmeticUser user = CosmeticUsers.getUser(player);

            user.addPlayerCosmetic(cosmetic);
            user.updateCosmetic(cosmetic.getSlot());
            sender.sendMessage("Applying - Finish with  " + cosmetic.getId());
            return true;
        }
        else if (args[0].equalsIgnoreCase("unapply")) {
            Player player = null;
            CosmeticSlot cosmeticSlot;

            if (sender instanceof Player) player = ((Player) sender).getPlayer();
            if (args.length >= 3) player = Bukkit.getPlayer(args[2]);

            cosmeticSlot = CosmeticSlot.valueOf(args[1]);

            if (player == null || cosmeticSlot == null) {
                sender.sendMessage("Something was null");
                return true;
            }

            CosmeticUser user = CosmeticUsers.getUser(player);

            user.removeCosmeticSlot(cosmeticSlot);
            user.updateCosmetic(cosmeticSlot);
            return true;
        }
        else if (args[0].equalsIgnoreCase("wardrobe")) {
            Player player = null;

            if (sender instanceof Player) player = ((Player) sender).getPlayer();
            if (args.length >= 3) player = Bukkit.getPlayer(args[2]);

            if (player == null) {
                sender.sendMessage("Player was null");
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

            if (player == null || menu == null) {
                sender.sendMessage("Something was null");
                return true;
            }

            menu.openMenu(CosmeticUsers.getUser(player));
            return true;
        }

        else if (args[0].equalsIgnoreCase("dataclear")) {
            if (args.length == 1) return true;
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            if (player == null) return true;
            Database.clearData(player.getUniqueId());
            sender.sendMessage("Cleared data for " + player.getName());
            return true;
        }

        else if (args[0].equalsIgnoreCase("dye") && args.length == 2) {
            Player player = sender instanceof Player ? (Player) sender : null;
            if (player == null) return true;
            CosmeticUser user = CosmeticUsers.getUser(player);
            if (user == null) return true;
            DyeMenu.openMenu(user, user.getCosmetic(CosmeticSlot.valueOf(args[1])));
        }

        else if (args[0].equalsIgnoreCase("dump") && args.length == 1) {
            Player player = sender instanceof Player ? (Player) sender : null;
            if (player == null) return true;
            CosmeticUser user = CosmeticUsers.getUser(player);
            if (user == null) return true;
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
