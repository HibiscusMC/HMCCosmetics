package com.hibiscusmc.hmccosmetics.nms.v1_19_R3;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;

public class HMCArmorStand extends ArmorStand {

    public HMCArmorStand(Level world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public HMCArmorStand(Location loc) {
        super(((CraftWorld) loc.getWorld()).getHandle(), loc.getX(), loc.getY(), loc.getZ());
        this.setPos(loc.getX(), loc.getY(), loc.getZ());
        setInvisible(true);
        setInvulnerable(true);
        setMarker(true);
        setSilent(true);
        getBukkitLivingEntity().setCollidable(false);
        persist = false;
    }
}
