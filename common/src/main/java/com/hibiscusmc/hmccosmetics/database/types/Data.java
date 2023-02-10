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
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Data {

    public void setup() {
        // Override
    }

    public void save(CosmeticUser user) {
        // Override
    }

    @Nullable
    public CosmeticUser get(UUID uniqueId) {
        // Override
        return null;
    }

    public void clear(UUID uniqueId) {
        // Override
    }
    // BACKPACK=colorfulbackpack&RRGGBB,HELMET=niftyhat,BALLOON=colorfulballoon,CHESTPLATE=niftychestplate
    public String steralizeData(CosmeticUser user) {
        String data = "";
        if (user.getHidden()) {
            if (shouldHiddenSave(user.getHiddenReason())) {
                data = "HIDDEN=" + user.getHiddenReason();
            }
        }
        for (Cosmetic cosmetic : user.getCosmetic()) {
            Color color = user.getCosmeticColor(cosmetic.getSlot());
            String input = cosmetic.getSlot() + "=" + cosmetic.getId();
            if (color != null) input = input + "&" + color.asRGB();
            if (data.length() == 0) {
                data = input;
                continue;
            }
            data = data + "," + input;
        }
        return data;
    }

    public Map<CosmeticSlot, Map<Cosmetic, Color>> desteralizedata(CosmeticUser user, String raw) {
        Map<CosmeticSlot, Map<Cosmetic, Color>> cosmetics = new HashMap<>();
        boolean checkPermission = Settings.getForcePermissionJoin();

        String[] rawData = raw.split(",");
        for (String a : rawData) {
            if (a == null || a.isEmpty()) continue;
            String[] splitData = a.split("=");
            CosmeticSlot slot = null;
            Cosmetic cosmetic = null;
            MessagesUtil.sendDebugMessages("First split (suppose slot) " + splitData[0]);
            if (splitData[0].equalsIgnoreCase("HIDDEN")) {
                if (EnumUtils.isValidEnum(CosmeticUser.HiddenReason.class, splitData[1])) {
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
                if (cosmetic.requiresPermission() && checkPermission) {
                    if (!user.getPlayer().hasPermission(cosmetic.getPermission())) {
                        continue;
                    }
                }
                cosmetics.put(slot, Map.of(cosmetic, Color.fromRGB(Integer.parseInt(colorSplitData[1]))));
            } else {
                if (Cosmetics.hasCosmetic(splitData[1])) cosmetic = Cosmetics.getCosmetic(splitData[1]);
                if (slot == null || cosmetic == null) continue;
                if (cosmetic.requiresPermission() && checkPermission) {
                    if (!user.getPlayer().hasPermission(cosmetic.getPermission())) {
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
            case EMOTE, NONE -> {
                return false;
            }
            default -> {
                return true;
            }
        }
    }
}
