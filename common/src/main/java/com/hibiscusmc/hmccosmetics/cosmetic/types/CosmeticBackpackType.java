package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

public class CosmeticBackpackType extends Cosmetic {

    private final String modelName;

    public CosmeticBackpackType(String id, ConfigurationNode config) {
        super(id, config);

        modelName = config.node("model").getString();
    }

    @Override
    public void update(@NotNull CosmeticUser user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        if (player == null) return;

        Location loc = player.getLocation().clone().add(0, 2, 0);

        if (user.isInWardrobe() || !user.isBackupSpawned()) return;
        if (loc.getWorld() != user.getUserBackpackManager().getArmorStand().getWorld()) {
            user.getUserBackpackManager().getArmorStand().teleport(loc);
        }

        user.getUserBackpackManager().getArmorStand().teleport(loc);

        PacketManager.sendRidingPacket(player.getEntityId(), user.getUserBackpackManager().getFirstArmorStandId(), loc);

        user.getUserBackpackManager().getArmorStand().setRotation(loc.getYaw(), loc.getPitch());
        user.getUserBackpackManager().showBackpack();
    }

    public String getModelName() {
        return modelName;
    }
}
