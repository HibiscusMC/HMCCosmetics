package com.hibiscusmc.hmccosmetics.entities;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.projectile.DragonFireball;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;

public class MEGEntity extends Chicken {

    public MEGEntity(Location loc) {
        super(EntityType.CHICKEN, ((CraftWorld) loc.getWorld()).getHandle());
        this.setPos(loc.getX(), loc.getY(), loc.getZ());
        HMCCosmeticsPlugin.getInstance().getLogger().info("Spawned MEGEntity at " + loc);
        getBukkitLivingEntity().setInvisible(true);
        setInvulnerable(true);
        setNoAi(true);
        setNoGravity(true);
        persist = false;
    }
}
