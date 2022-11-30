package com.hibiscusmc.hmccosmetics.command;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.database.Database;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
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
            // Possble default menu here?
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            HMCCosmeticsPlugin.setup();
            sender.sendMessage("Reloaded.");
            return true;
        }
        if (args[0].equalsIgnoreCase("apply")) {
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
            return true;
        }
        if (args[0].equalsIgnoreCase("unapply")) {
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
        if (args[0].equalsIgnoreCase("wardrobe")) {
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
        if (args[0].equalsIgnoreCase("menu")) {
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

        if (args[0].equalsIgnoreCase("dataclear")) {
            if (args.length == 1) return true;
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            if (player == null) return true;
            Database.clearData(player.getUniqueId());
            sender.sendMessage("Cleared data for " + player.getName());
        }
        return true;
    }
}
