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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        if (user.getHidden()) {
            if (shouldHiddenSave(user.getHiddenReason())) {
                data.append("HIDDEN=").append(user.getHiddenReason());
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
        for (String a : rawData) {
            if (a == null || a.isEmpty()) continue;
            String[] splitData = a.split("=");
            CosmeticSlot slot = null;
            Cosmetic cosmetic = null;
            MessagesUtil.sendDebugMessages("First split (suppose slot) " + splitData[0]);
            if (splitData[0].equalsIgnoreCase("HIDDEN")) {
                if (EnumUtils.isValidEnum(CosmeticUser.HiddenReason.class, splitData[1])) {
                    if (Settings.isForceShowOnJoin()) continue;
                    Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
                        user.hideCosmetics(CosmeticUser.HiddenReason.valueOf(splitData[1]));
                    });
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
        return cosmetics;
    }

    private boolean shouldHiddenSave(CosmeticUser.HiddenReason reason) {
        switch (reason) {
            case EMOTE, NONE, GAMEMODE, WORLD -> {
                return false;
            }
            default -> {
                return true;
            }
        }
    }
}
