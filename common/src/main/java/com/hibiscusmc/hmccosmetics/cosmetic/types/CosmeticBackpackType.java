package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.manager.UserBackpackManager;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

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
        Player player = Bukkit.getPlayer(user.getUniqueId());
        if (player == null) return;

        Location loc = player.getLocation().clone().add(0, 2, 0);

        if (user.isInWardrobe() || !user.isBackpackSpawned()) return;
        if (!user.getUserBackpackManager().getArmorStand().isValid()) {
            MessagesUtil.sendDebugMessages("Invalid Backpack detected! Respawning backpack, report this on the discord if this happens often!", Level.WARNING);
            user.respawnBackpack();
            return;
        }
        if (loc.getWorld() != user.getUserBackpackManager().getArmorStand().getWorld()) {
            user.getUserBackpackManager().getArmorStand().teleport(loc);
        }

        user.getUserBackpackManager().getArmorStand().teleport(loc);

        if (user.getUserBackpackManager().getBackpackType().equals(UserBackpackManager.BackpackType.FIRST_PERSON)) {
            user.getUserBackpackManager().teleportEffectEntity(loc);
            PacketManager.sendRidingPacket(player.getEntityId(), user.getUserBackpackManager().getAreaEffectEntityId(), loc);
            PacketManager.sendRidingPacket(user.getUserBackpackManager().getAreaEffectEntityId(), user.getUserBackpackManager().getFirstArmorStandId(), loc);
        } else {
            PacketManager.sendRidingPacket(player.getEntityId(), user.getUserBackpackManager().getFirstArmorStandId(), loc);
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
