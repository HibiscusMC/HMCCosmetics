package com.hibiscusmc.hmccosmetics.command;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.ticxo.playeranimator.api.PlayerAnimator;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CosmeticCommandTabComplete implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> finalCompletitons = new ArrayList<>();

        if (args.length == 1) {
            if (hasPermission(sender, "hmccosmetics.cmd.apply")) completions.add("apply");
            if (hasPermission(sender, "hmccosmetics.cmd.unapply")) completions.add("unapply");
            if (hasPermission(sender, "hmccosmetics.cmd.menu")) completions.add("menu");
            if (hasPermission(sender, "hmccosmetics.cmd.reload")) completions.add("reload");
            if (hasPermission(sender, "hmccosmetics.cmd.wardrobe")) completions.add("wardrobe");
            if (hasPermission(sender, "hmccosmetics.cmd.dataclear")) completions.add("dataclear");
            if (hasPermission(sender, "hmccosmetics.cmd.dye")) completions.add("dye");
            if (hasPermission(sender, "hmccosmetics.cmd.setlocation")) completions.add("setlocation");
            if (hasPermission(sender, "hmccosmetics.cmd.hide")) completions.add("hide");
            if (hasPermission(sender, "hmccosmetics.cmd.show")) completions.add("show");
            if (hasPermission(sender, "hmccosmetics.cmd.debug")) completions.add("debug");
            if (hasPermission(sender, "hmccosmetics.cmd.emote")) completions.add("emote");

            StringUtil.copyPartialMatches(args[0], completions, finalCompletitons);
        }

        if (!(sender instanceof Player)) return completions;
        CosmeticUser user = CosmeticUsers.getUser(((Player) sender).getUniqueId());

        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "apply" -> {
                    completions.addAll(applyCommandComplete(user, args));
                }
                case "unapply" -> {
                    for (Cosmetic cosmetic : user.getCosmetic()) {
                        completions.add(cosmetic.getSlot().toString().toUpperCase());
                    }
                }
                case "menu" -> {
                    for (Menu menu : Menus.getMenu()) {
                        if (menu.canOpen(user.getPlayer())) completions.add(menu.getId());
                    }
                }
                case "dataclear", "wardrobe", "hide", "show", "emote" -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        completions.add(player.getName());
                    }
                }
                case "dye" -> {
                    for (CosmeticSlot slot : user.getDyeableSlots()) {
                        completions.add(slot.name());
                    }
                }
                case "setlocation" -> {
                    completions.add("wardrobelocation");
                    completions.add("viewerlocation");
                    completions.add("leavelocation");
                }
            }
            StringUtil.copyPartialMatches(args[1], completions, finalCompletitons);
        }
        if (args.length == 3) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "dye" -> {
                    completions.add("#FFFFFF");
                }
                case "menu", "apply", "unapply" -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        completions.add(player.getName());
                    }
                }
            }
            StringUtil.copyPartialMatches(args[2], completions, finalCompletitons);
        }

        if (args.length == 4) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "apply" -> {
                    completions.add("#FFFFFF");
                }
            }
            StringUtil.copyPartialMatches(args[3], completions, finalCompletitons);
        }

        Collections.sort(finalCompletitons);
        return finalCompletitons;
    }

    private static List<String> applyCommandComplete(CosmeticUser user, String[] args) {
        List<String> completitions = new ArrayList<>();

        if (args.length == 2) {
            for (Cosmetic cosmetic : Cosmetics.values()) {
                if (!user.canEquipCosmetic(cosmetic)) continue;
                completitions.add(cosmetic.getId());
            }
            //completitions.addAll(Cosmetics.keys());
        } else {
            if (args.length == 3) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completitions.add(player.getName());
                }
            }
        }
        return completitions;
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if (sender.isOp()) return true;
        if (sender.hasPermission(permission)) return true;
        return false;
    }
}
