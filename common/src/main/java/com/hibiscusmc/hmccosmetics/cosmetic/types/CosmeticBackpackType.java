package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.manager.UserBackpackManager;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.logging.Level;

public class CosmeticBackpackType extends Cosmetic {

    private final String modelName;
    private UserBackpackManager.BackpackType backpackType;

    public CosmeticBackpackType(String id, ConfigurationNode config) {
        super(id, config);

        modelName = config.node("model").getString();
        backpackType = UserBackpackManager.BackpackType.valueOf(config.node("type").getString("NORMAL").toUpperCase());
    }

    @Override
    public void update(@NotNull CosmeticUser user) {
        Entity entity = Bukkit.getEntity(user.getUniqueId());
        if (entity == null) return;

        Location loc = entity.getLocation().clone().add(0, 2, 0);

        if (user.isInWardrobe() || !user.isBackpackSpawned()) return;
        if (!user.getUserBackpackManager().IsValidBackpackEntity()) {
            MessagesUtil.sendDebugMessages("Invalid Backpack Entity[owner=" + user.getUniqueId() + ",player_location=" + loc + "]!", Level.WARNING);
            user.respawnBackpack();
            return;
        }
        if (loc.getWorld() != user.getUserBackpackManager().getArmorStand().getWorld()) {
            user.getUserBackpackManager().getArmorStand().teleport(loc);
        }

        user.getUserBackpackManager().getArmorStand().teleport(loc);

        if (user.getUserBackpackManager().getBackpackType().equals(UserBackpackManager.BackpackType.FIRST_PERSON)) {
            ArrayList<Integer> particleCloud = user.getUserBackpackManager().getAreaEffectEntityId();
            for (int i = 0; i < particleCloud.size(); i++) {
                //particleCloud.get(i).teleport(loc);
                if (i == 0) {
                    PacketManager.sendRidingPacket(entity.getEntityId(), particleCloud.get(i), loc);
                } else {
                    PacketManager.sendRidingPacket(particleCloud.get(i - 1), particleCloud.get(i) , loc);
                }
                MessagesUtil.sendDebugMessages("num: " + i + " / valid? ");
            }
            //PacketManager.sendRidingPacket(entity.getEntityId(), user.getUserBackpackManager().getAreaEffectEntityId(), loc);
            PacketManager.sendRidingPacket(particleCloud.get(particleCloud.size() - 1), user.getUserBackpackManager().getFirstArmorStandId(), loc);
            MessagesUtil.sendDebugMessages("ParticleCloud: " + particleCloud.toString());
        } else {
            PacketManager.sendRidingPacket(entity.getEntityId(), user.getUserBackpackManager().getFirstArmorStandId(), loc);
        }

        user.getUserBackpackManager().getArmorStand().setRotation(loc.getYaw(), loc.getPitch());
        user.getUserBackpackManager().showBackpack();
    }

    public String getModelName() {
        return modelName;
    }

    public UserBackpackManager.BackpackType getBackpackType() {
        return backpackType;
    }
}
