package com.hibiscusmc.hmccosmetics.nms.v1_19_R2;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cod;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.persistence.PersistentDataType;

public class MEGEntity extends Cod {

    public MEGEntity(Location loc) {
            super(EntityType.COD, ((CraftWorld) loc.getWorld()).getHandle());
            this.setPos(loc.getX(), loc.getY(), loc.getZ());
            MessagesUtil.sendDebugMessages("Spawned MEGEntity at " + loc);
            getBukkitLivingEntity().setInvisible(true);
            getBukkitLivingEntity().setInvulnerable(true); // NOTE - CREATIVE PLAYERS CAN DESTROY IT STILL
            getBukkitLivingEntity().setAI(false);
            getBukkitLivingEntity().setGravity(false);
            getBukkitLivingEntity().setSilent(true);
            getBukkitLivingEntity().setCollidable(false);
            persist = false;

            getBukkitEntity().getPersistentDataContainer().set(new NamespacedKey(HMCCosmeticsPlugin.getInstance(), "cosmeticMob"), PersistentDataType.SHORT, Short.valueOf("1"));

            ((CraftWorld) loc.getWorld()).getHandle().addFreshEntity(this);
    }
}
