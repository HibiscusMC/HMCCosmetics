package com.hibiscusmc.hmccosmetics.nms.v1_19_R1;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;

public class InvisibleArmorstand extends ArmorStand {

    public InvisibleArmorstand(Level world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public InvisibleArmorstand(Location loc) {
        super(((CraftWorld) loc.getWorld()).getHandle(), loc.getX(), loc.getY(), loc.getZ());
        this.setPos(loc.getX(), loc.getY(), loc.getZ());
        setInvisible(true);
        setInvulnerable(true);
        setMarker(true);
        getBukkitLivingEntity().setCollidable(false);
        persist = false;
    }
}
