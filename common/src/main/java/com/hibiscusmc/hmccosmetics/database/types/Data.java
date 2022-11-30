package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
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

    public String steralizeData(CosmeticUser user) {
        String data = "";
        for (Cosmetic cosmetic : user.getCosmetic()) {
            String input = cosmetic.getSlot() + "=" + cosmetic.getId();
            if (data.length() == 0) {
                data = input;
                continue;
            }
            data = data + "," + input;
        }
        return data;
    }

    public Map<CosmeticSlot, Cosmetic> desteralizedata(String raw) {
        Map<CosmeticSlot, Cosmetic> cosmetics = new HashMap<>();

        String[] rawData = raw.split(",");
        for (String a : rawData) {
            if (a == null || a.isEmpty()) continue;
            String[] splitData = a.split("=");
            CosmeticSlot slot = null;
            Cosmetic cosmetic = null;
            HMCCosmeticsPlugin.getInstance().getLogger().info("First split (suppose slot) " + splitData[0]);
            if (CosmeticSlot.valueOf(splitData[0]) != null) slot = CosmeticSlot.valueOf(splitData[0]);
            if (Cosmetics.hasCosmetic(splitData[1])) cosmetic = Cosmetics.getCosmetic(splitData[1]);
            if (slot == null || cosmetic == null) continue;
            cosmetics.put(slot, cosmetic);
        }
        return cosmetics;
    }
}
