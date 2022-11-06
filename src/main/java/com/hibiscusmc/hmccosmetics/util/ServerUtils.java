package com.hibiscusmc.hmccosmetics.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.Entity;

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

    public static Entity getEntity(int entityId) {
        return getNMSEntity(entityId).getBukkitEntity();
    }

    public static net.minecraft.world.entity.Entity getNMSEntity(int entityId) {
        for (ServerLevel world : ((CraftServer) Bukkit.getServer()).getHandle().getServer().getAllLevels()) {
            net.minecraft.world.entity.Entity entity = world.getEntity(entityId);
            if (entity == null) return null;
            return entity;
        }
        return null;
    }
}
