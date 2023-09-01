package com.hibiscusmc.hmccosmetics.user;

import com.google.common.collect.HashBiMap;
import com.hibiscusmc.hmccosmetics.util.ServerUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class CosmeticUsers {

    private static HashBiMap<UUID, CosmeticUser> COSMETIC_USERS = HashBiMap.create();

    public static void addUser(CosmeticUser user) {
        if (COSMETIC_USERS.containsKey(user.getUniqueId())) return; // do not add if already exists
        COSMETIC_USERS.put(user.getUniqueId(), user);
    }

    public static void removeUser(UUID uuid) {
        COSMETIC_USERS.remove(uuid);
    }

    public static void removeUser(CosmeticUser user) {
        COSMETIC_USERS.remove(user);
    }

    @Nullable
    public static CosmeticUser getUser(UUID uuid) {
        return COSMETIC_USERS.get(uuid);
    }

    @Nullable
    public static CosmeticUser getUser(Player player) {
        return COSMETIC_USERS.get(player.getUniqueId());
    }

    @Nullable
    public static CosmeticUser getUser(int entityId) {
        Entity entity = ServerUtils.getEntity(entityId);
        if (entity == null) return null;
        if (!(entity instanceof Player player)) return null;
        return COSMETIC_USERS.get(player.getUniqueId());
    }

    public static Set<CosmeticUser> values() {
        return COSMETIC_USERS.values();
    }
}
