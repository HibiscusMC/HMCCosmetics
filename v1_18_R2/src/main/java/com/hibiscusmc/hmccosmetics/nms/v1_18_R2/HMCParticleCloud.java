package com.hibiscusmc.hmccosmetics.nms.v1_18_R2;


import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;

public class HMCParticleCloud extends AreaEffectCloud {

    public HMCParticleCloud(Level world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public HMCParticleCloud(Location loc) {
        super(((CraftWorld) loc.getWorld()).getHandle(), loc.getX(), loc.getY(), loc.getZ());
        this.setPos(loc.getX(), loc.getY(), loc.getZ());
        setInvisible(true);
        setInvulnerable(true);
        setSilent(true);
        setNoGravity(true);
        persist = false;
    }
}
