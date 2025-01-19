package com.hibiscusmc.hmccosmetics.command;

import com.hibiscusmc.hmccolor.HMCColorContextKt;
import com.hibiscusmc.hmccosmetics.config.Wardrobe;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.emotes.EmoteManager;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import me.lojosho.hibiscuscommons.hooks.Hooks;
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
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();
        List<String> finalCompletions = new ArrayList<>();

        if (args.length == 1) {
            if (hasPermission(sender, "hmccosmetics.cmd.apply")) completions.add("apply");
            if (hasPermission(sender, "hmccosmetics.cmd.unapply")) completions.add("unapply");
            if (hasPermission(sender, "hmccosmetics.cmd.menu")) completions.add("menu");
            if (hasPermission(sender, "hmccosmetics.cmd.reload")) completions.add("reload");
            if (hasPermission(sender, "hmccosmetics.cmd.wardrobe")) completions.add("wardrobe");
            if (hasPermission(sender, "hmccosmetics.cmd.dataclear")) completions.add("dataclear");
            if (hasPermission(sender, "hmccosmetics.cmd.dye")) completions.add("dye");
            if (hasPermission(sender, "hmccosmetics.cmd.setwardrobesetting")) completions.add("setwardrobesetting");
            if (hasPermission(sender, "hmccosmetics.cmd.hide")) completions.add("hide");
            if (hasPermission(sender, "hmccosmetics.cmd.show")) completions.add("show");
            if (hasPermission(sender, "hmccosmetics.cmd.debug")) completions.add("debug");
            if (hasPermission(sender, "hmccosmetics.cmd.emote")) completions.add("emote");
            if (hasPermission(sender, "hmccosmetics.cmd.playemote")) completions.add("playemote");
            if (hasPermission(sender, "hmccosmetics.cmd.disableall")) completions.add("disableall");
            if (hasPermission(sender, "hmccosmetics.cmd.hiddenreasons")) completions.add("hiddenreasons");
            if (hasPermission(sender, "hmccosmetics.cmd.clearhiddenreasons")) completions.add("clearhiddenreasons");

            StringUtil.copyPartialMatches(args[0], completions, finalCompletions);
        }

        if (!(sender instanceof Player)) return completions;
        CosmeticUser user = CosmeticUsers.getUser(((Player) sender).getUniqueId());
        if (user == null) return completions; // User hasn't loaded in yet, can't do proper checks

        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "apply" -> {
                    completions.addAll(applyCommandComplete(user, args));
                }
                case "unapply" -> {
                    for (Cosmetic cosmetic : user.getCosmetics()) {
                        completions.add(cosmetic.getSlot().toString().toUpperCase());
                    }
                    completions.add("ALL");
                }
                case "menu" -> {
                    for (Menu menu : Menus.getMenu()) {
                        if (menu.canOpen(user.getPlayer())) completions.add(menu.getId());
                    }
                }
                case "dataclear", "hide", "show", "emote", "hiddenreasons", "clearhiddenreasons" -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        completions.add(player.getName());
                    }
                }
                case "disableall" -> {
                    completions.add("true");
                    completions.add("false");
                }
                case "wardrobe" -> {
                    for (Wardrobe wardrobe : WardrobeSettings.getWardrobes()) {
                        if (wardrobe.hasPermission()) {
                            if (user.getPlayer().hasPermission(wardrobe.getPermission())) completions.add(wardrobe.getId());
                        } else {
                            completions.add(wardrobe.getId());
                        }
                    }
                }
                case "dye" -> {
                    for (CosmeticSlot slot : user.getDyeableSlots()) {
                        completions.add(slot.toString());
                    }
                }
                case "setwardrobesetting" -> {
                    for (Wardrobe wardrobe : WardrobeSettings.getWardrobes()) {
                        completions.add(wardrobe.getId());
                    }
                }
                case "playemote" -> completions.addAll(EmoteManager.getAllNames());
            }
            StringUtil.copyPartialMatches(args[1], completions, finalCompletions);
        }
        if (args.length == 3) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "dye" -> {
                    completions.add("#FFFFFF");
                }
                case "menu", "wardrobe", "apply", "unapply", "playemote" -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        completions.add(player.getName());
                    }
                }
                case "setwardrobesetting" -> {
                    completions.add("npclocation");
                    completions.add("viewerlocation");
                    completions.add("leavelocation");
                    completions.add("permission");
                    completions.add("distance");
                }
            }
            StringUtil.copyPartialMatches(args[2], completions, finalCompletions);
        }

        if (args.length == 4) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "apply" -> {
                    if (Hooks.isActiveHook("HMCColor")) completions.addAll(HMCColorContextKt.getHmcColor().getConfig().getColors().keySet());
                    completions.add("#FFFFFF");
                }
            }
            StringUtil.copyPartialMatches(args[3], completions, finalCompletions);
        }

        Collections.sort(finalCompletions);
        return finalCompletions;
    }

    @NotNull
    private static List<String> applyCommandComplete(CosmeticUser user, String @NotNull [] args) {
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

    private boolean hasPermission(@NotNull CommandSender sender, String permission) {
        if (sender.isOp()) return true;
        if (sender.hasPermission(permission)) return true;
        return false;
    }
}
