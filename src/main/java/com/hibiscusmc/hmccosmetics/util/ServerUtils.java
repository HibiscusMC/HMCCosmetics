package com.hibiscusmc.hmccosmetics.util;

import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.jetbrains.annotations.Nullable;

public class ServerUtils {

    /**
     * Converts a bukkit gamemode into an integer for use in packets
     * @param gamemode Bukkit gamemode to convert.
     * @return int of the gamemode
     */
    public static int convertGamemode(final GameMode gamemode) {
        return switch (gamemode) {
            case SURVIVAL -> 0;
            case CREATIVE -> 1;
            case ADVENTURE -> 2;
            case SPECTATOR -> 3;
        };
    }

    @Nullable
    public static org.bukkit.entity.Entity getEntity(int entityId) {
        net.minecraft.world.entity.Entity entity = getNMSEntity(entityId);
        if (entity == null) return null;
        return entity.getBukkitEntity();
    }
    @Nullable
    public static net.minecraft.world.entity.Entity getNMSEntity(int entityId) {
        for (ServerLevel world : ((CraftServer) Bukkit.getServer()).getHandle().getServer().getAllLevels()) {
            net.minecraft.world.entity.Entity entity = world.getEntity(entityId);
            if (entity == null) return null;
            return entity;
        }
        return null;
    }
}
