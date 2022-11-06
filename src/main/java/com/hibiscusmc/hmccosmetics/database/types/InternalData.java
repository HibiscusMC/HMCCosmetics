package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class InternalData extends Data {

    @Override
    public void save(CosmeticUser user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        for (Cosmetic cosmetic : user.getCosmetic()) {
            NamespacedKey slotkey = new NamespacedKey(HMCCosmeticsPlugin.getInstance(), cosmetic.getSlot().toString());
            player.getPersistentDataContainer().set(slotkey, PersistentDataType.STRING, cosmetic.getId());
        }
    }

    @Override
    public CosmeticUser get(UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        CosmeticUser user = new CosmeticUser(uniqueId);
        for (CosmeticSlot slot : CosmeticSlot.values()) {
            NamespacedKey key = new NamespacedKey(HMCCosmeticsPlugin.getInstance(), slot.toString());
            if (!player.getPersistentDataContainer().has(key, PersistentDataType.STRING)) continue;
            user.addPlayerCosmetic(Cosmetics.getCosmetic(player.getPersistentDataContainer().get(key, PersistentDataType.STRING)));
        }
        return user;
    }
}
