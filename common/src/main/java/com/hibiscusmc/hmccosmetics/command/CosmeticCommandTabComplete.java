package com.hibiscusmc.hmccosmetics.command;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
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

        if (args.length == 1) {
            completions.add("apply");
            completions.add("wardrobe");
            completions.add("unapply");
            completions.add("menu");
            completions.add("reload");
            completions.add("dataclear");
        }

        // This needs to be redone.
        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("apply")) {
                completions.addAll(applyCommandComplete(args));
            } else if (args[0].equalsIgnoreCase("unapply")) {
                for (CosmeticSlot slot : CosmeticSlot.values()) {
                    completions.add(slot.name());
                }
            } else if (args[0].equalsIgnoreCase("menu")) {
                completions.addAll(Menus.getMenuNames());
            } else if (args[0].equalsIgnoreCase("dataclear")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        }

        Collections.sort(completions);
        return completions;
    }

    private static List<String> applyCommandComplete(String[] args) {
        List<String> completitions = new ArrayList<>();

        if (args.length == 2) {
            completitions.addAll(Cosmetics.keys());
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
