package com.hibiscusmc.hmccosmetics.command;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
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
            completions.add("apply");
            completions.add("wardrobe");
            completions.add("unapply");
            completions.add("menu");
            completions.add("reload");
            completions.add("dataclear");
            completions.add("dye");
            completions.add("setlocation");

            StringUtil.copyPartialMatches(args[0], completions, finalCompletitons);
        }

        if (!(sender instanceof Player)) return completions;
        CosmeticUser user = CosmeticUsers.getUser(((Player) sender).getUniqueId());

        if (args.length >= 2) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "apply" -> {
                    completions.addAll(applyCommandComplete(user, args));
                }
                case "upapply" -> {
                    for (CosmeticSlot slot : CosmeticSlot.values()) {
                        completions.add(slot.toString());
                    }
                }
                case "menu" -> {
                    completions.addAll(Menus.getMenuNames());
                }
                case "dataclear", "wardrobe" -> {
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
}
