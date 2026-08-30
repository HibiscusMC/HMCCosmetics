package com.hibiscusmc.hmccosmetics.command;

import com.hibiscusmc.hmccolor.HMCColorContextKt;
import com.hibiscusmc.hmccosmetics.config.section.Wardrobe;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
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

        // Null for console; user-specific filtering is skipped in that case
        Player senderPlayer = sender instanceof Player ? (Player) sender : null;
        CosmeticUser user = senderPlayer == null ? null : CosmeticUsers.getUser(senderPlayer.getUniqueId());

        if (args.length == 1) {
            completions.add("help");
            for (CosmeticCommand.SubCommandInfo info : CosmeticCommand.SUBCOMMANDS) {
                if (CosmeticCommand.hasPermission(sender, info.permission())) completions.add(info.name());
            }
            StringUtil.copyPartialMatches(args[0], completions, finalCompletions);
        }

        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "apply" -> {
                    for (Cosmetic cosmetic : Cosmetics.values()) {
                        if (user != null && !user.canEquipCosmetic(cosmetic)) continue;
                        completions.add(cosmetic.getId());
                    }
                }
                case "unapply" -> {
                    if (user != null) {
                        for (Cosmetic cosmetic : user.getCosmetics()) {
                            completions.add(cosmetic.getSlot().toString().toUpperCase());
                        }
                    } else {
                        completions.addAll(CosmeticSlot.values().keySet());
                    }
                    completions.add("ALL");
                }
                case "menu" -> {
                    for (Menu menu : Menus.getMenu()) {
                        if (user != null && !menu.canOpen(user.getPlayer())) continue;
                        completions.add(menu.getId());
                    }
                }
                case "dataclear", "hide", "show", "toggle", "hiddenreasons", "clearhiddenreasons" -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        completions.add(player.getName());
                    }
                }
                case "disableall" -> {
                    completions.add("true");
                    completions.add("false");
                }
                case "wardrobes" -> {
                    for (Wardrobe wardrobe : WardrobeSettings.getWardrobes()) {
                        if (user != null && wardrobe.hasPermission() && !user.getPlayer().hasPermission(wardrobe.getPermission())) continue;
                        completions.add(wardrobe.getId());
                    }
                }
                case "dye" -> {
                    if (user != null) {
                        for (CosmeticSlot slot : user.getDyeableSlots()) {
                            completions.add(slot.toString());
                        }
                    }
                }
                case "setwardrobesetting" -> {
                    for (Wardrobe wardrobe : WardrobeSettings.getWardrobes()) {
                        completions.add(wardrobe.getId());
                    }
                }
            }
            StringUtil.copyPartialMatches(args[1], completions, finalCompletions);
        }

        if (args.length == 3) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "dye" -> {
                    completions.add("#FFFFFF");
                }
                case "menu", "wardrobes", "apply", "unapply" -> {
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
                    completions.add("defaultmenu");
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
                case "setwardrobesetting" -> {
                    if (args[2].equalsIgnoreCase("defaultmenu")) {
                        completions.addAll(Menus.getMenuNames());
                    }
                }
            }
            StringUtil.copyPartialMatches(args[3], completions, finalCompletions);
        }

        Collections.sort(finalCompletions);
        return finalCompletions;
    }
}
