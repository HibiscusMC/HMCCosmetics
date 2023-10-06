package com.hibiscusmc.hmccosmetics.nms.v1_20_R2;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.persistence.PersistentDataType;

public class MEGEntity extends ArmorStand {
        public MEGEntity(Location loc) {
                super(EntityType.ARMOR_STAND, ((CraftWorld) loc.getWorld()).getHandle());
                this.setPos(loc.getX(), loc.getY(), loc.getZ());

                MessagesUtil.sendDebugMessages("Spawned MEGEntity at " + loc);
                setInvisible(true);
                setNoGravity(true);
                setSilent(true);
                setInvulnerable(true);
                setSmall(true);
                setMarker(true);

                persist = false;
                getBukkitEntity().getPersistentDataContainer().set(new NamespacedKey(HMCCosmeticsPlugin.getInstance(), "cosmeticMob"), PersistentDataType.SHORT, Short.valueOf("1"));

                ((CraftWorld) loc.getWorld()).getHandle().addFreshEntity(this);
        }
}
