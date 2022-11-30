package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;

public class InternalData extends Data {

    NamespacedKey key = new NamespacedKey(HMCCosmeticsPlugin.getInstance(), "cosmetics");

    @Override
    public void setup() {
        // Nothing
    }

    @Override
    public void save(CosmeticUser user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());

        player.getPersistentDataContainer().set(key, PersistentDataType.STRING, steralizeData(user));
    }

    @Override
    public CosmeticUser get(UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        CosmeticUser user = new CosmeticUser(uniqueId);

        if (!player.getPersistentDataContainer().has(key, PersistentDataType.STRING)) return user;
        String rawData = player.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        Map<CosmeticSlot, Cosmetic> a = desteralizedata(rawData);
        for (CosmeticSlot slot : a.keySet()) {
            user.addPlayerCosmetic(a.get(slot));
            //HMCCosmeticsPlugin.getInstance().getLogger().info("Retrieved " + player.getName() + " | slot " + slot + " | cosmetic " + Cosmetics.getCosmetic(player.getPersistentDataContainer().get(key, PersistentDataType.STRING)));
        }
        return user;
    }

    @Override
    public void clear(UUID uniqueId) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uniqueId);

        if (player.isOnline()) {
            Player onlinePlayer = player.getPlayer();
            if (onlinePlayer.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                onlinePlayer.getPersistentDataContainer().remove(key);
            }
            return;
        }
    }
}
