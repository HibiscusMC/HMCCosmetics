package com.hibiscusmc.hmccosmetics.nms.v1_19_R1;

import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;

public class NMSHandler implements com.hibiscusmc.hmccosmetics.nms.NMSHandler {
    @Override
    public int getNextEntityId() {
        return Entity.nextEntityId();
    }



    @Override
    public boolean getSupported() {
        return true;
    }
}
