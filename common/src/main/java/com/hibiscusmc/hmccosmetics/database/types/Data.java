package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Data {

    public abstract void setup();

    public abstract void save(CosmeticUser user);

    @Nullable
    public abstract CosmeticUser get(UUID uniqueId);

    public abstract void clear(UUID uniqueId);

    // BACKPACK=colorfulbackpack&RRGGBB,HELMET=niftyhat,BALLOON=colorfulballoon,CHESTPLATE=niftychestplate
    @NotNull
    public final String serializeData(@NotNull CosmeticUser user) {
        StringBuilder data = new StringBuilder();
        if (user.isHidden()) {
            for (CosmeticUser.HiddenReason reason :  user.getHiddenReasons()) {
                if (shouldHiddenSave(reason)) data.append("HIDDEN=").append(reason);
            }
        }
        for (Cosmetic cosmetic : user.getCosmetics()) {
            Color color = user.getCosmeticColor(cosmetic.getSlot());
            String input = cosmetic.getSlot() + "=" + cosmetic.getId();
            if (color != null) input = input + "&" + color.asRGB();
            if (data.isEmpty()) {
                data.append(input);
                continue;
            }
            data.append(",").append(input);
        }
        return data.toString();
    }

    public final Map<CosmeticSlot, Map<Cosmetic, Color>> deserializeData(CosmeticUser user, @NotNull String raw) {
        return deserializeData(user, raw, Settings.isForcePermissionJoin());
    }

    @NotNull
    public final Map<CosmeticSlot, Map<Cosmetic, Color>> deserializeData(CosmeticUser user, @NotNull String raw, boolean permissionCheck) {
        Map<CosmeticSlot, Map<Cosmetic, Color>> cosmetics = new HashMap<>();

        String[] rawData = raw.split(",");
        ArrayList<CosmeticUser.HiddenReason> hiddenReason = new ArrayList<>();
        for (String a : rawData) {
            if (a == null || a.isEmpty()) continue;
            String[] splitData = a.split("=");
            CosmeticSlot slot = null;
            Cosmetic cosmetic = null;
            MessagesUtil.sendDebugMessages("First split (suppose slot) " + splitData[0]);
            if (splitData[0].equalsIgnoreCase("HIDDEN")) {
                if (EnumUtils.isValidEnum(CosmeticUser.HiddenReason.class, splitData[1])) {
                    if (Settings.isForceShowOnJoin()) continue;
                    hiddenReason.add(CosmeticUser.HiddenReason.valueOf(splitData[1]));
                }
                continue;
            }
            if (CosmeticSlot.valueOf(splitData[0]) != null) slot = CosmeticSlot.valueOf(splitData[0]);
            if (splitData[1].contains("&")) {
                String[] colorSplitData = splitData[1].split("&");
                if (Cosmetics.hasCosmetic(colorSplitData[0])) cosmetic = Cosmetics.getCosmetic(colorSplitData[0]);
                if (slot == null || cosmetic == null) continue;
                if (permissionCheck && cosmetic.requiresPermission()) {
                    if (user.getPlayer() != null && !user.getPlayer().hasPermission(cosmetic.getPermission())) {
                        continue;
                    }
                }
                cosmetics.put(slot, Map.of(cosmetic, Color.fromRGB(Integer.parseInt(colorSplitData[1]))));
            } else {
                if (Cosmetics.hasCosmetic(splitData[1])) cosmetic = Cosmetics.getCosmetic(splitData[1]);
                if (slot == null || cosmetic == null) continue;
                if (permissionCheck && cosmetic.requiresPermission()) {
                    if (user.getPlayer() != null && !user.getPlayer().hasPermission(cosmetic.getPermission())) {
                        continue;
                    }
                }
                HashMap<Cosmetic, Color> cosmeticColorHashMap = new HashMap<>();
                cosmeticColorHashMap.put(cosmetic, null);
                cosmetics.put(slot, cosmeticColorHashMap);
            }
        }

        MessagesUtil.sendDebugMessages("Hidden Reason: " + hiddenReason);
        // if else this, if else that, if else I got to deal with this anymore i'll lose my mind
        if (!hiddenReason.isEmpty()) {
            for (CosmeticUser.HiddenReason reason : hiddenReason) user.silentlyAddHideFlag(reason);
        } else {
            Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
                // Handle gamemode check
                if (user.getPlayer() != null && Settings.getDisabledGamemodes().contains(user.getPlayer().getGameMode().toString())) {
                    MessagesUtil.sendDebugMessages("Hiding Cosmetics due to gamemode");
                    user.hideCosmetics(CosmeticUser.HiddenReason.GAMEMODE);
                    return;
                } else {
                    if (user.isHidden(CosmeticUser.HiddenReason.GAMEMODE)) {
                        MessagesUtil.sendDebugMessages("Join Gamemode Check: Showing Cosmetics");
                        user.showCosmetics(CosmeticUser.HiddenReason.GAMEMODE);
                        return;
                    }
                }
                // Handle world check
                if (user.getPlayer() != null && Settings.getDisabledWorlds().contains(user.getPlayer().getWorld().getName())) {
                    MessagesUtil.sendDebugMessages("Hiding Cosmetics due to world");
                    user.hideCosmetics(CosmeticUser.HiddenReason.WORLD);
                } else {
                    if (user.isHidden(CosmeticUser.HiddenReason.WORLD)) {
                        MessagesUtil.sendDebugMessages("Join World Check: Showing Cosmetics");
                        user.showCosmetics(CosmeticUser.HiddenReason.WORLD);
                    }
                }
                if (Settings.isAllPlayersHidden()) {
                    user.hideCosmetics(CosmeticUser.HiddenReason.DISABLED);
                }
            });
        }
        return cosmetics;
    }

    private boolean shouldHiddenSave(CosmeticUser.HiddenReason reason) {
        switch (reason) {
            case EMOTE, NONE, GAMEMODE, WORLD, DISABLED -> {
                return false;
            }
            default -> {
                return true;
            }
        }
    }
}
